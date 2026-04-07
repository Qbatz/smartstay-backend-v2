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
public class CustomerRecurringTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackerId;
    private String hostelId;
    private String customerId;
    private Integer creationDay;
    private Integer creationMonth;
    private Integer creationYear;
    private Date createdAt;
    //Recurring creation Mode enum
    private String mode;
    private String createdBy;
}
