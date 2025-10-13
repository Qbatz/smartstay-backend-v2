package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AmentityRepository extends JpaRepository<AmenitiesV1, String> {

    boolean existsByAmenityNameAndHostelIdAndIsActiveTrueAndIsDeletedFalse(String amenityName, String hostelId);

    AmenitiesV1 findByAmenityIdAndHostelIdAndParentIdAndIsDeletedFalse(String amenityId, String hostelId, String parentId);

    boolean existsByAmenityIdAndHostelIdAndParentIdAndIsDeletedTrue(String amenityId, String hostelId, String parentId);

    @Query(value = """
                SELECT a.amenity_id   AS amenityId,
                       a.amenity_name AS amenityName,
                       a.amenity_amount AS amenityAmount,
                       a.pro_rate    AS proRate
                FROM amenitiesV1 a
                WHERE a.hostel_id = :hostelId and a.parent_id =:parentId AND a.is_active = true AND a.is_deleted = false
            """, nativeQuery = true)
    List<AmenityInfoProjection> findAmenityInfoByHostelId(@Param("hostelId") String hostelId, @Param("parentId") String parentId);

    @Query(value = """
                SELECT a.amenity_id   AS amenityId,
                       a.amenity_name AS amenityName,
                       a.amenity_amount AS amenityAmount,
                       a.pro_rate    AS proRate
                FROM amenitiesV1 a
                WHERE a.hostel_id = :hostelId and a.parent_id =:parentId and a.amenity_id =:amenityId AND a.is_active = true AND a.is_deleted = false
            """, nativeQuery = true)
    AmenityInfoProjection findAmenityInfoByHostelIdByAmenityId(@Param("hostelId") String hostelId, @Param("parentId") String parentId, @Param("amenityId") String amenityId);

}
