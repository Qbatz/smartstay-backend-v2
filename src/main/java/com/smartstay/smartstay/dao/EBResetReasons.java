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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EBResetReasons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reasonId;
    private String resetReason;
    private String hostelId;
    private Long resetReadingId;
    private String resetBy;
    private String createdBy;
    private Date resetOn;
}
