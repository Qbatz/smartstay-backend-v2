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

        return customerTableColumns.getColumns();
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

       TableColumns tableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, users.getUserId(), FilterOptionsModule.MODULE_TENANT.name());
       if (tableColumns == null) {
           List<ColumnFilters> listDefaultColumns = filterOptionsService.findCustomersBasicFilters();
           tableColumns = new TableColumns();
           tableColumns.setHostelId(hostelId);
           tableColumns.setUserId(users.getUserId());
           tableColumns.setModuleName(FilterOptionsModule.MODULE_TENANT.name());
           tableColumns.setActive(true);
           tableColumns.setCreatedAt(new Date());

           List<ColumnFilters> listNewColumnFilters = new ArrayList<>(listDefaultColumns
                   .stream()
                   .map(i -> {
                       boolean isExist = !customersTables
                               .stream()
                               .filter(i2 -> i2.fieldName().equalsIgnoreCase(i.getFieldName()))
                               .toList().isEmpty();
                       if (isExist) {
                           i.setSelected(true);
                       }
                       else {
                           i.setSelected(false);
                       }

                       return i;
                   })
                   .toList());

           tableColumns.setColumns(listNewColumnFilters);

           tableColumnsRepositories.save(tableColumns);

       }
       else {
           List<ColumnFilters> listNewColumnFilters = new ArrayList<>(tableColumns.getColumns()
                   .stream()
                   .map(i -> {
                       boolean isExist = !customersTables
                               .stream()
                               .filter(i2 -> i2.fieldName().equalsIgnoreCase(i.getFieldName()))
                               .toList().isEmpty();
                       if (isExist) {
                           i.setSelected(true);
                       }

                       return i;
                   })
                   .toList());

           tableColumns.setColumns(listNewColumnFilters);

           tableColumnsRepositories.save(tableColumns);
       }

       return new ResponseEntity<>(HttpStatus.OK);

    }
}
