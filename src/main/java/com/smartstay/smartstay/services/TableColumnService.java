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

    public List<ColumnFilters> getBookingsColumns(String hostelId, String moduleName) {
        TableColumns customerTableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, authentication.getName(), moduleName);
        if (customerTableColumns == null) {
            return filterOptionsService.findBookingsBasicFilters();
        }

        return customerTableColumns.getColumns()
                .stream()
                .sorted(Comparator.comparing(ColumnFilters::getOrder))
                .toList();
    }

    public List<ColumnFilters> getVendorColumns(String hostelId, String moduleName) {
        TableColumns vendorTableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, authentication.getName(), moduleName);
        if (vendorTableColumns == null) {
            return filterOptionsService.findVendorBasicFilters();
        }

        return vendorTableColumns.getColumns()
                .stream()
                .sorted(Comparator.comparing(ColumnFilters::getOrder))
                .toList();
    }

    public ResponseEntity<?> updateVendorTableFields(String hostelId, List<CustomersTablesColumn> vendorTables) {
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
        if (vendorTables == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (vendorTables.isEmpty()) {
            return new ResponseEntity<>(Utils.ATLEAST_ONE_COLUMN_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        List<ColumnFilters> listDefaultColumns = filterOptionsService.findVendorBasicFilters();
        boolean isAnySelected = vendorTables.stream()
                .anyMatch(i -> i != null && Boolean.TRUE.equals(i.isSelected()));

        TableColumns tableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, users.getUserId(), FilterOptionsModule.MODULE_VENDOR.name());
        if (tableColumns == null) {
            tableColumns = new TableColumns();
            tableColumns.setHostelId(hostelId);
            tableColumns.setUserId(users.getUserId());
            tableColumns.setModuleName(FilterOptionsModule.MODULE_VENDOR.name());
            tableColumns.setActive(true);
            tableColumns.setCreatedAt(new Date());
        }

        if (!isAnySelected) {
            tableColumns.setColumns(listDefaultColumns);
        } else {
            List<ColumnFilters> listNewColumns = vendorTables
                    .stream()
                    .filter(i -> i != null && i.fieldName() != null)
                    .map(i -> {
                        ColumnFilters newFilters = new ColumnFilters();
                        newFilters.setSelected(Boolean.TRUE.equals(i.isSelected()));
                        newFilters.setFieldName(i.fieldName());
                        newFilters.setOrder(i.order() != null ? i.order() : 0);
                        return newFilters;
                    })
                    .toList();
            tableColumns.setColumns(listNewColumns);
        }
        tableColumns.setUpdatedAt(new Date());
        tableColumnsRepositories.save(tableColumns);

        return new ResponseEntity<>(HttpStatus.OK);
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

    public ResponseEntity<?> updateBookingTableFields(String hostelId, List<CustomersTablesColumn> customersTables) {
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

        TableColumns tableColumns = tableColumnsRepositories.findByHostelIdAndUserId(hostelId, users.getUserId(), FilterOptionsModule.MODULE_BOOKINGS.name());
        if (tableColumns == null) {
            List<ColumnFilters> listDefaultColumns = filterOptionsService.findBookingsBasicFilters();
            tableColumns = new TableColumns();
            tableColumns.setHostelId(hostelId);
            tableColumns.setUserId(users.getUserId());
            tableColumns.setModuleName(FilterOptionsModule.MODULE_BOOKINGS.name());
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
            List<ColumnFilters> listDefaultColumns = filterOptionsService.findBookingsBasicFilters();
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
