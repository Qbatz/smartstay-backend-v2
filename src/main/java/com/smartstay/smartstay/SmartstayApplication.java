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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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


    private static final String CASH_ACCOUNT_TYPE = "CASH";
    private static final String BANK_ACCOUNT_TYPE = "BANK";
    private static final String CARD_ACCOUNT_TYPE = "CARD";
    private static final String UPI_ACCOUNT_TYPE = "UPI";

    private static final String DEFAULT_CASH_ACCOUNT_TYPE = "Petty Cash";
    private static final String DEFAULT_BANK_ACCOUNT_TYPE = "Savings";
    private static final String MIGRATION_PLATFORM = "web";

    @PersistenceContext
    private EntityManager entityManager;

    private static final int REFERENCE_PAGE_SIZE = 500;

    @Bean
    CommandLineRunner migrateBankingV1ToBankingV2(BankingRepository bankingV1Repository,
                                                  BankingV2Repository bankingV2Repository,
                                                  BankingMethodsRepository bankingMethodsRepository,
                                                  BankingIdsRepository bankingIdsRepository,
                                                  PlatformTransactionManager transactionManager) {
        return args -> {
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                        migrateBankingAccounts(bankingV1Repository, bankingV2Repository,
                                bankingMethodsRepository, bankingIdsRepository));
            } catch (Exception e) {
                System.out.print("Failed => "+ e.getMessage());
            }
        };
    }

    @Bean
    CommandLineRunner migrateTransactionReferencesToBankingV2(
            TransactionV1Repository transactionV1Repository,
            BankTransactionRepository bankTransactionRepository,
            BankingIdsRepository bankingIdsRepository,
            PlatformTransactionManager transactionManager) {
        return args -> {
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                        updateTransactionReferences(transactionV1Repository, bankTransactionRepository,
                                bankingIdsRepository));
            } catch (Exception e) {
                System.out.print("Failed => "+ e.getMessage());
            }
        };
    }

    private void updateTransactionReferences(TransactionV1Repository transactionV1Repository,
                                             BankTransactionRepository bankTransactionRepository,
                                             BankingIdsRepository bankingIdsRepository) {
        BankingIdMappings mappings = readIdMappings(bankingIdsRepository);

        updateTransactionBankIds(transactionV1Repository, mappings);
        updateBankTransactionReferences(bankTransactionRepository, mappings);
    }

    private BankingIdMappings readIdMappings(BankingIdsRepository bankingIdsRepository) {
        List<BankingIds> mappings = bankingIdsRepository.findAll();

        Map<String, String> bankIdV2ByV1Id = mappings.stream()
                .filter(mapping -> mapping.getPaymentMethod() == null)
                .filter(mapping -> mapping.getBankIdV1() != null && mapping.getBankIdV2() != null)
                .collect(Collectors.toMap(BankingIds::getBankIdV1, BankingIds::getBankIdV2,
                        (first, duplicate) -> first));

        Map<String, String> methodIdV2ByV1Id = mappings.stream()
                .filter(mapping -> mapping.getPaymentMethodIdV1() != null
                        && mapping.getPaymentMethodIdV2() != null)
                .collect(Collectors.toMap(BankingIds::getPaymentMethodIdV1, BankingIds::getPaymentMethodIdV2,
                        (first, duplicate) -> first));

        return new BankingIdMappings(bankIdV2ByV1Id, methodIdV2ByV1Id);
    }

    private void updateTransactionBankIds(TransactionV1Repository transactionV1Repository,
                                          BankingIdMappings mappings) {
        for (int pageNumber = 0; ; pageNumber++) {
            Page<TransactionV1> page = transactionV1Repository.findAll(nextPage(pageNumber));
            if (page.isEmpty()) {
                return;
            }

            List<TransactionV1> changed = page.getContent().stream()
                    .filter(transaction -> mappings.bankIdV2ByV1Id().containsKey(transaction.getBankId()))
                    .toList();
            changed.forEach(transaction ->
                    transaction.setBankId(mappings.bankIdV2ByV1Id().get(transaction.getBankId())));
            transactionV1Repository.saveAll(changed);

            releasePage();
            if (!page.hasNext()) {
                return;
            }
        }
    }

    private void updateBankTransactionReferences(BankTransactionRepository bankTransactionRepository,
                                                 BankingIdMappings mappings) {
        for (int pageNumber = 0; ; pageNumber++) {
            Page<BankTransactionsV1> page = bankTransactionRepository.findAll(nextPage(pageNumber));
            if (page.isEmpty()) {
                return;
            }

            List<BankTransactionsV1> changed = page.getContent().stream()
                    .filter(row -> mappings.bankIdV2ByV1Id().containsKey(row.getBankId())
                            || mappings.methodIdV2ByV1Id().containsKey(row.getPaymentMethodId()))
                    .toList();
            changed.forEach(row -> applyMappedIds(row, mappings));
            bankTransactionRepository.saveAll(changed);

            releasePage();
            if (!page.hasNext()) {
                return;
            }
        }
    }

    private void applyMappedIds(BankTransactionsV1 row, BankingIdMappings mappings) {
        String bankIdV2 = mappings.bankIdV2ByV1Id().get(row.getBankId());
        if (bankIdV2 != null) {
            row.setBankId(bankIdV2);
        }
        String methodIdV2 = mappings.methodIdV2ByV1Id().get(row.getPaymentMethodId());
        if (methodIdV2 != null) {
            row.setPaymentMethodId(methodIdV2);
        }
    }

    private PageRequest nextPage(int pageNumber) {
        return PageRequest.of(pageNumber, REFERENCE_PAGE_SIZE, Sort.by("transactionId"));
    }

    private void releasePage() {
        entityManager.flush();
        entityManager.clear();
    }

    private record BankingIdMappings(Map<String, String> bankIdV2ByV1Id,
                                     Map<String, String> methodIdV2ByV1Id) {
    }

    private void migrateBankingAccounts(BankingRepository bankingV1Repository,
                                        BankingV2Repository bankingV2Repository,
                                        BankingMethodsRepository bankingMethodsRepository,
                                        BankingIdsRepository bankingIdsRepository) {
        MigrationMappings mappings = readExistingMappings(bankingIdsRepository);
        List<BankingV1> allAccounts = bankingV1Repository.findAll();

        int cashMigrated = migrateAccounts(allAccounts, CASH_ACCOUNT_TYPE, mappings,
                bankingV2Repository, bankingIdsRepository);
        int bankMigrated = migrateAccounts(allAccounts, BANK_ACCOUNT_TYPE, mappings,
                bankingV2Repository, bankingIdsRepository);
        int methodsMigrated = migratePaymentMethods(allAccounts, mappings,
                bankingV2Repository, bankingMethodsRepository, bankingIdsRepository);

        System.out.println("Cashs Migrated => " + cashMigrated);
        System.out.println("Banks Migrated => " + bankMigrated);
        System.out.println("paymentMethods Migrated => " + methodsMigrated);
    }


    private MigrationMappings readExistingMappings(BankingIdsRepository bankingIdsRepository) {
        List<BankingIds> existing = bankingIdsRepository.findAll();

        Set<String> migratedAccountIds = existing.stream()
                .filter(mapping -> mapping.getPaymentMethod() == null)
                .map(BankingIds::getBankIdV1)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> migratedMethodIds = existing.stream()
                .filter(mapping -> mapping.getPaymentMethod() != null)
                .map(BankingIds::getPaymentMethodIdV1)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, String> bankIdV2ByV1Id = existing.stream()
                .filter(mapping -> mapping.getPaymentMethod() == null)
                .filter(mapping -> mapping.getBankIdV1() != null && mapping.getBankIdV2() != null)
                .collect(Collectors.toMap(BankingIds::getBankIdV1, BankingIds::getBankIdV2,
                        (first, duplicate) -> first, HashMap::new));

        return new MigrationMappings(migratedAccountIds, migratedMethodIds, bankIdV2ByV1Id);
    }

    private int migrateAccounts(List<BankingV1> allAccounts, String accountType, MigrationMappings mappings,
                                BankingV2Repository bankingV2Repository,
                                BankingIdsRepository bankingIdsRepository) {
        List<BankingV1> pending = allAccounts.stream()
                .filter(v1 -> accountType.equalsIgnoreCase(trimOrNull(v1.getAccountType())))
                .filter(v1 -> !mappings.migratedAccountIds().contains(v1.getBankId()))
                .filter(v1 -> hasHostel(v1, accountType))
                .toList();

        pending.forEach(v1 -> {
            BankingV2 migrated = bankingV2Repository.save(toBankingV2(v1, accountType));
            bankingIdsRepository.save(toAccountMapping(v1, migrated, accountType));
            mappings.migratedAccountIds().add(v1.getBankId());
            mappings.bankIdV2ByV1Id().put(v1.getBankId(), migrated.getBankId());
        });

        return pending.size();
    }

    private int migratePaymentMethods(List<BankingV1> allAccounts, MigrationMappings mappings,
                                      BankingV2Repository bankingV2Repository,
                                      BankingMethodsRepository bankingMethodsRepository,
                                      BankingIdsRepository bankingIdsRepository) {
        Map<String, BankingV1> bankAccountsByNumber = indexBankAccountsByNumber(allAccounts);

        List<BankingV1> pending = allAccounts.stream()
                .filter(v1 -> toPaymentMethod(v1.getAccountType()) != null)
                .filter(v1 -> !mappings.migratedMethodIds().contains(v1.getBankId()))
                .toList();

        int migrated = 0;
        for (BankingV1 v1 : pending) {
            BankingV1 parentV1 = bankAccountsByNumber.get(accountNumberKey(v1));
            BankingV2 parentBank = resolveParentBank(parentV1, mappings, bankingV2Repository);
            if (parentBank == null) {
                continue;
            }

            PaymentMethod paymentMethod = toPaymentMethod(v1.getAccountType());
            BankingMethods method = bankingMethodsRepository.save(
                    toBankingMethod(v1, parentBank, paymentMethod));
            bankingIdsRepository.save(toMethodMapping(v1, parentV1, parentBank, method, paymentMethod));
            mappings.migratedMethodIds().add(v1.getBankId());
            migrated++;
        }

        return migrated;
    }

    private BankingV2 resolveParentBank(BankingV1 parentV1, MigrationMappings mappings,
                                        BankingV2Repository bankingV2Repository) {
        if (parentV1 == null) {
            return null;
        }
        String parentBankIdV2 = mappings.bankIdV2ByV1Id().get(parentV1.getBankId());
        return parentBankIdV2 != null ? bankingV2Repository.findById(parentBankIdV2).orElse(null) : null;
    }

    private Map<String, BankingV1> indexBankAccountsByNumber(List<BankingV1> allAccounts) {
        return allAccounts.stream()
                .filter(v1 -> BANK_ACCOUNT_TYPE.equalsIgnoreCase(trimOrNull(v1.getAccountType())))
                .filter(v1 -> accountNumberKey(v1) != null)
                .collect(Collectors.toMap(SmartstayApplication::accountNumberKey, v1 -> v1,
                        (first, duplicate) -> first));
    }

    private BankingV2 toBankingV2(BankingV1 v1, String accountType) {
        BankingV2 v2 = new BankingV2();
        v2.setHostelId(v1.getHostelId());
        v2.setUserId(v1.getUserId());
        v2.setParentId(v1.getParentId());
        v2.setAccountType(accountType);
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
        v2.setPlatform(MIGRATION_PLATFORM);
        v2.setBalance(v1.getBalance() != null ? v1.getBalance() : 0.0);
        v2.setCreatedAt(v1.getCreatedAt() != null ? v1.getCreatedAt() : new Date());

        if (CASH_ACCOUNT_TYPE.equals(accountType)) {
            v2.setDisplayName(firstNonBlank(v1.getAccountHolderName(), v1.getBankName(), "Cash"));
            v2.setCashAccountType(DEFAULT_CASH_ACCOUNT_TYPE);
            v2.setResponsiblePerson(v1.getUserId());
        } else {
            v2.setDisplayName(firstNonBlank(v1.getBankName(), v1.getAccountHolderName(), "Bank"));
            v2.setBankAccountType(DEFAULT_BANK_ACCOUNT_TYPE);
        }

        return v2;
    }

    private BankingMethods toBankingMethod(BankingV1 v1, BankingV2 parentBank, PaymentMethod paymentMethod) {
        BankingMethods method = new BankingMethods();
        method.setBank(parentBank);
        method.setPaymentMethod(paymentMethod);
        method.setUpiId(v1.getUpiId());
        method.setCardNumber(firstNonBlank(v1.getDebitCardNumber(), v1.getCreditCardNumber()));
        method.setCardHolderName(v1.getAccountHolderName());
        method.setDisplayName(firstNonBlank(v1.getUpiId(), v1.getAccountHolderName(),
                paymentMethod.getValue()));
        method.setDescription(v1.getDescription());
        method.setHostelId(v1.getHostelId());
        method.setUserId(v1.getUserId());
        method.setBalance(v1.getBalance() != null ? v1.getBalance() : 0.0);
        method.setCreatedBy(v1.getCreatedBy());
        method.setUpdatedBy(v1.getUpdatedBy());
        method.setCreatedAt(v1.getCreatedAt() != null ? v1.getCreatedAt() : new Date());
        method.setUpdatedAt(v1.getUpdatedAt());
        return method;
    }

    private BankingIds toAccountMapping(BankingV1 v1, BankingV2 migrated, String accountType) {
        BankingIds mapping = new BankingIds();
        mapping.setBankIdV1(v1.getBankId());
        mapping.setBankIdV2(migrated.getBankId());
        mapping.setBankAccountType(accountType);
        mapping.setPaymentMethodIdV1(v1.getBankId());
        mapping.setMigratedAt(new Date());
        return mapping;
    }

    private BankingIds toMethodMapping(BankingV1 v1, BankingV1 parentV1, BankingV2 parentBank,
                                       BankingMethods method, PaymentMethod paymentMethod) {
        BankingIds mapping = new BankingIds();
        mapping.setBankIdV1(parentV1.getBankId());
        mapping.setBankIdV2(parentBank.getBankId());
        mapping.setBankAccountType(BANK_ACCOUNT_TYPE);
        mapping.setPaymentMethodIdV1(v1.getBankId());
        mapping.setPaymentMethodIdV2(method.getPaymentMethodId());
        mapping.setPaymentMethod(paymentMethod.name());
        mapping.setMigratedAt(new Date());
        return mapping;
    }

    private static PaymentMethod toPaymentMethod(String v1AccountType) {
        String accountType = trimOrNull(v1AccountType);
        if (UPI_ACCOUNT_TYPE.equalsIgnoreCase(accountType)) {
            return PaymentMethod.UPI;
        }
        if (CARD_ACCOUNT_TYPE.equalsIgnoreCase(accountType)) {
            return PaymentMethod.DEBIT_CARD;
        }
        return null;
    }

    private static String accountNumberKey(BankingV1 v1) {
        String hostelId = trimOrNull(v1.getHostelId());
        String accountNumber = trimOrNull(v1.getAccountNumber());
        return (hostelId != null && accountNumber != null) ? hostelId + "|" + accountNumber : null;
    }

    private static boolean hasHostel(BankingV1 v1, String accountType) {
        if (trimOrNull(v1.getHostelId()) != null) {
            return true;
        }
        return false;
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

    private record MigrationMappings(Set<String> migratedAccountIds,
                                     Set<String> migratedMethodIds,
                                     Map<String, String> bankIdV2ByV1Id) {
    }

}
