package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolesRepository extends JpaRepository<RolesV1, Integer> {


    List<RolesV1> findAllByParentId(String parentId);
    RolesV1 findByRoleId(int roleId);
    RolesV1 findByRoleIdAndRoleNameNotIn(int roleId, List<String> roleNames);

    RolesV1 findByRoleIdAndParentId(int roleId,String parentId);
    RolesV1 findByRoleName(String roleName);

    RolesV1 findByRoleIdAndHostelId(int roleId, String hostelId);
    boolean existsByRoleId(int roleId);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM rolesv1 role
    WHERE role.role_name = :roleName
      AND role.parent_id = :parentId
    """, nativeQuery = true)
    int existsByParentIdAndRoleName(@Param("roleName") String roleName, @Param("parentId") String parentId);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM rolesv1 role
    WHERE role.role_name = :roleName
      AND role.parent_id = :parentId AND role.role_id = :roleId
    """, nativeQuery = true)
    int existsByParentIdAndRole(@Param("roleId") int roleId,@Param("roleName") String roleName, @Param("parentId") String parentId);
}
