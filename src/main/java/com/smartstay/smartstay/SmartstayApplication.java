package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.roles.Permission;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.services.TemplatesService;
import com.smartstay.smartstay.util.BillingCycle;
import com.smartstay.smartstay.util.BillingCycleUtil;
import com.smartstay.smartstay.util.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.yaml.snakeyaml.comments.CommentLine;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
                List<TenantFilters> filters = new ArrayList<>();

                TenantFilters filters1 = new TenantFilters();
                filters1.setSelected(true);
                filters1.setFieldName("Profile Pic");
                filters1.setOrder(1);

                TenantFilters filters2 = new TenantFilters();
                filters2.setSelected(true);
                filters2.setFieldName("Full Name");
                filters2.setOrder(2);

                TenantFilters filters3 = new TenantFilters();
                filters3.setSelected(true);
                filters3.setFieldName("Status");
                filters3.setOrder(3);

                TenantFilters filters4 = new TenantFilters();
                filters4.setSelected(true);
                filters4.setFieldName("Joining Date");
                filters4.setOrder(4);

                TenantFilters filters5 = new TenantFilters();
                filters5.setSelected(true);
                filters5.setFieldName("Mobile No");
                filters5.setOrder(5);

                TenantFilters filters6 = new TenantFilters();
                filters6.setSelected(true);
                filters6.setFieldName("Floor");
                filters6.setOrder(6);

                TenantFilters filters7 = new TenantFilters();
                filters7.setSelected(true);
                filters7.setFieldName("Room");
                filters7.setOrder(7);

                TenantFilters filters8 = new TenantFilters();
                filters8.setSelected(true);
                filters8.setFieldName("Bed");
                filters8.setOrder(8);

                TenantFilters filters9 = new TenantFilters();
                filters9.setSelected(false);
                filters9.setFieldName("Email ID");
                filters9.setOrder(9);

                TenantFilters filters10 = new TenantFilters();
                filters10.setSelected(false);
                filters10.setFieldName("Booking Date");
                filters10.setOrder(10);

                TenantFilters filters11 = new TenantFilters();
                filters11.setSelected(false);
                filters11.setFieldName("Monthly Rent");
                filters11.setOrder(11);

                TenantFilters filters12 = new TenantFilters();
                filters12.setSelected(false);
                filters12.setFieldName("Advance");
                filters12.setOrder(12);

                TenantFilters filters13 = new TenantFilters();
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