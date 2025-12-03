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
public class ComplaintComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String comment;
    private String userName;
    private Boolean isActive;
    private Date commentDate;


    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private ComplaintsV1 complaint;
}
