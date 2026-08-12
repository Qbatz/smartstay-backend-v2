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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

    private static final Logger MIGRATION_LOG = LoggerFactory.getLogger(SmartstayApplication.class);
    private static final String CASH_ACCOUNT_TYPE = "CASH";
    private static final String DEFAULT_CASH_ACCOUNT_TYPE = "Petty Cash";
    private static final String MIGRATION_PLATFORM = "web";

    @Bean
    CommandLineRunner migrateCashAccountsToBankingV2(BankingRepository bankingV1Repository,
                                                     BankingV2Repository bankingV2Repository,
                                                     BankingIdsRepository bankingIdsRepository,
                                                     PlatformTransactionManager transactionManager) {
        return args -> {
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    Set<String> alreadyMigrated = bankingIdsRepository.findAll()
                            .stream()
                            .map(BankingIds::getBankIdV1)
                            .collect(Collectors.toSet());

                    List<BankingV1> pendingCashAccounts = bankingV1Repository.findAll()
                            .stream()
                            .filter(v1 -> CASH_ACCOUNT_TYPE.equalsIgnoreCase(trimOrNull(v1.getAccountType())))
                            .filter(v1 -> !alreadyMigrated.contains(v1.getBankId()))
                            .filter(v1 -> trimOrNull(v1.getHostelId()) != null)
                            .toList();

                    pendingCashAccounts.forEach(v1 -> {
                        BankingV2 migrated = bankingV2Repository.save(toCashBankingV2(v1));
                        bankingIdsRepository.save(toBankingIds(v1, migrated));
                    });

                    MIGRATION_LOG.info("[bankingv2-migration] CASH accounts migrated: {}",
                            pendingCashAccounts.size());
                });
            } catch (Exception e) {
                MIGRATION_LOG.error("[bankingv2-migration] failed - {}", e.getMessage(), e);
            }
        };
    }

    private BankingIds toBankingIds(BankingV1 v1, BankingV2 migrated) {
        BankingIds mapping = new BankingIds();
        mapping.setBankIdV1(v1.getBankId());
        mapping.setBankIdV2(migrated.getBankId());
        mapping.setBankAccountType(CASH_ACCOUNT_TYPE);
        mapping.setPaymentMethodIdV1(v1.getBankId());
        mapping.setPaymentMethodIdV2(null);
        mapping.setPaymentMethod(null);
        mapping.setMigratedAt(new Date());
        return mapping;
    }


    private BankingV2 toCashBankingV2(BankingV1 v1) {
        BankingV2 v2 = new BankingV2();
        v2.setHostelId(v1.getHostelId());
        v2.setUserId(v1.getUserId());
        v2.setParentId(v1.getParentId());
        v2.setAccountType(CASH_ACCOUNT_TYPE);
        v2.setBankName(v1.getBankName());
        v2.setAccountNumber(v1.getAccountNumber());
        v2.setIfscCode(v1.getIfscCode());
        v2.setBranchName(v1.getBranchName());
        v2.setAccountHolderName(v1.getAccountHolderName());
        v2.setTransactionType(v1.getTransactionType());
        v2.setDescription(v1.getDescription());
        v2.setActive(v1.isActive());
        v2.setDeleted(v1.isDeleted());
        v2.setDefaultAccount(v1.isDefaultAccount());
        v2.setCreatedBy(v1.getCreatedBy());
        v2.setUpdatedBy(v1.getUpdatedBy());
        v2.setUpdatedAt(v1.getUpdatedAt());
        v2.setLastTransaction(v1.getLastTransaction());

        v2.setDisplayName(firstNonBlank(v1.getAccountHolderName(), v1.getBankName(), "Cash"));
        v2.setCashAccountType(DEFAULT_CASH_ACCOUNT_TYPE);
        v2.setResponsiblePerson(v1.getUserId());
        v2.setPlatform(MIGRATION_PLATFORM);
        v2.setBalance(v1.getBalance() != null ? v1.getBalance() : 0.0);
        v2.setCreatedAt(v1.getCreatedAt() != null ? v1.getCreatedAt() : new Date());

        return v2;
    }

    private static String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .map(SmartstayApplication::trimOrNull)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static String trimOrNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

}
