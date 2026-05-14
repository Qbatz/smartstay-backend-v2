package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DraftsRepository extends JpaRepository<Draft, String> {

    List<Draft> findByCustomerIdIn(Collection<String> customerIds);
}
