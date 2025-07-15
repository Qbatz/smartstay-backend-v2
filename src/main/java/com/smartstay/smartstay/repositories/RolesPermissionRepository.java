package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RolesPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolesPermissionRepository extends JpaRepository<RolesPermission, Integer> {

    List<RolesPermission> findAllByRoleId(int roleId);

    Optional<RolesPermission> findByRoleIdAndModuleId(int roleId, int moduleId);
}
