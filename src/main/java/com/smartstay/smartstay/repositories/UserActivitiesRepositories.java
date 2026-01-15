package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UserActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivitiesRepositories extends JpaRepository<UserActivities, Long> {
}
