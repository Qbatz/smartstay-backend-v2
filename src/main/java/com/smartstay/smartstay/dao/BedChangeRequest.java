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
public class BedChangeRequest {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String hostelId;
    String customerId;
    Integer bedId;
    Integer floorId;
    Integer roomId;
    Date startsFrom;
    String reason;
    String preferredType;
    Date createdAt;
    Date updatedAt;
    String currentStatus;
    boolean isActive;
    boolean isDeleted;
}
