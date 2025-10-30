package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Plans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;
    private String planName;
    private Double price;
    private Long duration;
    //discounts in percentage
    private Double discounts;
    //basic or premium or affordable
    private String planType;
    private String planCode;
    private boolean shouldShow;
    private boolean canCustomize;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanFeatures> featuresList;

}
