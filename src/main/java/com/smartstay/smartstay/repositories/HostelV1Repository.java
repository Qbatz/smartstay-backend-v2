package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelV1Repository extends JpaRepository<HostelV1, String> {

    HostelV1 findByHostelId(String hostelId);

    HostelV1 findByHostelIdAndParentId(String hostelId,String parentId);

    HostelV1 findByHostelIdAndIsDeletedFalse(String hostelId);

    @Query("""
            SELECT hostel FROM HostelV1 hostel WHERE hostel.hostelId NOT IN(:hostelIds)
            """)
    List<HostelV1> findAllHostelsNoIncludeIds(List<String> hostelIds);

}
