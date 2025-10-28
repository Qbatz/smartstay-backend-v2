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
public class HostelPlanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    private String hostelId;
    private String planCode;
    private String planName;
    private Date planStartsAt;
    private Date planEndsAt;
    private Date activatedAt;
    private Double paidAmount;
    private Double planAmount;
    private Date createdAt;


}
