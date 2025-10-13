package com.smartstay.smartstay.dao;

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
public class ExpenseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    private String categoryName;
    private String hostelId;
    private String createdBy;
    private String updateBy;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;

    @OneToMany(mappedBy = "expenseCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSubCategory> listSubCategories;
}
