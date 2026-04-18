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
public class TableColumns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long columnId;
    private String hostelId;
    private String userId;
    //From filter options module
    private String moduleName;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = TenantFilterConverters.class)
    private List<ColumnFilters> columns;
    private boolean isActive;
    private Date createdAt;
}
