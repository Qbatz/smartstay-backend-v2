package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UserOtp;
import com.smartstay.smartstay.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpRepository extends JpaRepository<UserOtp, String> {

    Optional<UserOtp> findByUsers_UserIdAndOtpAndIsVerifiedFalse(String userId, Integer otp);

    Optional<UserOtp> findByUsers(Users users);


}
