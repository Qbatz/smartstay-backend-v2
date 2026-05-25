package com.smartstay.smartstay.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Draft-only fields linked 1:1 to {@link Customers} via {@code customerId}.
 */
@Entity
@Table(name = "drafts")
@Data
@NoArgsConstructor
public class Draft {

    @Id
    @Column(name = "customer_id", length = 36, nullable = false)
    private String customerId;

    @Column(name = "hostel_id", length = 36, nullable = false)
    private String hostelId;

    @Temporal(TemporalType.DATE)
    @Column(name = "joining_date")
    private Date joiningDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "booking_date")
    private Date bookingDate;

    @Column(name = "booking_amount")
    private Double bookingAmount;

    @Column(name = "floor_id")
    private Integer floorId;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "bed_id")
    private Integer bedId;

    @Column(name = "bank_id", length = 64)
    private String bankId;

    @Column(name = "reference_number", length = 255)
    private String referenceNumber;

    @Column(name = "advance_amount")
    private Double advanceAmount;

    @Column(name = "rental_amount")
    private Double rentalAmount;

    @Column(name = "stay_type", length = 32)
    private String stayType;

    @Column(name = "deductions_json", columnDefinition = "LONGTEXT")
    private String deductionsJson;

    @Column(name = "pro_rate")
    private Boolean proRate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
