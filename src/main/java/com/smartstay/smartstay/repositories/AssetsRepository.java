package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AssetsV1;
import com.smartstay.smartstay.dto.assets.AssetAssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetsRepository extends JpaRepository<AssetsV1, Integer> {

    List<AssetsV1> findAllByHostelId(String hostelId);
    boolean existsByAssetNameAndIsDeletedFalse(String assetName);
    boolean existsByAssetNameAndIsDeletedFalseAndAssetIdNot(String assetName, Integer assetId);
    boolean existsBySerialNumberAndIsDeletedFalse(String serialNumber);
    boolean existsBySerialNumberAndIsDeletedFalseAndAssetIdNot(String serialNumber, Integer assetId);
    AssetsV1 findByAssetId(int assetId);
    AssetsV1 findByAssetIdAndHostelId(int assetId,String hostelId);

    @Query("SELECT new com.smartstay.smartstay.dto.assets.AssetAssignmentResponse(" +
           "a.assetId, a.assetName, a.brandName, a.productName, a.serialNumber, a.purchaseDate, a.price, " +
           "h.hostelName, CASE WHEN a.floorId IS NULL AND a.roomId IS NULL AND a.bedId IS NULL THEN 'Unassigned' ELSE 'Assigned' END) " +
           "FROM AssetsV1 a " +
           "LEFT JOIN HostelV1 h ON a.hostelId = h.hostelId " +
           "WHERE a.isDeleted = false AND a.isActive = true AND a.hostelId = :hostelId order by a.createdAt desc")
    List<AssetAssignmentResponse> findAssetAssignmentDetails(@Param("hostelId") String hostelId);

    @Query("SELECT new com.smartstay.smartstay.dto.assets.AssetAssignmentResponse(" +
           "a.assetId, a.assetName, a.brandName, a.productName, a.serialNumber, a.purchaseDate, a.price, " +
           "h.hostelName, CASE WHEN a.floorId IS NULL AND a.roomId IS NULL AND a.bedId IS NULL THEN 'Unassigned' ELSE 'Assigned' END) " +
           "FROM AssetsV1 a " +
           "LEFT JOIN HostelV1 h ON a.hostelId = h.hostelId " +
           "WHERE a.isDeleted = false AND a.isActive = true AND a.assetId = :assetId")
    AssetAssignmentResponse findAssetAssignmentDetailsById(@Param("assetId") Integer assetId);

}
