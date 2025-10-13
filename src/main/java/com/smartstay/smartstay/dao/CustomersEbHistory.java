package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.responses.electricity.RoomElectricityCustomersList;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CustomersEbHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer readingId;
    private String customerId;
    private Integer roomId;
    private Integer floorId;
    private Integer bedId;
    private Double units;
    private Double amount;
    private Date startDate;
    private Date endDate;
    private Date lastUpdate;
    private Date createdAt;
    private String createdBy;
    private String updatedBy;
}
