package com.smartstay.smartstay.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "vendor_comments", indexes = {
        @Index(name = "idx_vendor_comments_vendor_id", columnList = "vendorId"),
        @Index(name = "idx_vendor_comments_created_at", columnList = "createdAt")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer vendorId;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Soft-delete flag, consistent with the other vendor entities (categories / units).
    private boolean isActive;

    // Audit fields, populated by the service on create/update.
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
}
