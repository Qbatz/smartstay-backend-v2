package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostelV1Repository extends JpaRepository<HostelV1, String> {

    HostelV1 findByHostelId(String hostelId);

    HostelV1 findByHostelIdAndParentId(String hostelId,String parentId);
}
