package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.TableColumns;
import com.smartstay.smartstay.repositories.TableColumnsRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableColumnService {
    @Autowired
    private FilterOptionsService filterOptionsService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private TableColumnsRepositories tableColumnsRepositories;
    public List<ColumnFilters> getCustomerColumns(String hostelId, String moduleName) {
        TableColumns customerTableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, authentication.getName(), moduleName);
        if (customerTableColumns == null) {
            return filterOptionsService.findCustomersBasicFilters();
        }

        return customerTableColumns.getColumns();
    }
}
