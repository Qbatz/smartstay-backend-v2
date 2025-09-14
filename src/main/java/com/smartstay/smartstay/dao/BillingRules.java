package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer billingStartDate;
    private Integer billingDueDate;
    private Integer noticePeriod;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    private HostelV1 hostel;

}
