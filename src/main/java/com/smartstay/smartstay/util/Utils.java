package com.smartstay.smartstay.util;

import java.security.SecureRandom;

public class Utils {

    public static final String ENVIRONMENT_LOCAL = "LOCAL";
    public static final String ENVIRONMENT_DEV = "DEV";
    public static final String ENVIRONMENT_QA = "QA";
    public static final String ENVIRONMENT_PROD = "PROD";

    public static int generateOtp() {
        return (int)(Math.random() * 900000) + 100000;
    }
}
