package com.smartstay.smartstay.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AWSConfig {

    public static AmazonS3 setupS3Client(String accessKey, String secretKey) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);


        AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTH_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        return client;
    }
}
