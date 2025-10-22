package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomersBedHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int bedId;
    private int roomId;
    private int floorId;
    private String hostelId;
    private Date startDate;
    private Date endDate;
    private String customerId;
    private String changedBy;
    private String reason;
    private boolean isActive;
    private Date createdAt;
    //this is from CustomerBedType enum
    private String type;
    private double rentAmount;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingsV1 booking;

}
