package com.example.app_server.service;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class FileService {
    public void covertDocToXml(File docfile) throws IOException {
        StringBuilder xmlContent = new StringBuilder("<document>");
        try (FileInputStream fis = new FileInputStream(docfile);
            XWPFDocument document = new XWPFDocument(fis)){
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                xmlContent.append("<paragraph>").append(paragraph.getText()).append("</paragraph>").append("\n");
            }
            xmlContent.append("</document>");
            fis.close();
        }
        File xmlFile = new File("converted.xml");
        if (!xmlFile.exists()) {
            xmlFile.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(xmlFile)) {
            fos.write(xmlContent.toString().getBytes());
        }
    }
}
