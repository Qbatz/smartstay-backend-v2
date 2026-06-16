package com.smartstay.smartstay.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "units")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Units {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer unitId;

    private String unitName;

    private String addedBy;

    private boolean isEnabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    private String modifiedBy;
}
