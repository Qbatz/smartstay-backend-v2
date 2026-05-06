package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
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


    /**
     *
     * required to run on prod
     *
     * @param invoicesV1Repository
     * @return
     */
//    @Bean
//    CommandLineRunner mapInvoiceAmount(InvoicesV1Repository invoicesV1Repository) {
//        return args -> {
//            List<String> invoiceTypes = new ArrayList<>();
//            invoiceTypes.add(InvoiceType.BOOKING.name());
//            invoiceTypes.add(InvoiceType.ADVANCE.name());
//            List<InvoicesV1> listAdvanceInvoices = invoicesV1Repository.findAllAdvances(invoiceTypes)
//                    .stream()
//                    .map(i -> {
//                        if (i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
//                            i.setBalanceAmount(i.getPaidAmount());
//                        }
//                        else if (i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
//                            i.setBalanceAmount(i.getPaidAmount());
//                        }
//                        else {
//                            i.setBalanceAmount(0.0);
//                        }
//
//                        return i;
//                    })
//                    .toList();
//
//            invoicesV1Repository.saveAll(listAdvanceInvoices);
//
//        };
//    }

}