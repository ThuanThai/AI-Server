package com.example.app_server.controller;

import com.example.app_server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            File convertedFile = new File("converted.docx");
            if (!convertedFile.exists()) {
                convertedFile.createNewFile();
            }
            //transfer MultipartFile to target file
           try( OutputStream os = new FileOutputStream(convertedFile)){
               os.write(file.getBytes());
           }
            // Call the conversion service
            fileService.covertDocToXml(convertedFile);
            // You can return the XML content or any other response as needed
            return ResponseEntity.ok("done");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error converting DOCX to XML");
        }

    }
    @GetMapping
    public String hello() {
        return "Hello World";
    }
}
