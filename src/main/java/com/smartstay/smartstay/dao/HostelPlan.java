package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hostelPlanId;
    private String currentPlanCode;
    private String currentPlanName;
    private Date currentPlanStartsAt;
    private Date currentPlanEndsAt;
    private Double currentPlanPrice;
    private Double paidAmount;
    private boolean isTrial;
    private Date trialEndingAt;

    @OneToOne
    @JoinColumn(name = "hostel_id", referencedColumnName = "hostelId")
    private HostelV1 hostel;

}
