package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner mapInvoiceSubTotal(InvoicesV1Repository invoicesV1Repository) {
//        return args -> {
//            List<InvoicesV1> listInvoices = invoicesV1Repository.findAll()
//                    .stream()
//                    .filter(i -> i.getSubTotal() == null)
//                    .map(i -> {
//                        i.setSubTotal(i.getTotalAmount());
//                        return i;
//                    })
//                    .toList();
//            invoicesV1Repository.saveAll(listInvoices);
//
//            invoicesV1Repository.saveAll(newAdvanceInvoices);
//        };
//    }

//    @Bean
//    CommandLineRunner createDraftsTable(JdbcTemplate jdbcTemplate) {
//        return args -> {
//            jdbcTemplate.execute("""
//                    CREATE TABLE IF NOT EXISTS drafts (
//                        customer_id VARCHAR(36) NOT NULL,
//                        hostel_id VARCHAR(36) NOT NULL,
//                        joining_date DATE NULL,
//                        booking_date DATE NULL,
//                        booking_amount DOUBLE NULL,
//                        floor_id INT NULL,
//                        room_id INT NULL,
//                        bed_id INT NULL,
//                        bank_id VARCHAR(64) NULL,
//                        reference_number VARCHAR(255) NULL,
//                        advance_amount DOUBLE NULL,
//                        rental_amount DOUBLE NULL,
//                        stay_type VARCHAR(32) NULL,
//                        deductions_json LONGTEXT NULL,
//                        pro_rate TINYINT(1) NULL,
//                        created_at DATETIME NOT NULL,
//                        updated_at DATETIME NOT NULL,
//                        PRIMARY KEY (customer_id),
//                        KEY idx_drafts_hostel (hostel_id)
//                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
//                    """);
//        };
//    }

//    @Bean
//    CommandLineRunner removeUnwantedRentRevision(RentHistoryRepository rentHistoryRepository) {
//        return args -> {
//            List<UpcomingRents> upcomingRentsList = rentHistoryRepository.findUpcomingRents(new Date());
//            List<String> customerIdsHavingMultipleRents = upcomingRentsList
//                    .stream()
//                    .map(UpcomingRents::customerId)
//                    .toList();
//            List<RentHistory> listDuplicateRentHistory = rentHistoryRepository.findByCustomerIds(customerIdsHavingMultipleRents, new Date());
//            if (listDuplicateRentHistory != null && !listDuplicateRentHistory.isEmpty()) {
//                HashMap<String, RentHistory> mapRh = new HashMap<>();
//                List<RentHistory> rentHistoryToUpdate = new ArrayList<>();
//                listDuplicateRentHistory.forEach(item -> {
//                    RentHistory rh = listDuplicateRentHistory
//                            .stream()
//                            .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
//                            .min(Comparator.comparing(RentHistory::getStartsFrom))
//                            .orElse(null);
//                    if (rh != null) {
//                        if (!mapRh.containsKey(rh.getCustomerId())) {
//                            mapRh.put(rh.getCustomerId(), rh);
//                        }
//                    }
//                });
//
//                if (!mapRh.isEmpty()) {
//                    for (String key : mapRh.keySet()) {
//                        List<RentHistory> rh = listDuplicateRentHistory
//                                .stream()
//                                .filter(i -> i.getCustomerId().equalsIgnoreCase(key))
//                                .filter(i1 -> {
//                                    Long mapId = mapRh.get(key).getId();
//                                    return !i1.getId().equals(mapId);
//                                })
//                                .toList();
//                        rentHistoryToUpdate.addAll(rh);
//                    }
//                }
//
//                rentHistoryRepository.deleteAll(rentHistoryToUpdate);
//            }
//        };
//    }



}