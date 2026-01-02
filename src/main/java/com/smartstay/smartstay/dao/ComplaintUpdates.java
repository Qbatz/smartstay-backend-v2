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
public class ComplaintUpdates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long updateId;
//    from complaint status enum
    String status;
    //from user type enum
    String userType;
    String assignedTo;
    String updatedBy;
    Date createdAt;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    ComplaintsV1 complaint;

}
