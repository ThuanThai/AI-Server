package com.example.app_server.service;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {
    private String extractFontSize(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();

        // Check if runs list is not empty
        if (!runs.isEmpty()) {
            // Extract font size from the first run in the paragraph
            XWPFRun run = runs.get(0);

            // Get font size in half points (multiply by 2 to convert to points)
            int fontSize = run.getFontSize();

            // Check if font size is explicitly set
            if (fontSize != -1) {
                // Convert to points and return as a string
                return String.valueOf(fontSize) + "pt";
            }
        }

        // If runs list is empty or font size is not explicitly set, return a default value
        return "12pt"; // Or any other default value you prefer
    }

// Similarly update other methods (extractFontFamily, extractFontWeight, extractFontStyle) to handle empty runs list

    private String extractFontFamily(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        // Check if runs list is not empty
        if (!runs.isEmpty()) {
            // Extract font family from the first run in the paragraph
            XWPFRun run = runs.get(0);
            // Get font family
            String fontFamily = run.getFontFamily();
            return fontFamily != null ?  fontFamily   : "Arial";
        }
        // If runs list is empty, return a default value
        return "Arial";
    }

    private String extractFontWeight(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();

        // Check if runs list is not empty
        if (!runs.isEmpty()) {
            // Extract font weight from the first run in the paragraph
            XWPFRun run = runs.get(0);

            // Get font weight
            boolean isBold = run.isBold();
            return isBold ? "bold" : "normal";
        }

        // If runs list is empty, return a default value
        return "normal";
    }

    private String extractFontStyle(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();

        // Check if runs list is not empty
        if (!runs.isEmpty()) {
            // Extract font style from the first run in the paragraph
            XWPFRun run = runs.get(0);

            // Get font style
            boolean isItalic = run.isItalic();
            return isItalic ? "italic" : "normal";
        }

        // If runs list is empty, return a default value
        return "normal";
    }
    private String extractStyle(XWPFParagraph paragraph) {
        // Extract and return style information as needed
        String fontSize = extractFontSize(paragraph);
        String fontFamily = extractFontFamily(paragraph);
        String fontWeight = extractFontWeight(paragraph);
        String fontStyle = extractFontStyle(paragraph);

        // Format the style attribute
        return String.format("font-size:%s;font-family:%s;font-weight:%s;font-style:%s",
                fontSize, fontFamily, fontWeight, fontStyle);
    }

    private static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }


    private String escapeXml(String input) {
        return input.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;");
    }

    public File covertDocToXml(MultipartFile multipartFile) throws IOException {
        File docFile = convertMultipartFileToFile(multipartFile);

        File xmlFile = new File("converted.xml");
        if (!xmlFile.exists()) {
            xmlFile.createNewFile();
        }
        StringBuilder xmlContent = new StringBuilder("<document>");
        try (FileInputStream fis = new FileInputStream(docFile);
            XWPFDocument document = new XWPFDocument(fis);
            FileOutputStream fos = new FileOutputStream(xmlFile)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String style = extractStyle(paragraph);
                String paragraphText = paragraph.getText();
                String formattedParagraph = String.format("<paragraph style=\"%s\">%s</paragraph>%n", style, escapeXml(paragraphText));
                xmlContent.append(formattedParagraph);
            }
            xmlContent.append("</document>");
            fos.write(xmlContent.toString().getBytes());
        }
        docFile.delete();
        return xmlFile;
    }

    public void  convertXmlToDoc(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        // Create a new DOCX document
        XWPFDocument doc = new XWPFDocument();
        // Process XML elements and add content to the DOCX document
        processXmlElement(document.getDocumentElement(), doc);
        try (FileOutputStream fos = new FileOutputStream("output.docx")) {
            doc.write(fos);
        }
    }

    private void processXmlElement(Element element, XWPFDocument doc) {
        if (element.getNodeName().equals("paragraph")) {
            XWPFParagraph paragraph = doc.createParagraph();
            processTextContent(element, paragraph);
        }

        // Recursively process child nodes
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                processXmlElement((Element) childNode, doc);
            }
        }
    }

    private void processTextContent(Element element, XWPFParagraph paragraph) {
        String styleAttribute = element.getAttribute("style");
        String textContent = element.getTextContent();
        XWPFRun run = paragraph.createRun();
        run.setText(textContent);
        applyStyle(run, styleAttribute);
    }

    private void applyStyle(XWPFRun run, String styleAttribute ) {
        // Extract style attributes from the <style> tag
        if (styleAttribute != null && !styleAttribute.isEmpty()) {
            String[] styleProperties = styleAttribute.split(";");
            for (String property : styleProperties) {
                String[] keyValue = property.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    applyStyleProperty(run, key, value);
                }
            }
        }
    }


    private static void applyStyleProperty(XWPFRun run, String key, String value) {
        // Apply the specified style property to the paragraph
        // You may need to handle different properties accordingly
        switch (key) {
            case "font-size":
            {
                int sizeInPt = Integer.parseInt(value.replace("pt", ""));
                run.setFontSize(sizeInPt);
                break;
            }
            case "font-family": {
                if (!Objects.equals(value, "null")) {
                    run.setFontFamily(value);
                } else {
                    run.setFontFamily("Arial");
                }
                break;
            }
            case "font-weight":
                run.setBold(value.equals("bold"));
                break;
            case "font-style":
                run.setItalic(value.equals("italic"));
                break;
            // Add more cases as needed
        }
    }

}
