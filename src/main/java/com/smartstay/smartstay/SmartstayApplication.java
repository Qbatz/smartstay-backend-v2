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

    /**
     *
     * need to execute on production.
     *
     */
//    @Bean
//    CommandLineRunner backupTransactions(TempTransactionsRepositories tempTransactionsRepositories, TransactionV1Repository transactionV1Repository) {
//        return args -> {
//            List<TransactionV1> listTransactions = transactionV1Repository.findAll();
//            if (listTransactions != null) {
//                List<TempTransactions> listTempTransactions = listTransactions
//                        .stream()
//                        .map(i -> {
//                            TempTransactions tt = new TempTransactions();
//                            tt.setTransactionId(i.getTransactionId());
//                            tt.setType(i.getType());
//                            tt.setPaidAmount(i.getPaidAmount());
//                            tt.setCreatedBy(i.getCreatedBy());
//                            tt.setCreatedAt(i.getCreatedAt());
//                            tt.setStatus(i.getStatus());
//                            tt.setInvoiceId(i.getInvoiceId());
//                            tt.setHostelId(i.getHostelId());
//                            tt.setIsInvoice(i.getIsInvoice());
//                            tt.setCustomerId(i.getCustomerId());
//                            tt.setPaymentDate(i.getPaymentDate());
//                            tt.setTransactionMode(i.getTransactionMode());
//                            tt.setSource(i.getSource());
//                            tt.setTransactionReferenceId(i.getTransactionReferenceId());
//                            tt.setReceiptUrl(i.getReceiptUrl());
//                            tt.setBankId(i.getBankId());
//                            tt.setReferenceNumber(i.getReferenceNumber());
//                            tt.setPaidAt(i.getPaidAt());
//                            tt.setUpdatedBy(i.getUpdatedBy());
//
//                            return tt;
//                        })
//                        .toList();
//
//                tempTransactionsRepositories.saveAll(listTempTransactions);
//            }
//        };
//    }

//    @Bean
//    CommandLineRunner backupTempBankTransactions(TempBankTransactionsV1Repository bankTempTransactionsV1Repository, BankTransactionRepository bankTransactionRepository) {
//        return args -> {
//            List<BankTransactionsV1> listBankTransactionsV1 = bankTransactionRepository.findAll();
//            if (listBankTransactionsV1 != null) {
//                List<TempBankTransactionsV1> tempBankTransactionsV1List = listBankTransactionsV1
//                        .stream()
//                        .map(i -> {
//                            TempBankTransactionsV1 tbt = new TempBankTransactionsV1();
//                            tbt.setTransactionId(i.getTransactionId());
//                            tbt.setBankId(i.getBankId());
//                            tbt.setReferenceNumber(i.getReferenceNumber());
//                            tbt.setAmount(i.getAmount());
//                            tbt.setAccountBalance(i.getAccountBalance());
//                            tbt.setDescription(i.getDescription());
//                            tbt.setType(i.getType());
//                            tbt.setSource(i.getSource());
//                            tbt.setSourceId(i.getSourceId());
//                            tbt.setPaymentMethodId(i.getPaymentMethodId());
//                            tbt.setInvestorName(i.getInvestorName());
//                            tbt.setHostelId(i.getHostelId());
//                            tbt.setTransactionNumber(i.getTransactionNumber());
//                            tbt.setTransactionDate(i.getTransactionDate());
//                            tbt.setIsDeleted(i.getIsDeleted());
//                            tbt.setCreatedAt(i.getCreatedAt());
//                            tbt.setCreatedBy(i.getCreatedBy());
//                            tbt.setUpdatedAt(i.getUpdatedAt());
//                            tbt.setUpdatedBy(i.getUpdatedBy());
//                            tbt.setPlatform(i.getPlatform());
//                            return tbt;
//                        })
//                        .toList();
//
//                bankTempTransactionsV1Repository.saveAll(tempBankTransactionsV1List);
//            }
//        };
//    }
}