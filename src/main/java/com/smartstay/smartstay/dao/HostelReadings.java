package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HostelReadings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double previousReading;
    private Double currentReading;
    private Double currentUnitPrice;
    private String hostelId;
    private String billStatus;
    private Date billStartDate;
    private Date billEndDate;
    private Date entryDate;
    //    no of unit consumed
    private Double consumption;
    private boolean isFirstEntry;
    private boolean isMissedEntry;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
}
