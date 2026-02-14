package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BillTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillTemplateTypeRepository extends JpaRepository<BillTemplateType, Integer> {
}
