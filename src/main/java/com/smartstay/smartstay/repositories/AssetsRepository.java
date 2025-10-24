package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AssetsV1;
import com.smartstay.smartstay.dto.assets.AssetAssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetsRepository extends JpaRepository<AssetsV1, Long> {

    List<AssetsV1> findAllByHostelId(String hostelId);
    boolean existsByAssetNameAndIsDeletedFalseAndHostelId(String assetName,String hostelId);
    boolean existsByAssetNameAndIsDeletedFalseAndAssetIdNotAndHostelId(String assetName, Integer assetId, String hostelId);
    boolean existsBySerialNumberAndIsDeletedFalseAndHostelId(String serialNumber, String hostelId);
    boolean existsBySerialNumberAndIsDeletedFalseAndAssetIdNotAndHostelId(String serialNumber, Integer assetId, String hostelId);
    AssetsV1 findByAssetId(int assetId);
    AssetsV1 findByAssetIdAndHostelId(int assetId,String hostelId);

    @Query("SELECT new com.smartstay.smartstay.dto.assets.AssetAssignmentResponse(" +
            "a.assetId, a.assetName, a.brandName, a.productName, a.serialNumber, " +
            "DATE(a.purchaseDate), a.price, " +
            "h.hostelName, h.hostelId, f.floorId, v.vendorId, " +
            "COALESCE(CONCAT(v.firstName, ' ', v.lastName), 'N/A'), " +
            "f.floorName, r.roomId, r.roomName, b.bedId, b.bedName, " +
            "DATE(a.assignedAt), " +
            "CASE WHEN a.floorId IS NULL AND a.roomId IS NULL AND a.bedId IS NULL " +
            "THEN 'Unassigned' ELSE 'Assigned' END) " +
            "FROM AssetsV1 a " +
            "LEFT JOIN HostelV1 h ON a.hostelId = h.hostelId " +
            "LEFT JOIN Floors f ON a.floorId = f.floorId " +
            "LEFT JOIN VendorV1 v ON a.vendorId = v.vendorId " +
            "LEFT JOIN Rooms r ON a.roomId = r.roomId " +
            "LEFT JOIN Beds b ON a.bedId = b.bedId " +
            "WHERE a.isDeleted = false AND a.isActive = true AND a.hostelId = :hostelId " +
            "ORDER BY a.createdAt DESC")
    List<AssetAssignmentResponse> findAssetAssignmentDetails(@Param("hostelId") String hostelId);

    @Query("SELECT new com.smartstay.smartstay.dto.assets.AssetAssignmentResponse(" +
           "a.assetId, a.assetName, a.brandName, a.productName, a.serialNumber, a.purchaseDate, a.price, " +
           "h.hostelName, h.hostelId, f.floorId, v.vendorId, " +
           "COALESCE(CONCAT(v.firstName, ' ', v.lastName), 'N/A') AS vendorName, " +
           "f.floorName, r.roomId, r.roomName, b.bedId, b.bedName, a.assignedAt, " +
           "CASE WHEN a.floorId IS NULL AND a.roomId IS NULL AND a.bedId IS NULL THEN 'Unassigned' ELSE 'Assigned' END) " +
           "FROM AssetsV1 a " +
           "LEFT JOIN HostelV1 h ON a.hostelId = h.hostelId " +
           "LEFT JOIN Floors f ON a.floorId = f.floorId " +
           "LEFT JOIN VendorV1 v ON a.vendorId = v.vendorId " +
           "LEFT JOIN Rooms r ON a.roomId = r.roomId " +
           "LEFT JOIN Beds b ON a.bedId = b.bedId " +
           "WHERE a.isDeleted = false AND a.isActive = true " +
           "AND a.assetId = :assetId ORDER BY a.createdAt DESC")
    AssetAssignmentResponse findAssetAssignmentDetailsById(@Param("assetId") Integer assetId);
}
