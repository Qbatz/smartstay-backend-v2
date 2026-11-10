package com.smartstay.smartstay;

import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.dto.kyc.KycUsage;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.expense.Expense;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
//    CommandLineRunner mapInvoiceAmount(InvoicesV1Repository invoicesV1Repository, InvoiceDiscountRepository invoiceDiscountRepository) {
//        return args -> {
//            List<InvoiceDiscounts> invoiceDiscounts= invoiceDiscountRepository.findActiveInvoices();
//            List<InvoicesV1> invoiceV1 = invoicesV1Repository.findAll();
//            List<InvoicesV1> newInvoices = invoiceV1
//                    .stream()
//                    .map(i -> {
//                        i.setInvoiceUrl(null);
//                        double discountAmount = 0;
//                        if (invoiceDiscounts != null) {
//                            InvoiceDiscounts id = invoiceDiscounts
//                                    .stream()
//                                    .filter(i2 -> i2.getInvoiceId().equalsIgnoreCase(i.getInvoiceId()))
//                                    .findFirst()
//                                    .orElse(null);
//                            if (id != null) {
//                                if (id.getDiscountAmount() != null) {
//                                    i.setDiscounted(true);
//                                    discountAmount = id.getDiscountAmount();
//                                }
//                            }
//                        }
//                        i.setInvoiceAmount(i.getTotalAmount() + discountAmount);
//                        i.setPayableAmount(Utils.roundOffWithTwoDigit(i.getTotalAmount() + discountAmount));
//                        i.setTotalAmount(Utils.roundOfDouble(i.getTotalAmount() - discountAmount));
//                        return i;
//                    })
//                    .toList();
//
//            invoicesV1Repository.saveAll(newInvoices);
//        };
//    }
}