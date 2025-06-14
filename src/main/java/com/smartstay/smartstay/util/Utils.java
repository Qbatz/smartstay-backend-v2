package com.smartstay.smartstay.util;

import java.security.SecureRandom;

public class Utils {

    public static int generateOtp() {
        return (int)(Math.random() * 900000) + 100000;
    }
}
