package com.smartstay.smartstay.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantFilters {
    //it should be pointing the position
    private int order;
    //eg. tenant name or phone or room
    private String fieldName;
    private boolean isSelected;
}
