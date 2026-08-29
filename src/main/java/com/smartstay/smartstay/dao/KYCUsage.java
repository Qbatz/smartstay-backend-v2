package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "kycusage")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KYCUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;
    private String hostelId;
    //requested by owner id
    private String latestRequestBy;
    //requested to is tenant id
    private String latestRequestTo;
    private String latestCompletionBy;
    private Integer requestCount;
    private Integer verifiedCount;
    private Date latestRequest;
    private Date latestVerified;

}
