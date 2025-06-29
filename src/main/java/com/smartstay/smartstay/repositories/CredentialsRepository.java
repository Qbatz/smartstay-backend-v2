package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, String> {
}
