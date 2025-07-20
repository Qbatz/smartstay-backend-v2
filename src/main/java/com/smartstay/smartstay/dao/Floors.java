package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.handlers.RolesPermissionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Floors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer floorId;
    private String floorName;
    private Boolean isActive;
    private Boolean isDeleted;
    private Date createdAt;
    private Date updatedAt;
    private String hostelId;

}