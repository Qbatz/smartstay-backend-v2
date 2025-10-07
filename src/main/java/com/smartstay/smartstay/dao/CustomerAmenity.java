package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String amenityMapperId;
    String amenityId;
    String customerId;
    boolean proRate;
    boolean isActive;
}
