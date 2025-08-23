package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.AddAdminUsersMapper;
import com.smartstay.smartstay.Wrappers.AdminDataMapper;
import com.smartstay.smartstay.Wrappers.ProfileUplodWrapper;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.Admin.UsersData;
import com.smartstay.smartstay.payloads.*;
import com.smartstay.smartstay.payloads.account.*;
import com.smartstay.smartstay.payloads.user.ResetPasswordRequest;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.responses.LoginUsersDetails;
import com.smartstay.smartstay.responses.OtpRequired;
import com.smartstay.smartstay.responses.account.AdminUserResponse;
import com.smartstay.smartstay.responses.user.OtpResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OTPService otpService;
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    RolesPermissionServie rolesPermissionService;


    @Autowired
    JWTService jwtService;

    @Autowired
    UploadFileToS3 uploadToS3;

    @Value("ENVIRONMENT")
    private String environment;

    @Autowired
    private com.smartstay.smartstay.config.Authentication authentication;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private RolesService rolesService;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private final RestTemplate restTemplate;

    public UsersService() {
        this.restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public ResponseEntity<AdminUserResponse> createAccount(CreateAccount createAccount) {

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
                users.setParentId(configUUID());
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

                return new ResponseEntity<>(new AdminUserResponse("", "", "Created successfully"), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(new AdminUserResponse("", "", "Password and confirm password is not matching"), HttpStatus.BAD_REQUEST);
            }
        } else {
            String mobileStatus = "";
            String emailStatus = "";

            if (userRepository.existsByEmailId(createAccount.mailId())) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }

            if (userRepository.existsByMobileNo(createAccount.mobile())) {
                mobileStatus = Utils.MOBILE_NO_EXISTS;
            }

            if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                return new ResponseEntity<>(
                        new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                        HttpStatus.BAD_REQUEST
                );
            }else {
                return new ResponseEntity<>(
                        new AdminUserResponse("", "", "Failed"),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

    }

    public ResponseEntity<Object> login(Login login) {
        Users users = userRepository.findUserByEmailId(login.emailId());
        if (users != null) {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(users.getUserId(), login.password()));

            if (authentication.isAuthenticated()) {
                if (users.isTwoStepVerificationStatus()) {
                    int otp = Utils.generateOtp();
                    String otpMessage = "Dear user, your SmartStay Login OTP is " + otp + ". Use this OTP to verify your login. Do not share it with anyone. - SmartStay";
                    otpService.insertOTP(users, otp);
                    if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL)) {
                        otpService.sendOtp(users.getMobileNo(), otpMessage);
                    }

                    OtpRequired otpRequired = new OtpRequired(true, users.getUserId());
                    return new ResponseEntity<>(otpRequired, HttpStatus.OK);
                }

                HashMap<String, Object> claims = new HashMap<>();
                claims.put("userId", users.getUserId());
                claims.put("role", rolesService.findById(users.getRoleId()));
                return new ResponseEntity<>(jwtService.generateToken(authentication.getName(), claims), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    public ResponseEntity<Object> verifyOtp(VerifyOtpPayloads verifyOtp) {
        UserOtp users = otpService.verifyOtp(verifyOtp);
        if (users != null && users.getOtpValidity().after(new Date())) {
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUsers().getUserId());
            claims.put("role", rolesService.findById(users.getUsers().getRoleId()));
            return new ResponseEntity<>(jwtService.generateToken(users.getUsers().getEmailId(), claims), HttpStatus.OK);
        } else if (users != null && users.getOtpValidity().before(new Date())) {
            return new ResponseEntity<>("Otp expired.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Invalid Otp", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> getProfileInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isAuthenticated()) {
            LoginUsersDetails details1 = null;
            com.smartstay.smartstay.dto.LoginUsersDetails usersDetails = userRepository.getLoginUserDetails(authentication.getName());
            StringBuilder initials = new StringBuilder();
            initials.append(usersDetails.firstName().toUpperCase().charAt(0));
            if (usersDetails.lastName() != null && !usersDetails.lastName().equalsIgnoreCase("")) {
                initials.append(usersDetails.lastName().toUpperCase().charAt(0));
            }
            else {
                initials.append(usersDetails.firstName().toUpperCase().charAt(0));
            }
            if (usersDetails.roleId() == 1 || usersDetails.roleId() == 2) {
                details1 = new LoginUsersDetails(usersDetails.userId(),
                        usersDetails.firstName(),
                        usersDetails.lastName(),
                        usersDetails.mobileNo(),
                        usersDetails.mailId(),
                        usersDetails.roleId(),
                        "Admin",
                        usersDetails.countryId(),
                        usersDetails.email_authentication_status(),
                        usersDetails.sms_authentication_status(),
                        usersDetails.two_step_verification_status(),
                        usersDetails.countryName(),
                        usersDetails.currency(),
                        usersDetails.countryCode(), initials.toString());
            }
            else {
                details1 = new LoginUsersDetails(usersDetails.userId(),
                        usersDetails.firstName(),
                        usersDetails.lastName(),
                        usersDetails.mobileNo(),
                        usersDetails.mailId(),
                        usersDetails.roleId(),
                        usersDetails.roleName(),
                        usersDetails.countryId(),
                        usersDetails.email_authentication_status(),
                        usersDetails.sms_authentication_status(),
                        usersDetails.two_step_verification_status(),
                        usersDetails.countryName(),
                        usersDetails.currency(),
                        usersDetails.countryCode(), initials.toString());
            }
            return new ResponseEntity<>(details1, HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid user.", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> changePassword(Password password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Users user = userRepository.findUserByUserId(password.adminId());
            if (user == null) {
                return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            String encodedPassword = encoder.encode(password.password());
            user.setPassword(encodedPassword);
            userRepository.save(user);
            return new ResponseEntity<>(Utils.PASSWORD_CHANGED_SUCCESS, HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> requestPasswordReset(String email) {
        Users user = userRepository.findUserByEmailId(email);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_EMAIL, HttpStatus.NOT_FOUND);
        }
        int otp = Utils.generateOtp();
        otpService.insertOrUpdateOTP(user, otp);

        String otpMessage = "Dear user, your password reset OTP is " + otp
                + ". It is valid for 15 minutes.";
        if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL)) {
            otpService.sendOtp(user.getMobileNo(), otpMessage);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("message", Utils.OTP_SENT_SUCCESSFULLY);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> verifyOtpAndResetPassword(ResetPasswordRequest request) {
        Users user = userRepository.findUserByUserId(request.userId());
        if (user == null) {
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        UserOtp usersOtp = otpService.verifyOtp(request.userId(),request.otp());
        if (usersOtp == null) {
            return new ResponseEntity<>(Utils.INVALID_OTP, HttpStatus.OK);
        } else if (usersOtp.getOtpValidity().before(new Date())) {
            return new ResponseEntity<>(Utils.OTP_EXPIRED, HttpStatus.BAD_REQUEST);
        }
        String encodedPassword = encoder.encode(request.password());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return new ResponseEntity<>(Utils.PASSWORD_RESET_SUCCESS, HttpStatus.OK);
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
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
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

    public ResponseEntity<Object> updateProfileInformations(UpdateUserProfilePayloads updateProfile, MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userRepository.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        String profilePic = null;
        if (file != null && !file.isEmpty()) {
            profilePic = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file));
        }

        if (profilePic != null && !profilePic.equalsIgnoreCase("")) {
            user.setProfileUrl(profilePic);
        }

        Users usersForUpdate = new ProfileUplodWrapper(user).apply(updateProfile);

        userRepository.save(usersForUpdate);
        return new ResponseEntity<>("Updated Successfully", HttpStatus.OK);
    }

    public boolean checkUUID(String uuid) {
        if (userRepository.findUserByParentId(uuid) == null) {
            return false;
        }
        return true;
    }

    public String configUUID() {
        String uuid = Utils.generateRandomUUID();
        if (!checkUUID(uuid)) {
            return uuid;
        }
        return configUUID();
    }


    public ResponseEntity<?> listAllAdmins() {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            if (users != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_READ)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }
                List<UsersData> admins = userRepository.getAdminUserList(2, users.getParentId());
                if (admins.isEmpty()) {
                    return new ResponseEntity<>("No admins found", HttpStatus.NOT_FOUND);
                }

                List<com.smartstay.smartstay.responses.user.UsersData> listAdmins = admins.stream().map(itm -> new AdminDataMapper().apply(itm)).toList();
                return new ResponseEntity<>(listAdmins, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        }else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> listAllUsers() {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            if (users != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_READ)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }
                List<UsersData> usersList = userRepository.getUserList();
                if (usersList.isEmpty()) {
                    return new ResponseEntity<>("No users found", HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(usersList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        }else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> createAdmin(AddAdminPayload createAccount, MultipartFile file) {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            if (users != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_WRITE)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }
                String mobileStatus = "";
                String emailStatus = "";

                if (userRepository.existsByEmailId(createAccount.mailId())) {
                    emailStatus = Utils.EMAIL_ID_EXISTS;
                }

                if (userRepository.existsByMobileNo(createAccount.mobile())) {
                    mobileStatus = Utils.MOBILE_NO_EXISTS;
                }

                if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                    return new ResponseEntity<>(
                            new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                            HttpStatus.BAD_REQUEST
                    );
                }
                else {
                    Users adminUser  = new Users();
                    adminUser.setCreatedBy(users.getUserId());
                    adminUser.setParentId(users.getParentId());
                    adminUser.setPassword(encoder.encode(createAccount.password()));

                    String profilePic = null;
                    if (file != null && !file.isEmpty()) {
                        profilePic = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file));
                    }

                    if (profilePic != null && !profilePic.equalsIgnoreCase("")) {
                        adminUser.setProfileUrl(profilePic);
                    }

                    adminUser = new AddAdminUsersMapper(adminUser).apply(createAccount);
                    userRepository.save(adminUser);

                    Users user = userRepository.findUserByEmailId(createAccount.mailId());
                    if (user != null) {
                        userHostelService.addUserToExistingHostel(users.getParentId(), user.getUserId());
                    }


                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            }
            else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }

        }
        else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public Users findUserByUserId(String userId) {
        return userRepository.findUserByUserId(userId);
    }

    public List<Users> findAllByParentId(String parentId) {
        return userRepository.findAllByParentId(parentId);
    }

    public ResponseEntity<?> createAdminUser(AddAdminUser adminUser, String hostelId) {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            if (users != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_WRITE)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }

                String mobileStatus = "";
                String emailStatus = "";

                if (userRepository.existsByEmailId(adminUser.emailId())) {
                    emailStatus = Utils.EMAIL_ID_EXISTS;
                }

                if (userRepository.existsByMobileNo(adminUser.mobile())) {
                    mobileStatus = Utils.MOBILE_NO_EXISTS;
                }

                if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                    return new ResponseEntity<>(
                            new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                            HttpStatus.BAD_REQUEST
                    );
                }
                if (!rolesService.checkRoleId(adminUser.roleId())) {
                    return new ResponseEntity<>(Utils.INVALID_ROLE, HttpStatus.BAD_REQUEST);
                }
                else {
                    Users admin = new Users();
                    admin.setMobileNo(adminUser.mobile());
                    admin.setEmailId(adminUser.emailId());
                    admin.setPassword(encoder.encode(adminUser.password()));
                    admin.setParentId(users.getParentId());
                    admin.setDescription(adminUser.description());
                    admin.setFirstName(adminUser.name());
                    admin.setRoleId(adminUser.roleId());
                    admin.setCreatedBy(users.getUserId());
                    admin.setCreatedAt(new Date());
                    admin.setCountry(1L);
                    admin.setActive(true);
                    admin.setDeleted(false);

                    userRepository.save(admin);

                    Users user = userRepository.findUserByEmailId(adminUser.emailId());
                    if (user != null) {
                        userHostelService.mapUserHostel(user.getUserId(), user.getParentId(), hostelId);
                    }

                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            }
            else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        }
        else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> updateTwoStepVerification(UpdateVerificationStatus verificationStatus) {
        if (authentication.isAuthenticated()) {
            Users user = userRepository.findById(authentication.getName()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
            user.setTwoStepVerificationStatus(verificationStatus.isStatus());
            userRepository.save(user);
            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public List<Users> findActiveUsersByRole(int roleId) {
        return userRepository.findByRoleIdAndIsActiveTrueAndIsDeletedFalse(roleId);
    }
}