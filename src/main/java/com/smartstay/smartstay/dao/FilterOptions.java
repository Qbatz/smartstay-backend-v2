package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.handlers.TenantFilterConverters;
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
public class FilterOptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filterOptionId;
    private String moduleName;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = TenantFilterConverters.class)
    private List<TenantFilters> filterOptions;
    private Boolean isActive;
    private Date createdAt;

}
