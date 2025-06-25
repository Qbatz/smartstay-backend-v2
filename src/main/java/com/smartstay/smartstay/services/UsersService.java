package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.ProfileUplodWrapper;
import com.smartstay.smartstay.dao.Address;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.*;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.responses.LoginUsersDetails;
import com.smartstay.smartstay.responses.OtpRequired;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Calendar;
import java.util.Date;
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

    @Value("ENVIRONMENT")
    private String environment;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

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
                users.setCountry(1L);
                users.setEmailAuthenticationStatus(false);
                users.setSmsAuthenticationStatus(false);
                users.setEmailAuthenticationStatus(false);
                users.setActive(true);
                users.setDeleted(false);
                users.setCreatedAt(Calendar.getInstance().getTime());
                users.setLastUpdate(Calendar.getInstance().getTime());

                Address address = new Address();
                address.setUser(users);
                users.setAddress(address);

                userRepository.save(users);
//                Users userData = userRepository.findUserByEmailId(createAccount.mailId());
//
//                otpService.insertOTP(userData);

                com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Created Successfully");

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Password and confirm password is not matching");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Email Id already registered");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<Object> login(Login login) {
        Users users = userRepository.findUserByEmailId(login.emailId());

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(users.getUserId(), login.password()));

        if (authentication.isAuthenticated()) {
            if (users.isTwoStepVerificationStatus()) {
                int otp = Utils.generateOtp();
                String otpMessage = "Dear user, your SmartStay Login OTP is " + otp + ". Use this OTP to verify your login. Do not share it with anyone. - SmartStay";
                otpService.insertOTP(users, otp);
                if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL)) {
                    otpService.sendOtp(users.getMobileNo(), otpMessage);
                } else {
                    System.out.println("ignoring....");
                }

                OtpRequired otpRequired = new OtpRequired(true, users.getUserId());
                return new ResponseEntity<>(otpRequired, HttpStatus.OK);
            }

            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUserId());
            claims.put("role", rolesRepository.findById(users.getRoleId()).orElse(new RolesV1()).getRoleName());
            return new ResponseEntity<>(jwtService.generateToken(authentication.getName(), claims), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    public ResponseEntity<Object> verifyOtp(VerifyOtpPayloads verifyOtp) {
        UserOtp users = otpService.verifyOtp(verifyOtp);
        if (users != null && users.getOtpValidity().after(new Date())) {
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUsers().getUserId());
            claims.put("role", rolesRepository.findById(users.getUsers().getRoleId()).orElse(new RolesV1()).getRoleName());
            return new ResponseEntity<>(jwtService.generateToken(users.getUsers().getEmailId(), claims), HttpStatus.OK);
        } else if (users != null && users.getOtpValidity().before(new Date())) {
            return new ResponseEntity<>("Otp expired.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Invalid Otp", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> getProfileInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isAuthenticated()) {
            LoginUsersDetails usersDetails = userRepository.getLoginUserDetails(authentication.getName());
            return new ResponseEntity<>(usersDetails, HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid user.", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> changePassword(Password password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {

            Users user = userRepository.findUserByUserId(authentication.getName());
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
            }

            String encodedPassword = encoder.encode(password.password());
            user.setPassword(encodedPassword);
            userRepository.save(user);
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid user.", HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<Object> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {

            Users user = userRepository.findUserByUserId(authentication.getName());
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
            }

            user.setDeleted(true);
            userRepository.save(user);
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid user.", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> verifyPassword(Password currentPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userRepository.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        boolean matches = encoder.matches(currentPassword.password(), user.getPassword());

        if (matches) {
            return new ResponseEntity<>("Password Matched!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> updateProfileInformations(UpdateUserProfilePayloads updateProfile) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userRepository.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Users usersForUpdate = new ProfileUplodWrapper(user).apply(updateProfile);

        userRepository.save(usersForUpdate);
        return new ResponseEntity<>("Updated Successfully", HttpStatus.OK);
    }
}
