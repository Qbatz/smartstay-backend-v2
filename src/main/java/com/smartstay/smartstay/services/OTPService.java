package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.repositories.UserOtpRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
public class OTPService {

    @Autowired
    UserOtpRepository userOtpRepository;

    public void insertOTP(String userId) {
        UserOtp userOtp = new UserOtp();
        userOtp.setUserId(userId);
        userOtp.setVerified(false);
        userOtp.setOtp(Utils.generateOtp());
        userOtp.setCreatedAt(new Date());

        userOtpRepository.save(userOtp);
    }
}
