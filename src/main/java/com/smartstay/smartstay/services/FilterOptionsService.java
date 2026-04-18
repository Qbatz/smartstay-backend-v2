package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.FilterOptions;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.repositories.FilterOptionsRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FilterOptionsService {
    @Autowired
    private FilterOptionsRepositories filterOptionsRepositories;
    public List<ColumnFilters> findCustomersBasicFilters() {
        FilterOptions filterOptions = filterOptionsRepositories.findByModuleName(FilterOptionsModule.MODULE_TENANT.name());
        if (filterOptions != null) {
            return filterOptions.getFilterOptions();
        }
        return new ArrayList<>();
    }
}
