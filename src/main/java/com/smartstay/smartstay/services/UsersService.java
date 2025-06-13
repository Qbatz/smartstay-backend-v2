package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.CreateAccount;
import com.smartstay.smartstay.payloads.Login;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    JWTService jwtService;

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

            Users userFOrOtp = userRepository.findByEmailId(createAccount.mailId());

            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Created Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
       else {
            com.smartstay.smartstay.responses.CreateAccount response = new com.smartstay.smartstay.responses.CreateAccount("Password and confirm password is not matching");
           return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> login(Login login) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(login.emailId(), login.password()));

        if (authentication.isAuthenticated()) {
            Users users = userRepository.findByEmailId(login.emailId());
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", users.getUserId());
            claims.put("role", rolesRepository.findById(users.getRoleId()).orElse(new RolesV1()).getRoleName());
            return new ResponseEntity<>(jwtService.generateToken(authentication.getName(), claims), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
