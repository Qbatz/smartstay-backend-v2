package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer billingStartDate;
    private Integer billDueDays;
    private Integer noticePeriod;
    private boolean isInitial;
    private Date startFrom;
    private Date endTill;
    private Date createdAt;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    private HostelV1 hostel;

}
