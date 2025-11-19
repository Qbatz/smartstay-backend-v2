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
public class RentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double rent;
    private String customerId;
    private String reason;
    private Date startsFrom;
    private Date endingAt;
    private Date createdAt;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingsV1 booking;

}
