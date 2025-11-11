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
public class CustomersAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String amenityId;
    String customerId;
    Double amenityPrice;
    Date createdAt;
    Date updatedAt;
    String updatedBy;
    Date startDate;
    Date endDate;
}
