package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UserActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivitiesRepositories extends JpaRepository<UserActivities, Long> {
    @Query("""
            SELECT ua FROM UserActivities ua WHERE ua.source='EXPENSE'
            """)
    List<UserActivities> findAllExpenseActivities();
}
