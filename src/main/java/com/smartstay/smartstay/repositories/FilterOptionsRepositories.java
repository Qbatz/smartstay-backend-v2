package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.FilterOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterOptionsRepositories extends JpaRepository<FilterOptions, Long> {
    @Query("""
            SELECT fo FROM FilterOptions fo WHERE fo.moduleName='MODULE_TENANT'
            """)
    FilterOptions findTenantFilterOption();
}
