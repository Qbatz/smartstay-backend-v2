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
public class Reasons {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String reasonId;
    private String reasonType;
    private String reasonText;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customers customers;


}
