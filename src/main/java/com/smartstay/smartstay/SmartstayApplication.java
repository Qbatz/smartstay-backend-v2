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

    /**
     * Recomputes GST columns for every plan row on each startup so changes to the rates in
     * {@link #applyGstColumns} are persisted. Safe to run repeatedly when {@code applyGstColumns}
     * derives amounts from {@code finalPrice} (fallback: {@code price}).
     */
    @Bean
    CommandLineRunner backfillPlanGstColumns(PlansRepository plansRepository) {
        return args -> {
            List<Plans> allPlans = plansRepository.findAll();
            if (allPlans.isEmpty()) {
                return;
            }

            allPlans.forEach(SmartstayApplication::applyGstColumns);
            plansRepository.saveAll(allPlans);
        };
    }

    /**
     * Idempotent. Prefer {@code finalPrice} as the gross total; if it is null, use {@code price}
     * once (first migration / legacy rows). Then recompute cgst/sgst amounts and net {@code price}.
     */
    public static void applyGstColumns(Plans plan) {
        double cgstPercent = 9.0;
        double sgstPercent = 9.0;

        double basePrice;
        if (plan.getFinalPrice() != null) {
            basePrice = plan.getFinalPrice();
        } else if (plan.getPrice() != null) {
            basePrice = plan.getPrice();
        } else {
            basePrice = 0.0;
        }

        double cgstAmount = (cgstPercent / 100.0) * basePrice;
        double sgstAmount = (sgstPercent / 100.0) * basePrice;

        plan.setCgst(cgstPercent);
        plan.setSgst(sgstPercent);
        plan.setFinalPrice(basePrice);
        plan.setCgstAmount(cgstAmount);
        plan.setSgstAmount(sgstAmount);
        plan.setPrice(basePrice - (cgstAmount + sgstAmount));
    }

}