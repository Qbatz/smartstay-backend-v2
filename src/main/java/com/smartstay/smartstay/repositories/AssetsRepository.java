package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AssetsV1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetsRepository extends JpaRepository<AssetsV1, Long> {

    List<AssetsV1> findAllByHostelId(String hostelId);
    List<AssetsV1> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalseOrderByCreatedAtDesc(String hostelId);
    List<AssetsV1> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalseAndAssetNameContainingIgnoreCaseOrderByCreatedAtDesc(String hostelId,
                                                                                                                         String assetName
    );
    boolean existsByAssetNameAndIsDeletedFalseAndHostelId(String assetName,String hostelId);
    boolean existsByAssetNameAndIsDeletedFalseAndAssetIdNotAndHostelId(String assetName, Integer assetId, String hostelId);
    boolean existsBySerialNumberAndIsDeletedFalseAndHostelId(String serialNumber, String hostelId);
    boolean existsBySerialNumberAndIsDeletedFalseAndAssetIdNotAndHostelId(String serialNumber, Integer assetId, String hostelId);
    AssetsV1 findByAssetId(int assetId);
    AssetsV1 findByAssetIdAndHostelId(int assetId,String hostelId);
    List<AssetsV1> findByHostelId(String hostelId);
    AssetsV1 findByAssetIdAndIsActiveTrueAndIsDeletedFalse(Long assetId);
}
