package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UsersConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersConfigRepository extends JpaRepository<UsersConfig, Long> {
    Optional<UsersConfig> findByUser_UserIdAndPin(String userId, Integer pin);
}
