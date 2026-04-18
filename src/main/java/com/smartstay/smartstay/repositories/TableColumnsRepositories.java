package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TableColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableColumnsRepositories extends JpaRepository<TableColumns, Long> {
    @Query("""
            SELECT tc FROM TableColumns tc WHERE tc.hostelId=:hostelId AND tc.userId=:userId AND 
            tc.moduleName=:moduleName AND tc.isActive=true
            """)
    TableColumns findByHostelIdAndUserId(String hostelId, String userId, String moduleName);
}
