package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class RolesPermission {
    private int moduleId;
    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canUpdate;

}
