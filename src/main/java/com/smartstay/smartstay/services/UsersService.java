package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.CreateAccount;
import com.smartstay.smartstay.repositories.UserOtpRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OTPService otpService;
    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);

    public ResponseEntity<com.smartstay.smartstay.responses.CreateAccount> createAccount(CreateAccount createAccount) {

        if (createAccount.password().equalsIgnoreCase(createAccount.confirmPassword())) {
            Users users = new Users();
            users.setFirstName(createAccount.firstName());
            users.setLastName(createAccount.lastName());
            users.setMobileNo(createAccount.mobile());
            users.setPassword(encoder.encode(createAccount.password()));
            users.setEmailId(createAccount.mailId());
            users.setRoleId(1);

            userRepository.save(users);
            Users userData = userRepository.findUserByEmailId(createAccount.mailId());

            otpService.insertOTP(userData.getUserId());

            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Created Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
       else {
            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Password and confirm password is not matching");
           return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
