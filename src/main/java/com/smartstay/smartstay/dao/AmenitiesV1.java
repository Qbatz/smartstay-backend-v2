package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmenitiesV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String amenityId;
    String amenityName;
    Double amenityAmount;
    Boolean isActive;
    Boolean isDeleted;
    String createdBy;
    String updatedBy;
    Date createdAt;
    Date updatedAt;
    String hostelId;
    String parentId;
}
