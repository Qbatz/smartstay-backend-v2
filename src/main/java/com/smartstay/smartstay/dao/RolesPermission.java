package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Setter
@Getter
public class RolesPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int roleId;
    private int moduleId;
    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canUpdate;

}
