package com.smartstay.smartstay.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesConfig {

//    public static File convertMultipartToFile(MultipartFile file) {
//        File tempFolder = new File("temp-folder");
//        if (!tempFolder.exists()) {
//            tempFolder.mkdir();
//        }
//        File convFile = new File(tempFolder + "/" + file.getOriginalFilename());
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(convFile);
//            fos.write( file.getBytes() );
//            fos.close();
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return convFile;
//    }

    public static File convertMultipartToFile(MultipartFile file)  {
        File convFile = null;
        try {
            convFile = File.createTempFile("admin_", "_" + file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return convFile;
    }

    public static File writePdf(byte[] pdfBytes, String fileName) {
        try {
            Path tempFile = Files.createTempFile(fileName, ".pdf");
            Files.write(tempFile, pdfBytes);
            return tempFile.toFile();

//            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
//
//            Path filePath = tempDir.resolve(fileName + ".pdf");
//            Files.write(filePath, pdfBytes);
//            return tempDir.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write PDF to temp file", e);
        }
    }
}


