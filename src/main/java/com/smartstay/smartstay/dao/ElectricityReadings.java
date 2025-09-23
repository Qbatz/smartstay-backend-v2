package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class ElectricityReadings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Double previousReading;
    private Double currentReading;
    private Double currentUnitPrice;
    private String hostelId;
    private String billStatus;
    private Integer roomId;
    private Integer floorId;
    private Date entryDate;
//    no of unit consumed
    private Double consumption;
    private boolean isMissedEntry;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;

}
