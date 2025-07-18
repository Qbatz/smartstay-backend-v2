package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<RolesV1, Integer> {
    RolesV1 findByRoleId(int roleId);
}
