package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.dto.kyc.KycUsage;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.*;
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
//    CommandLineRunner backupTransactions(BankTransactionRepository bankTransactionRepository, TempTransactionsRepositories tempTransactionsRepositories) {
//        return args -> {
//            List<BankTransactionsV1> bankTransactionsV1s = bankTransactionRepository.findAll();
//            if (bankTransactionsV1s != null) {
//                List<TempTransactions> listTempTransactions = bankTransactionsV1s
//                        .stream()
//                        .map(i -> {
//                            TempTransactions t = new TempTransactions();
//                            t.setTransactionId(i.getTransactionId());
//                            t.setBankId(i.getBankId());
//                            t.setReferenceNumber(i.getReferenceNumber());
//                            t.setAmount(i.getAmount());
//                            t.setAccountBalance(i.getAccountBalance());
//                            t.setDescription(i.getDescription());
//                            t.setType(i.getType());
//                            t.setSource(i.getSource());
//                            t.setSourceId(i.getSourceId());
//                            t.setPaymentMethodId(i.getPaymentMethodId());
//                            t.setInvestorName(i.getInvestorName());
//                            t.setHostelId(i.getHostelId());
//                            t.setTransactionNumber(i.getTransactionNumber());
//                            t.setTransactionDate(i.getTransactionDate());
//                            t.setIsDeleted(i.getIsDeleted());
//                            t.setCreatedAt(i.getCreatedAt());
//                            t.setCreatedBy(i.getCreatedBy());
//                            t.setUpdatedAt(i.getUpdatedAt());
//                            t.setUpdatedBy(i.getUpdatedBy());
//                            t.setPlatform(i.getPlatform());
//                            return t;
//                        })
//                        .toList();
//
//                tempTransactionsRepositories.saveAll(listTempTransactions);
//            }
//        };
//    }

//    @Bean
//    CommandLineRunner mapCashAccounts(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository) {
//        return args -> {
//            List<BankingV1> cashAccounts = bankingRepository.findAllCashAccounts();
//            if (cashAccounts != null) {
//                cashAccounts.forEach(item -> {
//                    BankingV2 bankingV2 = new BankingV2();
//                    bankingV2.setDisplayName("Cash");
//                    bankingV2.setBankName("");
//                    bankingV2.setAccountNumber("");
//                });
//            }
//        };
//    }

}