package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.CreateAccount;
import com.smartstay.smartstay.payloads.Login;
import com.smartstay.smartstay.payloads.VerifyOtpPayloads;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.UserOtpRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.responses.OtpRequired;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.HashMap;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OTPService otpService;
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    JWTService jwtService;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);

    public ResponseEntity<com.smartstay.smartstay.responses.CreateAccount> createAccount(CreateAccount createAccount) {

        Users usr = userRepository.findUserByEmailId(createAccount.mailId());
        if (usr == null) {
            if (createAccount.password().equalsIgnoreCase(createAccount.confirmPassword())) {
                Users users = new Users();
                users.setFirstName(createAccount.firstName());
                users.setLastName(createAccount.lastName());
                users.setMobileNo(createAccount.mobile());
                users.setPassword(encoder.encode(createAccount.password()));
                users.setEmailId(createAccount.mailId());
                users.setRoleId(1);
                users.setEmailAuthenticationStatus(false);
                users.setSmsAuthenticationStatus(false);
                users.setEmailAuthenticationStatus(false);

                userRepository.save(users);
                Users userData = userRepository.findUserByEmailId(createAccount.mailId());

                otpService.insertOTP(userData);

                com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Created Successfully");

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else {
                com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Password and confirm password is not matching");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        else {
            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Email Id already registered");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<Object> login(Login login) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(login.emailId(), login.password()));

        if (authentication.isAuthenticated()) {
            Users users = userRepository.findByEmailId(login.emailId());

            if (users.isTwoStepVerificationStatus()) {
                int otp = Utils.generateOtp();
                String otpMessage = "Dear user, your SmartStay Login OTP is " + otp + ". Use this OTP to verify your login. Do not share it with anyone. - SmartStay";
                otpService.insertOTP(users, otp);
                otpService.sendOtp(users.getMobileNo(), otpMessage);

                OtpRequired otpRequired = new OtpRequired(true, users.getUserId());
                return new ResponseEntity<>(otpRequired, HttpStatus.OK);
            }

            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUserId());
            claims.put("role", rolesRepository.findById(users.getRoleId()).orElse(new RolesV1()).getRoleName());
            return new ResponseEntity<>(jwtService.generateToken(authentication.getName(), claims), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    public ResponseEntity<Object> verifyOtp(VerifyOtpPayloads verifyOtp) {
        UserOtp users = otpService.verifyOtp(verifyOtp);
        if (users != null) {
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUsers().getUserId());
            claims.put("role", rolesRepository.findById(users.getUsers().getRoleId()).orElse(new RolesV1()).getRoleName());
            return new ResponseEntity<>(jwtService.generateToken(users.getUsers().getEmailId(), claims), HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid Otp", HttpStatus.BAD_REQUEST);
    }
}
