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
public class Beds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bedId;
    private String bedName;
    private Boolean isActive;
    private Boolean isDeleted;
    private Date createdAt;
    private Date updatedAt;
    private String parentId;
    private Integer roomId;
    private String hostelId;
    private boolean isBooked;
    private double rentAmount;
    private String status;
    private String currentStatus;
    private Date freeFrom;
}
