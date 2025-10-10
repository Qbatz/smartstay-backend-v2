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
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String subscriptionId;
    private String planName;
    private String planCode;
    private String subscriptionNumber;
    private String status;
    private int total;
    private int subTotal;
    private int gst;
    private int planAmount;
    private int discount;
    private int discountAmount;
    private String currentStatus;
    private Date createdAt;
    private Date activatedAt;
    private Date trialStartsAt;
    private Date trialEndsAt;
    private int trialRemainingDays;
    private Date nextBillingAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id")
    private HostelV1 hostel;
}
