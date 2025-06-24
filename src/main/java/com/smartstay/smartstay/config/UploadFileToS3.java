package com.smartstay.smartstay.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class UploadFileToS3 {

    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    public String uploadFileToS3(File file) {

        AmazonS3 s3 = AWSConfig.setupS3Client(accessKey, secretKey);
//
        PutObjectRequest request = new PutObjectRequest(bucketName, "general_user_profile/" + file.getName(), file);
        PutObjectResult result = s3.putObject(request);


        String fileName = s3.getUrl(bucketName, "general_user_profile/" + file.getName()).toString();
//
        if (file.exists()) {
            file.deleteOnExit();
        }

        return fileName;
    }
}
