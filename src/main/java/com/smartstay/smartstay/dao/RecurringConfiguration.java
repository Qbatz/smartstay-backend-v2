package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RecurringConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;
    private String hostelId;
    private Boolean shouldVerify;
    //owner id
    private String requestedBy;
    //Agent Id
    private String createdBy;
    private Date createdAt;
}
