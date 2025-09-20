package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean shouldIncludeInRent;
    private String typeOfReading;
    private boolean isProRate;
    private Date lastUpdate;
    private String updatedBy;
    private Double charge;

    @OneToOne()
    @JoinColumn(name = "hostel_id", referencedColumnName = "hostelId")
    private HostelV1 hostel;
}
