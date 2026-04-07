package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelImagesRepository extends JpaRepository<HostelImages, Integer> {
    List<HostelImages> findByHostel_HostelId(String hostelId);
}
