package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, String> {

}
