package com.smartstay.smartstay.dao;

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
public class ElectricityReadings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer previousReading;
    private Integer currentReading;
    private Double currentUnitPrice;
    private String hostelId;
    private Integer roomId;
    private Date entryDate;
//    no of unit consumed
    private Integer consumption;
    private boolean isMissedEntry;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;


}
