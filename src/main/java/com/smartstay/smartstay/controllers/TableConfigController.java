package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.tables.CustomersTablesColumn;
import com.smartstay.smartstay.services.TableColumnService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v2/table-config")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class TableConfigController {
    @Autowired
    private TableColumnService tableColumnService;

    @PutMapping("/customers/{hostelId}")
    public ResponseEntity<?> modifyCustomerTables(@PathVariable("hostelId") String hostelId, @RequestBody List<CustomersTablesColumn> customersTables) {
        return tableColumnService.updateCustomerTableFields(hostelId, customersTables);
    }
}
