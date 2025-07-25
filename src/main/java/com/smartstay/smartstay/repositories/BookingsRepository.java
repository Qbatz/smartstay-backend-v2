package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BookingsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {
}
