package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.DraftItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftItemsRepository extends JpaRepository<DraftItems, Long> {
}
