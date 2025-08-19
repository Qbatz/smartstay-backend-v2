package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AssetsV1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetsRepository extends JpaRepository<AssetsV1, Integer> {

    List<AssetsV1> findAllByHostelId(String hostelId);

    AssetsV1 findByAssetId(int assetId);
}
