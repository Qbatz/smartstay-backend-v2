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


}