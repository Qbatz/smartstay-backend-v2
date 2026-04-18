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
    //From filteroption module enum
    private String moduleName;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = TenantFilterConverters.class)
    private List<ColumnFilters> filterOptions;
    private Boolean isActive;
    private Date createdAt;

}
