package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolesRepository extends JpaRepository<RolesV1, Integer> {


    List<RolesV1> findAllByParentId(String parentId);
}
