package com.smartstay.smartstay.config;

import io.netty.handler.codec.base64.Base64Decoder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        } catch (IOException e) {
            throw new RuntimeException("Failed to write PDF to temp file", e);
        }
    }

    public static File zipToFile(byte[] pdfBytes, String fileName) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(pdfBytes))) {

            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                throw new RuntimeException("No file found in ZIP");
            }

            Path pdfPath = Files.createTempFile(fileName, ".pdf");
            Files.copy(zis, pdfPath, StandardCopyOption.REPLACE_EXISTING);

            return pdfPath.toFile();
        } catch (IOException e) {
            return null;
        }
    }

    public static File base64ToImage(String customerId, String base64) {
        if (base64 == null) {
            return null;
        }
        String[] parts = base64.split(",");

        String imageString = "";
        if (parts.length > 1) {
            imageString = parts[1];
        }
        else {
            imageString = base64;
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageString);

        try {
            Path tempFile = Files.createTempFile(customerId, ".jpg");
            Files.write(tempFile, imageBytes);
            return tempFile.toFile();
        } catch (IOException e) {
            return null;
        }
    }
}


