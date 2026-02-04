package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.AddAdminUsersMapper;
import com.smartstay.smartstay.Wrappers.AdminDataMapper;
import com.smartstay.smartstay.Wrappers.ProfileUplodWrapper;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.Admin.UsersData;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.AppSource;
import com.smartstay.smartstay.events.AddAdminEvents;
import com.smartstay.smartstay.events.AddUserEvents;
import com.smartstay.smartstay.payloads.*;
import com.smartstay.smartstay.payloads.account.*;
import com.smartstay.smartstay.payloads.profile.Logout;
import com.smartstay.smartstay.payloads.profile.UpdateFCMToken;
import com.smartstay.smartstay.payloads.user.ResetPasswordRequest;
import com.smartstay.smartstay.payloads.user.SetupPin;
import com.smartstay.smartstay.payloads.user.VerifyPin;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.repositories.UsersConfigRepository;
import com.smartstay.smartstay.responses.LoginUsersDetails;
import com.smartstay.smartstay.responses.OtpRequired;
import com.smartstay.smartstay.responses.account.AdminUserResponse;
import com.smartstay.smartstay.responses.user.MobileLogin;
import com.smartstay.smartstay.responses.user.OtpResponse;
import com.smartstay.smartstay.util.Utils;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    JWTService jwtService;

    @Autowired
    UploadFileToS3 uploadToS3;

    @Value("${ENVIRONMENT}")
    private String environment;

    @Autowired
    private com.smartstay.smartstay.config.Authentication authentication;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UsersConfigRepository usersConfigRepository;
    @Autowired
    private MyUserDetailService myUserDetailService;
    @Autowired
    private LoginHistoryService loginHistoryService;
    @Autowired
    private UserActivitiesService userActivitiesService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private final RestTemplate restTemplate;

    private BankingService bankingService;

    public UsersService() {
        this.restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    @Autowired
    public void setBankingService(@Lazy BankingService bankingService) {
        this.bankingService = bankingService;
    }

    public ResponseEntity<AdminUserResponse> createAccount(CreateAccount createAccount) {

        String mobileStatus = "";
        String emailStatus = "";

        if (createAccount.mailId() != null && !createAccount.mailId().isEmpty()
                && userRepository.existsByEmailIdAndIsDeletedFalse(createAccount.mailId())) {
            emailStatus = Utils.EMAIL_ID_EXISTS;
        }

        if (createAccount.mobile() != null && !createAccount.mobile().isEmpty()
                && userRepository.existsByMobileNoAndIsDeletedFalse(createAccount.mobile())) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
        }

        if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
            return new ResponseEntity<>(
                    new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!createAccount.password().equalsIgnoreCase(createAccount.confirmPassword())) {
            return new ResponseEntity<>(
                    new AdminUserResponse("", "", "Password and confirm password is not matching"),
                    HttpStatus.BAD_REQUEST);
        }

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
        users.setActive(true);
        users.setDeleted(false);
        users.setCreatedAt(Calendar.getInstance().getTime());
        users.setLastUpdate(Calendar.getInstance().getTime());

        Address address = new Address();
        address.setUser(users);
        users.setAddress(address);

        Users usr = userRepository.save(users);
        userActivitiesService.createUser(ActivitySource.PROFILE.name(), ActivitySourceType.CREATE.name(), usr);

        return new ResponseEntity<>(new AdminUserResponse("", "", "Created successfully"), HttpStatus.CREATED);
    }

    public ResponseEntity<?> mobileLogin(Login login) {
        if (login == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(login.emailId()) && !Utils.checkNullOrEmpty(login.password())) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }
        Users users = userRepository.findByEmailIdAndIsDeletedFalse(login.emailId());
        if (users == null) {
            return new ResponseEntity<>(Utils.INVALID_USER_NAME_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(users.getUserId(), login.password()));
        if (authentication.isAuthenticated()) {
            boolean isPinSetup = false;
            UsersConfig config = users.getConfig();
            if (config == null) {
                isPinSetup = false;
            } else {
                if (config.getPin() == null) {
                    isPinSetup = false;
                } else {
                    isPinSetup = true;
                }
            }
            MobileLogin mobileLogin = new MobileLogin(users.getUserId(), isPinSetup);

            return new ResponseEntity<>(mobileLogin, HttpStatus.OK);
        }

        else {
            return new ResponseEntity<>(Utils.INVALID_USER_NAME_PASSWORD, HttpStatus.FORBIDDEN);
        }
    }

    public ResponseEntity<Object> login(Login login) {
        Users users = userRepository.findByEmailIdAndIsDeletedFalse(login.emailId());
        if (users != null) {
            Authentication authentication = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(users.getUserId(), login.password()));

            if (authentication.isAuthenticated()) {
                if (users.isTwoStepVerificationStatus()) {
                    int otp = Utils.generateOtp();
                    String otpMessage = "Dear user, your SmartStay Login OTP is " + otp
                            + ". Use this OTP to verify your login. Do not share it with anyone. - SmartStay";
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
                loginHistoryService.login(users.getUserId(), users.getParentId(), AppSource.WEB.name(), "");
                userActivitiesService.addLoginLog(null, null, ActivitySource.PROFILE.name(),
                        ActivitySourceType.LOGGED_IN.name(), users.getUserId(), users);
                return new ResponseEntity<>(jwtService.generateToken(authentication.getName(), claims), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    public ResponseEntity<Object> verifyOtp(VerifyOtpPayloads verifyOtp) {
        UserOtp users = otpService.verifyOtp(verifyOtp);
        if (users != null && users.getOtpValidity().after(new Date())) {
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUsers().getUserId());
            claims.put("role", rolesService.findById(users.getUsers().getRoleId()));
            loginHistoryService.login(users.getUsers().getUserId(), users.getUsers().getParentId(),
                    AppSource.WEB.name(), "");

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
            com.smartstay.smartstay.dto.LoginUsersDetails usersDetails = userRepository
                    .getLoginUserDetails(authentication.getName());
            StringBuilder initials = new StringBuilder();
            initials.append(usersDetails.firstName().toUpperCase().charAt(0));
            if (usersDetails.lastName() != null && !usersDetails.lastName().equalsIgnoreCase("")) {
                initials.append(usersDetails.lastName().toUpperCase().charAt(0));
            } else {
                if (usersDetails.firstName().length() > 1) {
                    initials.append(usersDetails.firstName().toUpperCase().charAt(1));
                } else {
                    initials.append(usersDetails.firstName().toUpperCase().charAt(0));
                }

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
            } else {
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
        Users user = userRepository.findByEmailIdAndIsDeletedFalse(email);
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
        UserOtp usersOtp = otpService.verifyOtp(request.userId(), request.otp());
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

    public ResponseEntity<Object> updateProfileInformations(UpdateUserProfilePayloads updateProfile,
            MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userRepository.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
                List<UsersData> admins = userRepository.getAdminUserList(2, users.getParentId(),
                        authentication.getName());
                if (admins.isEmpty()) {
                    return new ResponseEntity<>("No admins found", HttpStatus.NO_CONTENT);
                }

                List<com.smartstay.smartstay.responses.user.UsersData> listAdmins = admins.stream()
                        .map(itm -> new AdminDataMapper().apply(itm)).toList();
                return new ResponseEntity<>(listAdmins, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> listAllUsers(String hostelId) {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            if (users != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_READ)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }
                if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
                    return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
                }
                List<UsersData> usersList = userRepository.getUserList(hostelId, users.getUserId());
                if (usersList.isEmpty()) {
                    return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(usersList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
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

                if (createAccount.mailId() != null && !createAccount.mailId().isEmpty()
                        && userRepository.existsByEmailIdAndIsDeletedFalse(createAccount.mailId())) {
                    emailStatus = Utils.EMAIL_ID_EXISTS;
                }

                if (createAccount.mobile() != null && !createAccount.mobile().isEmpty()
                        && userRepository.existsByMobileNoAndIsDeletedFalse(createAccount.mobile())) {
                    mobileStatus = Utils.MOBILE_NO_EXISTS;
                }

                if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                    return new ResponseEntity<>(
                            new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                            HttpStatus.BAD_REQUEST);
                } else {
                    Users adminUser = new Users();
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
                    Users createdAccount = userRepository.save(adminUser);

                    if (createdAccount != null) {
                        userHostelService.addUserToExistingHostel(users.getParentId(), createdAccount.getUserId(),
                                users.getUserId());
                    }

                    StringBuilder fullName = new StringBuilder();
                    if (createdAccount.getFirstName() != null) {
                        fullName.append(createdAccount.getFirstName());
                    }
                    if (createdAccount.getLastName() != null && !createdAccount.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(createdAccount.getLastName());
                    }
                    eventPublisher.publishEvent(new AddAdminEvents(this, users.getParentId(),
                            createdAccount.getUserId(), fullName.toString()));
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }

        } else {
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

                if (adminUser.emailId() != null && !adminUser.emailId().isEmpty()
                        && userRepository.existsByEmailIdAndIsDeletedFalse(adminUser.emailId())) {
                    emailStatus = Utils.EMAIL_ID_EXISTS;
                }

                if (adminUser.mobile() != null && !adminUser.mobile().isEmpty()
                        && userRepository.existsByMobileNoAndIsDeletedFalse(adminUser.mobile())) {
                    mobileStatus = Utils.MOBILE_NO_EXISTS;
                }

                if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                    return new ResponseEntity<>(
                            new AdminUserResponse(mobileStatus, emailStatus, "Validation failed"),
                            HttpStatus.BAD_REQUEST);
                }
                if (!rolesService.checkRoleId(adminUser.roleId())) {
                    return new ResponseEntity<>(Utils.INVALID_ROLE, HttpStatus.BAD_REQUEST);
                } else {
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

                    Users user = userRepository.save(admin);

                    if (user != null) {
                        userHostelService.mapUserHostel(user.getUserId(), user.getParentId(), hostelId);
                    }

                    StringBuilder fullName = new StringBuilder();
                    if (user.getFirstName() != null) {
                        fullName.append(user.getFirstName());
                    }
                    if (user.getLastName() != null && !user.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(user.getLastName());
                    }
                    eventPublisher.publishEvent(new AddUserEvents(this, user.getUserId(), hostelId, fullName.toString(),
                            user.getParentId()));
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
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
            user.setLastUpdate(new Date());
            userRepository.save(user);
            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> deleteUser(String hostelId, String userId) {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            Users inputUser = userRepository.findUserByUserIdAndParentId(userId, users.getParentId());

            if (inputUser != null) {
                if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_WRITE)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }

                if (inputUser.getRoleId() == 1) {
                    return new ResponseEntity<>("Admin user cannot be deleted", HttpStatus.FORBIDDEN);
                }

                if (bankingService.deleteBankForUser(inputUser.getUserId(), hostelId)) {
                    inputUser.setDeleted(true);
                    inputUser.setActive(false);
                    userRepository.save(inputUser);
                }

                return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);

            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> deleteAdminUser(String userId) {
        if (authentication.isAuthenticated()) {
            Users users = userRepository.findUserByUserId(authentication.getName());
            Users inputUser = userRepository.findUserByUserIdAndParentId(userId, users.getParentId());

            if (inputUser != null) {
                if (!rolesService.checkPermission(inputUser.getRoleId(), Utils.MODULE_ID_USER,
                        Utils.PERMISSION_WRITE)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }

                if (inputUser.getRoleId() != 2) {
                    return new ResponseEntity<>("This user cannot be deleted", HttpStatus.FORBIDDEN);
                }

                inputUser.setDeleted(true);
                inputUser.setActive(false);
                userRepository.save(inputUser);
                return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);

            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public List<Users> findActiveUsersByRole(int roleId) {
        return userRepository.findByRoleIdAndIsActiveTrueAndIsDeletedFalse(roleId);
    }

    public Users existsByUserIdAndIsActiveTrueAndIsDeletedFalseAndParentId(String userId, String parentId) {
        return userRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalseAndParentId(userId, parentId);
    }

    public ResponseEntity<?> updateAdminProfile(String adminId, EditAdmin payloads, MultipartFile profilePic) {
        if (payloads != null) {
            if (authentication.isAuthenticated()) {
                Users user = userRepository.findById(authentication.getName()).orElse(null);
                if (user == null) {
                    return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
                }

                if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_UPDATE)) {
                    return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
                }
                if (payloads == null) {
                    return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
                }
                if (payloads.mobile() != null) {
                    int isMobileExsists = userRepository.getUsersCountByMobile(adminId, payloads.mobile());
                    if (isMobileExsists > 0) {
                        return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                }

                Users adminUser = userRepository.findUserByUserId(adminId);

                if (adminUser == null) {
                    return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
                }

                Address add = adminUser.getAddress();
                if (add == null) {
                    add = new Address();
                }
                if (payloads.mailId() != null && !payloads.mailId().equalsIgnoreCase("")) {
                    if (userRepository.getUsersCountByEmail(adminId, adminUser.getEmailId()) > 0) {
                        return new ResponseEntity<>(Utils.EMAIL_ID_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                    adminUser.setEmailId(payloads.mailId());
                }

                String pic = null;
                if (profilePic != null && !profilePic.isEmpty()) {
                    pic = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic));
                }

                if (pic != null && !pic.equalsIgnoreCase("")) {
                    adminUser.setProfileUrl(pic);
                }

                if (payloads.firstName() != null && !payloads.firstName().equalsIgnoreCase("")) {
                    adminUser.setFirstName(payloads.firstName());
                }

                if (payloads.lastName() != null && !payloads.lastName().equalsIgnoreCase("")) {
                    adminUser.setLastName(payloads.lastName());
                } else {
                    if (adminUser.getLastName() != null) {
                        adminUser.setLastName(null);
                    }
                }
                if (payloads.mobile() != null && !payloads.mobile().equalsIgnoreCase("")) {
                    adminUser.setMobileNo(payloads.mobile());
                    // high priority, check mobile exist
                }

                if (payloads.houseNo() != null && !payloads.houseNo().equalsIgnoreCase("")) {
                    add.setHouseNo(payloads.houseNo());
                } else {
                    if (add.getHouseNo() != null) {
                        add.setHouseNo(null);
                    }
                }
                if (payloads.street() != null && !payloads.street().equalsIgnoreCase("")) {
                    add.setStreet(payloads.street());
                } else if (add.getStreet() != null) {
                    add.setStreet(null);
                }
                if (payloads.landmark() != null && !payloads.landmark().equalsIgnoreCase("")) {
                    add.setLandMark(payloads.landmark());
                } else {
                    if (add.getLandMark() != null) {
                        add.setLandMark(null);
                    }
                }
                if (payloads.city() != null && !payloads.city().equalsIgnoreCase("")) {
                    add.setCity(payloads.city());
                }
                if (payloads.pincode() != null) {
                    add.setPincode(payloads.pincode());
                }
                if (payloads.state() != null && !payloads.state().equalsIgnoreCase("")) {
                    add.setState(payloads.state());
                }
                adminUser.setAddress(add);
                adminUser.setLastUpdate(new Date());

                userRepository.save(adminUser);

                return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<?> updateUsersProfile(String hostelId, String userId, EditUsers payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();

        Users users = userRepository.findUserByUserId(loginId);
        if (payloads == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_USER, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Users userToUpdate = userRepository.findUserByUserId(userId);
        if (userToUpdate == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(payloads.emailId())) {
            if (userRepository.getUsersCountByEmail(userToUpdate.getUserId(), payloads.emailId()) > 0) {
                return new ResponseEntity<>(Utils.EMAIL_ID_EXISTS, HttpStatus.BAD_REQUEST);
            }
            userToUpdate.setEmailId(payloads.emailId());
        }
        if (Utils.checkNullOrEmpty(payloads.role())) {
            if (!rolesService.checkRoleIdExistForHostel(payloads.role(), hostelId)) {
                return new ResponseEntity<>(Utils.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
            userToUpdate.setRoleId(payloads.role());
        }

        if (Utils.checkNullOrEmpty(payloads.name())) {
            String[] names = payloads.name().split(" ");
            userToUpdate.setFirstName(names[0]);
            if (names.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < names.length; i++) {
                    builder.append(names[i]);
                }
                userToUpdate.setLastName(builder.toString());
            }
        }
        if (Utils.checkNullOrEmpty(payloads.mobile())) {
            userToUpdate.setMobileNo(payloads.mobile());
        }
        if (Utils.checkNullOrEmpty(payloads.description())) {
            userToUpdate.setDescription(payloads.description());
        }
        userToUpdate.setLastUpdate(new Date());

        userRepository.save(userToUpdate);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }


    public List<Users> findByListOfUserIds(List<String> assignes) {
        return userRepository.findAllById(assignes);
    }

    public List<String> findAdminUsers(List<String> userIds) {
        return userRepository.findAdminUsersBasedOnHostelIdFromListUsers(userIds)
                .stream()
                .map(Users::getUserId)
                .toList();
    }

    public ResponseEntity<?> setupPin(String userId, SetupPin pin) {
        if (pin == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (pin.pin() == null) {
            return new ResponseEntity<>(Utils.PIN_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Users users = userRepository.findUserByUserId(userId);
        if (users == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.BAD_REQUEST);
        }
        UsersConfig config = users.getConfig();
        if (config != null) {
            if (config.getPin() == null || config.getPin() == 0) {
                config.setPin(pin.pin());
                config.setUser(users);
                users.setConfig(config);
                userRepository.save(users);
                return generateToken(config);
            } else {
                return new ResponseEntity<>(Utils.PIN_ALREADY_SETUP, HttpStatus.BAD_REQUEST);
            }
        } else {
            config = new UsersConfig();
            config.setUser(users);
            config.setPin(pin.pin());
            users.setConfig(config);
            userRepository.save(users);
            return generateToken(config);
        }
    }

    public ResponseEntity<?> verifyPin(String userId, VerifyPin pin) {
        if (userId == null) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (pin == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (pin.pin() == null) {
            return new ResponseEntity<>(Utils.PIN_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        UsersConfig usersConfig = usersConfigRepository.findByUser_UserIdAndPin(userId, pin.pin()).orElse(null);
        if (usersConfig == null) {
            return new ResponseEntity<>(Utils.INVALID_PIN, HttpStatus.BAD_REQUEST);
        }

        if (usersConfig.getUser() == null) {
            return new ResponseEntity<>(Utils.INVALID_PIN, HttpStatus.BAD_REQUEST);
        }

        return generateToken(usersConfig);
    }

    public ResponseEntity<?> generateToken(UsersConfig usersConfig) {
        Users users = usersConfig.getUser();
        UserDetails userDetails = myUserDetailService.loadUserByUsername(users.getUserId());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_PIN, HttpStatus.BAD_REQUEST);
        }

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", users.getUserId());
        claims.put("role", rolesService.findById(users.getRoleId()));

        Long validity = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 15);
        loginHistoryService.login(users.getUserId(), users.getParentId(), AppSource.MOBILE.name(), "Android");
        String token = jwtService.generateMobileToken(authentication.getName(), claims, validity);
        com.smartstay.smartstay.responses.user.VerifyPin vPin = new com.smartstay.smartstay.responses.user.VerifyPin(
                validity, token);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    public ResponseEntity<?> addFCMToken(UpdateFCMToken updateFCMToken) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userRepository.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (updateFCMToken == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (updateFCMToken.token() == null) {
            return new ResponseEntity<>(Utils.TOKEN_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        UsersConfig usersConfig = users.getConfig();
        if (usersConfig != null) {
            if (updateFCMToken.source().equalsIgnoreCase("WEB")) {
                usersConfig.setFcmWebToken(updateFCMToken.token());
                usersConfigRepository.save(usersConfig);
            } else {
                usersConfig.setFcmToken(updateFCMToken.token());
                usersConfigRepository.save(usersConfig);
            }

            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
        } else {
            usersConfig = new UsersConfig();
            usersConfig.setUser(users);
            if (updateFCMToken.source().equalsIgnoreCase("WEB")) {
                usersConfig.setFcmWebToken(updateFCMToken.token());
            } else {
                usersConfig.setFcmToken(updateFCMToken.token());
            }

            users.setConfig(usersConfig);

            userRepository.save(users);

            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
        }

    }

    public ResponseEntity<?> logout(Logout logout) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (logout == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        UsersConfig usersConfig = users.getConfig();
        if (usersConfig != null) {
            if (logout.source().equalsIgnoreCase("WEB")) {
                usersConfig.setFcmWebToken(null);
            } else if (logout.source().equalsIgnoreCase("MOBILE")) {
                usersConfig.setFcmToken(null);
            }

            usersConfigRepository.save(usersConfig);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void addUserLog(String hostelId, String sourceId, ActivitySource activitySource,
            ActivitySourceType activitySourceType, Users users) {
        userActivitiesService.addLoginLog(hostelId, null, activitySource.name(), activitySourceType.name(), sourceId,
                users);
    }

    public void addUserLog(String hostelId, String sourceId, ActivitySource activitySource,
            ActivitySourceType activitySourceType, Users user, List<String> customerIds) {
        userActivitiesService.addLoginLog(hostelId, null, activitySource.name(), activitySourceType.name(), sourceId,
                user, customerIds);
    }

    public void finalSettlementGenetated(String hostelId, String invoiceId, ActivitySource activitySource,
            ActivitySourceType activitySourceType, String customerId, Users users) {
        List<String> customerIds = new ArrayList<>();
        customerIds.add(customerId);
        userActivitiesService.addLoginLog(hostelId, null, activitySource.name(), activitySourceType.name(), invoiceId,
                users, customerIds);
    }

    public List<Users> findAllUsersByHostelId(String hostelId) {
        List<String> userIds = userHostelService.listAllUsersFromHostelId(hostelId);
        return userRepository.findAllByUserIdIn(userIds);
    }

    public List<Users> findAllUsersFromUserId(List<String> userIds) {
        return userRepository.findAllByUserIdIn(userIds);
    }
}
