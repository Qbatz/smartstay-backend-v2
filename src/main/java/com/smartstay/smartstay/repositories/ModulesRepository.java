package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Modules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulesRepository extends JpaRepository<Modules, Integer> {
    Modules findByModuleName(String moduleName);
}
