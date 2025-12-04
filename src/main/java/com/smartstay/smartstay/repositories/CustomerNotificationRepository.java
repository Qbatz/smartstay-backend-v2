package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerNotificationRepository extends JpaRepository<CustomerNotifications, Long> {
}
