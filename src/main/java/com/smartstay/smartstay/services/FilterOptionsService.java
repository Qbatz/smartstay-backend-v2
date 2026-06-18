package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.FilterOptions;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.repositories.FilterOptionsRepositories;
import com.smartstay.smartstay.util.columnOptions.VendorColumnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FilterOptionsService {
    @Autowired
    private FilterOptionsRepositories filterOptionsRepositories;
    public List<ColumnFilters> findCustomersBasicFilters() {
        FilterOptions filterOptions = filterOptionsRepositories.findByModuleName(FilterOptionsModule.MODULE_TENANT.name());
        if (filterOptions != null) {
            return filterOptions.getFilterOptions()
                    .stream()
                    .sorted(Comparator.comparing(ColumnFilters::getOrder))
                    .toList();
        }
        return new ArrayList<>();
    }

    public List<ColumnFilters> findBookingsBasicFilters() {
        FilterOptions filterOptions = filterOptionsRepositories.findByModuleName(FilterOptionsModule.MODULE_BOOKINGS.name());
        if (filterOptions != null) {
            return filterOptions.getFilterOptions()
                    .stream()
                    .sorted(Comparator.comparing(ColumnFilters::getOrder))
                    .toList();
        }
        return new ArrayList<>();
    }

    public List<ColumnFilters> findVendorBasicFilters() {
        FilterOptions filterOptions = filterOptionsRepositories.findByModuleName(FilterOptionsModule.MODULE_VENDOR.name());
        if (filterOptions != null && filterOptions.getFilterOptions() != null && !filterOptions.getFilterOptions().isEmpty()) {
            return filterOptions.getFilterOptions()
                    .stream()
                    .sorted(Comparator.comparing(ColumnFilters::getOrder))
                    .toList();
        }
        // Fall back to the code-defined defaults so the feature works without a DB seed row.
        return VendorColumnUtils.defaultColumns();
    }
}
