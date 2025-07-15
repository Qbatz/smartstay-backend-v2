package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.UserHostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHostelRepository extends JpaRepository<UserHostel, Integer> {
    List<UserHostel> findByUserId(String userId);

    UserHostel findByUserIdAndHostelId(String userId, String hostelId);

    List<UserHostel> findAllByHostelId(String hostelId);

    List<UserHostel> findAllByParentId(String parentId);

    @Query(value = "select * from user_hostel where parent_id=:parentId group by user_id", nativeQuery = true)
    List<UserHostel> findAllUserFromParentId(String parentId);

}
