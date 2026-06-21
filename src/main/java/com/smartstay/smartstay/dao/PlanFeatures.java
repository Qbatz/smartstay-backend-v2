package com.smartstay.smartstay.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanFeatures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String featureName;
    private Double price;
    private Long smartstayFeatureId;
    //this is to show what is the header label should be appear on owner app (updates from console)
    //required for pro features on lower plans
    private String labelText;
    //this is to show the description text on owner app(text comes from console).
    //required for pro features.
    private String labelDescription;
    //pro features starts from date, can be null (start date comes from console, default null)
    private Date startsFrom;
    //pro feature end date, can be null. if null pro feature will not end for lower plans.
    //eg. KYC feature can be accessed for ever on Basic plans without ending.
    //endsAt comes from console default should be null
    private Date endsAt;
    //To disable the particular feature. This takes immediate effect.
    //comes from console app.
    private boolean isFeatureActive;
    private boolean isActive;

    //All the updates comes from console app.

    @ManyToOne
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private Plans plan;

}
