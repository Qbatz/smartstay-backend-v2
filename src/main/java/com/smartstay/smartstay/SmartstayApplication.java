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
//    public CommandLineRunner mapAdvanceToInvoice(CustomersRepository customersRepository, InvoicesV1Repository invoicesV1Repository) {
//        return args -> {
//            Customers customers = customersRepository.findById("866f83ac-1f51-4d67-a2ca-1478aaa594c1").orElse(null);
//            if (customers != null) {
//                Advance advance = customers.getAdvance();
//                if (advance != null) {
//                    List<Deductions> deductionsList = advance.getDeductions();
//                    if (deductionsList != null) {
//                        InvoicesV1 invoicesV1 = invoicesV1Repository.findAdvanceInvoiceByCustomerId(customers.getCustomerId());
//                        double deductionAmount = deductionsList
//                                .stream()
//                                .mapToDouble(Deductions::getAmount)
//                                .sum();
//                        invoicesV1.setDeductionAmount(deductionAmount);
//                        invoicesV1.setDeductions(deductionsList);
//
//                        invoicesV1Repository.save(invoicesV1);
//                    }
//                }
//
//
//            }
//        };
//    }


}