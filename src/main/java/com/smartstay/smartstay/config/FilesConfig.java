package com.smartstay.smartstay.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilesConfig {

    public static File convertMultipartToFile(MultipartFile file) {
        File tempFolder = new File("temp-folder");
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }
        File convFile = new File(tempFolder + "/" + file.getOriginalFilename());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(convFile);
            fos.write( file.getBytes() );
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return convFile;
    }

    public static File convertMultipartToFileNew(MultipartFile file)  {
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
}


