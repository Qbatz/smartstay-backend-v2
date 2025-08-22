package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.VerifyOtpPayloads;
import com.smartstay.smartstay.repositories.UserOtpRepository;
import com.smartstay.smartstay.util.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Service
public class OTPService {

    @Autowired
    UserOtpRepository userOtpRepository;

    @Value("${SMS_API_KEY}")
    String SMS_API_KEY;
    @Value("${SMS_SENDER_ID}")
    String SMS_SENDER_ID;
    @Value("${SMS_CHANNEL}")
    String SMS_CHANNEL;

    @Value("${SMS_DCS}")
    String SMS_DCS;

    public void insertOTP(Users users) {
        Calendar calendar = Calendar.getInstance();
        UserOtp userOtp = new UserOtp();
        userOtp.setVerified(false);
        userOtp.setOtp(Utils.generateOtp());
        userOtp.setCreatedAt(new Date());
        userOtp.setUsers(users);

        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 15);
        userOtp.setOtpValidity(calendar.getTime());
        userOtpRepository.save(userOtp);
    }

    public void insertOTP(Users users, int otp) {
        UserOtp userOtp = new UserOtp();
        userOtp.setVerified(false);
        userOtp.setOtp(otp);
        userOtp.setUsers(users);
        userOtp.setCreatedAt(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 15);
        userOtp.setOtpValidity(calendar.getTime());

        userOtpRepository.save(userOtp);
    }

    public void insertOrUpdateOTP(Users users, int otp) {
        UserOtp userOtp = userOtpRepository.findByUsers(users).orElse(new UserOtp());
        userOtp.setVerified(false);
        userOtp.setOtp(otp);
        userOtp.setUsers(users);
        userOtp.setCreatedAt(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 15);
        userOtp.setOtpValidity(calendar.getTime());

        userOtpRepository.save(userOtp);
    }

    public void sendOtp(String mobileNo,String message) {

        try {
            String requestUrl = "https://www.smsgatewayhub.com/api/mt/SendSMS?" +
                    "APIKey=" + URLEncoder.encode(SMS_API_KEY, StandardCharsets.UTF_8) +
                    "&senderid=" + URLEncoder.encode(SMS_SENDER_ID, StandardCharsets.UTF_8) +
                    "&channel=" + URLEncoder.encode(SMS_CHANNEL, StandardCharsets.UTF_8) +
                    "&DCS=" + URLEncoder.encode(SMS_DCS, StandardCharsets.UTF_8) +
                    "&flashsms=" + URLEncoder.encode("0", StandardCharsets.UTF_8) +
                    "&number=" + URLEncoder.encode(mobileNo, StandardCharsets.UTF_8) +
                    "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                    "&route=" + URLEncoder.encode("", StandardCharsets.UTF_8);

            URL url = URI.create(requestUrl).toURL();
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();

            uc.disconnect();
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public UserOtp verifyOtp(VerifyOtpPayloads verifyOtp) {
        UserOtp userOtp = userOtpRepository.findByUsers_UserIdAndOtpAndIsVerifiedFalse(verifyOtp.userId(), verifyOtp.otp()).orElse(null);
        if (userOtp != null && userOtp.getOtpValidity().after(new Date())) {
            userOtp.setVerified(true);
            userOtpRepository.save(userOtp);
        }
        return userOtp;
    }

    public UserOtp verifyOtp(String userId, Integer otp) {
        UserOtp userOtp = userOtpRepository.findByUsers_UserIdAndOtpAndIsVerifiedFalse(userId, otp).orElse(null);
        if (userOtp != null && userOtp.getOtpValidity().after(new Date())) {
            userOtp.setVerified(true);
            userOtpRepository.save(userOtp);
        }
        return userOtp;
    }
}
