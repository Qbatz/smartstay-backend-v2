package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpRepository extends JpaRepository<UserOtp, String> {

    Optional<UserOtp> findByUsers_UserIdAndOtp(String userId, Integer otp);


}
