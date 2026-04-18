package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.repositories.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

    @Bean
    CommandLineRunner addTenantBasicFilterOptions(FilterOptionsRepositories filterOptionsRepositories) {
        return args -> {
            FilterOptions tenantFilterOptions = filterOptionsRepositories.findTenantFilterOption();
            if (tenantFilterOptions == null) {
                tenantFilterOptions = new FilterOptions();
                tenantFilterOptions.setModuleName(FilterOptionsModule.MODULE_TENANT.name());
                tenantFilterOptions.setIsActive(true);
                tenantFilterOptions.setCreatedAt(new Date());
                List<ColumnFilters> filters = new ArrayList<>();

                ColumnFilters filters1 = new ColumnFilters();
                filters1.setSelected(true);
                filters1.setFieldName("Profile Pic");
                filters1.setOrder(1);

                ColumnFilters filters2 = new ColumnFilters();
                filters2.setSelected(true);
                filters2.setFieldName("Full Name");
                filters2.setOrder(2);

                ColumnFilters filters3 = new ColumnFilters();
                filters3.setSelected(true);
                filters3.setFieldName("Status");
                filters3.setOrder(3);

                ColumnFilters filters4 = new ColumnFilters();
                filters4.setSelected(true);
                filters4.setFieldName("Joining Date");
                filters4.setOrder(4);

                ColumnFilters filters5 = new ColumnFilters();
                filters5.setSelected(true);
                filters5.setFieldName("Mobile No");
                filters5.setOrder(5);

                ColumnFilters filters6 = new ColumnFilters();
                filters6.setSelected(true);
                filters6.setFieldName("Floor");
                filters6.setOrder(6);

                ColumnFilters filters7 = new ColumnFilters();
                filters7.setSelected(true);
                filters7.setFieldName("Room");
                filters7.setOrder(7);

                ColumnFilters filters8 = new ColumnFilters();
                filters8.setSelected(true);
                filters8.setFieldName("Bed");
                filters8.setOrder(8);

                ColumnFilters filters9 = new ColumnFilters();
                filters9.setSelected(false);
                filters9.setFieldName("Email ID");
                filters9.setOrder(9);

                ColumnFilters filters10 = new ColumnFilters();
                filters10.setSelected(false);
                filters10.setFieldName("Booking Date");
                filters10.setOrder(10);

                ColumnFilters filters11 = new ColumnFilters();
                filters11.setSelected(false);
                filters11.setFieldName("Monthly Rent");
                filters11.setOrder(11);

                ColumnFilters filters12 = new ColumnFilters();
                filters12.setSelected(false);
                filters12.setFieldName("Advance");
                filters12.setOrder(12);

                ColumnFilters filters13 = new ColumnFilters();
                filters13.setSelected(false);
                filters13.setFieldName("Booking Amount");
                filters13.setOrder(13);

                filters.add(filters1);
                filters.add(filters2);
                filters.add(filters3);
                filters.add(filters4);
                filters.add(filters5);
                filters.add(filters6);
                filters.add(filters7);
                filters.add(filters8);
                filters.add(filters9);
                filters.add(filters10);
                filters.add(filters11);
                filters.add(filters12);
                filters.add(filters13);

                tenantFilterOptions.setFilterOptions(filters);

                filterOptionsRepositories.save(tenantFilterOptions);
            }
        };
    }
}