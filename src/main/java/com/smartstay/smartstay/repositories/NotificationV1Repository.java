package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AdminNotifications;
import com.smartstay.smartstay.dao.NotificationsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationV1Repository extends JpaRepository<AdminNotifications, Long> {
    List<AdminNotifications> findByHostelId(String hostelId);

    @Query("""
            SELECT an FROM AdminNotifications an WHERE an.hostelId=:hostelId AND an.isRead=false
            """)
    List<AdminNotifications> findUnReadNotifications(@Param("hostelId") String hostelId);
}
