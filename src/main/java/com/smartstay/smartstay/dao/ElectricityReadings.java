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
    //this is for billed month and year. eg. 03/2025 reading will taken on 01/04/2025 or later
    //or depends on billing cycle
    private Integer billedMonth;
    private Integer billedYear;
    private Date entryDate;
//    no of unit consumed
    private Double consumption;
    private boolean isMissedEntry;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;

}
