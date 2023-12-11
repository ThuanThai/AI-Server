package com.example.app_server.controller;

import com.example.app_server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/file")
@CrossOrigin("*")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file")MultipartFile file) {
       try {
           File xmlFile = fileService.covertDocToXml(file);
           fileService.convertXmlToDoc(xmlFile);
           return ResponseEntity.ok("Done");
       } catch (IOException e) {
           e.printStackTrace();
           return ResponseEntity.status(500).body("Error converting DOCX to XML");
       } catch (ParserConfigurationException e) {
           throw new RuntimeException(e);
       } catch (SAXException e) {
           throw new RuntimeException(e);
       }
    }
    @GetMapping
    public String hello() {
        return "Hello World";
    }
}
