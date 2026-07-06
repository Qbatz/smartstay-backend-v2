package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.handlers.DeductionsConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    private Double totalAdvanceAmount;

    @Column(name = "rental_amount")
    private Double rentalAmount;

    @Column(name = "stay_type", length = 32)
    private String stayType;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = DeductionsConverter.class)
    private List<Deductions> deductions;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = DeductionsConverter.class)
    private List<Deductions> oneTimeDeductions;

    @Column(name = "pro_rate")
    private Boolean proRate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "aadhar_pic")
    private String aadharPic;

    @Column(name = "pan_pic")
    private String panPic;

    @Column(name = "id_proof_json", columnDefinition = "LONGTEXT")
    private String idProofJson;

    @Column(name = "address_json", columnDefinition = "LONGTEXT")
    private String addressJson;

    @Column(name = "booking_json", columnDefinition = "LONGTEXT")
    private String bookingJson;

    @Column(name = "job_details_json", columnDefinition = "LONGTEXT")
    private String jobDetailsJson;

    @Column(name = "guardians_json", columnDefinition = "LONGTEXT")
    private String guardiansJson;
    private Boolean hasRefundableAdvance;
    //this will be Either booking or check in
    //From Draft type enum
    private String type;
    private String transactionId;
    private String createdBy;
    private String updatedBy;
}
