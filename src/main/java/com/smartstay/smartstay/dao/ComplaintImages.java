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
public class ComplaintImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String imageUrl;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isActive;
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private ComplaintsV1 complaints;
}
