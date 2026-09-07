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
public class KycHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    private String hostelId;
    private Date startDate;
    private Date endDate;
    private Boolean isCancelledDueToPlan;
    private String cancellationReason;
    private String activationReason;
    private String cancelledBy;
    private Date createdAt;
    //it shoud be agent id or null
    private String createdBy;
    private String updatedBy;
}
