package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AdminNotifications;
import com.smartstay.smartstay.dao.NotificationsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationV1Repository extends JpaRepository<AdminNotifications, Long> {
    List<AdminNotifications> findByHostelId(String hostelId);
}
