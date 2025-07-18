package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.handlers.RolesPermissionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolesV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    private String roleName;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = RolesPermissionConverter.class)
    private List<RolesPermission> permissions;

}
