package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    boolean existsByMobile(String mobileNo);

}
