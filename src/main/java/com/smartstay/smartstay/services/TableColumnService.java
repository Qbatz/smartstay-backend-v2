package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.TableColumns;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.payloads.tables.CustomersTablesColumn;
import com.smartstay.smartstay.repositories.TableColumnsRepositories;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class TableColumnService {
    @Autowired
    private FilterOptionsService filterOptionsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private TableColumnsRepositories tableColumnsRepositories;
    public List<ColumnFilters> getCustomerColumns(String hostelId, String moduleName) {
        TableColumns customerTableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, authentication.getName(), moduleName);
        if (customerTableColumns == null) {
            return filterOptionsService.findCustomersBasicFilters();
        }

        return customerTableColumns.getColumns()
                .stream()
                .sorted(Comparator.comparing(ColumnFilters::getOrder))
                .toList();
    }

    public ResponseEntity<?> updateCustomerTableFields(String hostelId, List<CustomersTablesColumn> customersTables) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
       if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
           return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
       }
       if (customersTables == null) {
           return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
       }

       if (customersTables.isEmpty()) {
           return new ResponseEntity<>(Utils.ATLEAST_ONE_COLUMN_REQUIRED, HttpStatus.BAD_REQUEST);
       }

       TableColumns tableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, users.getUserId(), FilterOptionsModule.MODULE_TENANT.name());
       if (tableColumns == null) {
           List<ColumnFilters> listDefaultColumns = filterOptionsService.findCustomersBasicFilters();
           tableColumns = new TableColumns();
           tableColumns.setHostelId(hostelId);
           tableColumns.setUserId(users.getUserId());
           tableColumns.setModuleName(FilterOptionsModule.MODULE_TENANT.name());
           tableColumns.setActive(true);
           tableColumns.setCreatedAt(new Date());

           boolean isAnySelected = customersTables
                   .stream().anyMatch(CustomersTablesColumn::isSelected);
           if (!isAnySelected) {
               tableColumns.setColumns(listDefaultColumns);
           }
           else {
               List<ColumnFilters> listNewColumns = customersTables
                       .stream()
                       .map(i -> {
                           ColumnFilters newFilters = new ColumnFilters();
                           newFilters.setSelected(i.isSelected());
                           newFilters.setFieldName(i.fieldName());
                           newFilters.setOrder(i.order());

                           return newFilters;
                       })
                       .toList();


               tableColumns.setColumns(listNewColumns);
           }



           tableColumnsRepositories.save(tableColumns);

       }
       else {
           List<ColumnFilters> listDefaultColumns = filterOptionsService.findCustomersBasicFilters();
           boolean isAnySelected = customersTables
                   .stream().anyMatch(CustomersTablesColumn::isSelected);
           if (!isAnySelected) {
               tableColumns.setColumns(listDefaultColumns);
           }
           else {

               List<ColumnFilters> listNewColumns = customersTables
                       .stream()
                       .map(i -> {
                           ColumnFilters newFilters = new ColumnFilters();
                           newFilters.setSelected(i.isSelected());
                           newFilters.setFieldName(i.fieldName());
                           newFilters.setOrder(i.order());

                           return newFilters;
                       })
                       .toList();


               tableColumns.setColumns(listNewColumns);
           }

           tableColumnsRepositories.save(tableColumns);
       }

       return new ResponseEntity<>(HttpStatus.OK);

    }
}
