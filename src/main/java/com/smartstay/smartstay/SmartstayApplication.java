package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.Utils;
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
     * {@link #} are persisted. Safe to run repeatedly when {@code applyGstColumns}
     * derives amounts from {@code finalPrice} (fallback: {@code price}).
     */
//    @Bean
//    CommandLineRunner backfillPlanGstColumns(PlansRepository plansRepository) {
//        return args -> {
//            List<Plans> allPlans = plansRepository.findAll();
//            if (!allPlans.isEmpty()) {
//                List<Plans> listNewPlans = allPlans
//                        .stream()
//                        .map(i -> {
//                            double gstAmount = 0.0;
//                            double cgstAmount = 0.0;
//                            double sgstAmount = 0.0;
//                            double finalPrice = 0.0;
//
//                            double gstPercentage = 18;
//                            double cgstPercentage = gstPercentage/2;
//                            double sgstPercentage = gstPercentage/2;
//
//                            if (i.getPrice() != null) {
//                                gstAmount = (gstPercentage/100) * i.getPrice();
//                                cgstAmount = (cgstPercentage/100) * i.getPrice();
//                                sgstAmount = (sgstPercentage/100) * i.getPrice();
//                                finalPrice = i.getPrice() + gstAmount;
//                            }
//                            i.setGst(Utils.roundOffWithTwoDigit(gstPercentage));
//                            i.setGstAmount(Utils.roundOffWithTwoDigit(gstAmount));
//                            i.setCgst(Utils.roundOffWithTwoDigit(cgstPercentage));
//                            i.setSgst(Utils.roundOffWithTwoDigit(sgstPercentage));
//                            i.setCgstAmount(Utils.roundOffWithTwoDigit(cgstAmount));
//                            i.setSgstAmount(Utils.roundOffWithTwoDigit(sgstAmount));
//                            i.setFinalPrice(Utils.roundOffWithTwoDigit(finalPrice));
//
//                            return i;
//                        })
//                        .toList();
//                plansRepository.saveAll(listNewPlans);
//            }
//        };
//    }



}