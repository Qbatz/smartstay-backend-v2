package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.dto.kyc.KycUsage;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
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

    /**
     *
     * have to execute first
     *
     * @param bankingRepository
     * @param bankingV2Repository
     * @param userRepository
     * @param bankIdsMappingRepository
     * @return
     */
//    @Bean
//    CommandLineRunner createNewCashAccountOnBankingV2(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository) {
//        return args -> {
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.MONTH, 3);
//            cal.set(Calendar.DAY_OF_MONTH, 31);
//            System.out.println(cal.getTime());
//
//            List<BankingV1> listCashAccounts = bankingRepository.findAllCashAccounts(cal.getTime());
//            if (listCashAccounts != null) {
//                listCashAccounts.forEach(item -> {
//                    Users users = userRepository.findUserByUserId(item.getUserId());
//                    if (users != null) {
//                        BankingV2 bankingV2 = new BankingV2();
//                        bankingV2.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                        bankingV2.setBankName(null);
//                        bankingV2.setAccountNumber(null);
//                        bankingV2.setParentId(item.getParentId());
//                        bankingV2.setIfscCode(null);
//                        bankingV2.setBranchName(null);
//                        bankingV2.setAccountHolderName(null);
//                        bankingV2.setAccountType(BankAccountTypeV2.CASH.name());
//                        bankingV2.setBankAccountType(null);
//                        bankingV2.setCashAccountType(CashAccountType.PETTY_CASH.getValue());
//                        bankingV2.setResponsiblePerson(users.getUserId());
//                        bankingV2.setDescription(null);
//                        bankingV2.setUserId(users.getUserId());
//                        bankingV2.setHostelId(item.getHostelId());
//                        bankingV2.setTransactionType(BankPurpose.BOTH.name());
//                        if (item.getBalance() != null) {
//                            bankingV2.setBalance(Utils.roundOfDouble(item.getBalance()));
//                        }
//                        else {
//                            bankingV2.setBalance(0.0);
//                        }
//                        bankingV2.setActive(true);
//                        bankingV2.setDeleted(false);
//                        bankingV2.setDefaultAccount(item.isDefaultAccount());
//                        bankingV2.setCreatedBy(item.getCreatedBy());
//                        bankingV2.setUpdatedBy(item.getUpdatedBy());
//                        bankingV2.setCreatedAt(item.getCreatedAt());
//                        bankingV2.setUpdatedAt(item.getUpdatedAt());
//                        bankingV2.setPlatform(null);
//
//                        BankingV2 v2 = bankingV2Repository.save(bankingV2);
//
//                        BankIdsMapping bankIdsMapping = new BankIdsMapping();
//                        bankIdsMapping.setOldBankId(item.getBankId());
//                        bankIdsMapping.setNewBankId(v2.getBankId());
//                        bankIdsMapping.setOldBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMapping.setNewBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMappingRepository.save(bankIdsMapping);
//
//                    }
//                });
//            }
//        };
//    }

    /**
     *
     * Second migration.
     *
     * @param bankingRepository
     * @param bankingV2Repository
     * @param userRepository
     * @param bankIdsMappingRepository
     * @return
     */

//    @Bean
//    CommandLineRunner createNewCashAccountOnBankingV2SecondMigration(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository) {
//        return args -> {
//            Calendar cal1 = Calendar.getInstance();
//            cal1.set(Calendar.MONTH, 3);
//            cal1.set(Calendar.DAY_OF_MONTH, 31);
//            System.out.println(cal1.getTime());
//
//            Calendar cal2 = Calendar.getInstance();
//            cal2.set(Calendar.MONTH, 7);
//            cal2.set(Calendar.DAY_OF_MONTH, 31);
//            System.out.println(cal2.getTime());
//            System.out.println(cal1.getTime());
//
//            List<BankingV1> listCashAccounts = bankingRepository.findAllCashAccountsBetweenTwoDates(cal1.getTime(), cal2.getTime());
//            if (listCashAccounts != null) {
//                listCashAccounts.forEach(item -> {
//                    Users users = userRepository.findUserByUserId(item.getUserId());
//                    if (users != null) {
//                        BankingV2 bankingV2 = new BankingV2();
//                        bankingV2.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                        bankingV2.setBankName(null);
//                        bankingV2.setAccountNumber(null);
//                        bankingV2.setParentId(item.getParentId());
//                        bankingV2.setIfscCode(null);
//                        bankingV2.setBranchName(null);
//                        bankingV2.setAccountHolderName(null);
//                        bankingV2.setAccountType(BankAccountTypeV2.CASH.name());
//                        bankingV2.setBankAccountType(null);
//                        bankingV2.setCashAccountType(CashAccountType.PETTY_CASH.getValue());
//                        bankingV2.setResponsiblePerson(users.getUserId());
//                        bankingV2.setDescription(null);
//                        bankingV2.setUserId(users.getUserId());
//                        bankingV2.setHostelId(item.getHostelId());
//                        bankingV2.setTransactionType(BankPurpose.BOTH.name());
//                        if (item.getBalance() != null) {
//                            bankingV2.setBalance(Utils.roundOfDouble(item.getBalance()));
//                        }
//                        else {
//                            bankingV2.setBalance(0.0);
//                        }
//                        bankingV2.setActive(true);
//                        bankingV2.setDeleted(false);
//                        bankingV2.setDefaultAccount(item.isDefaultAccount());
//                        bankingV2.setCreatedBy(item.getCreatedBy());
//                        bankingV2.setUpdatedBy(item.getUpdatedBy());
//                        bankingV2.setCreatedAt(item.getCreatedAt());
//                        bankingV2.setUpdatedAt(item.getUpdatedAt());
//                        bankingV2.setPlatform(null);
//
//                        BankingV2 v2 = bankingV2Repository.save(bankingV2);
//
//                        BankIdsMapping bankIdsMapping = new BankIdsMapping();
//                        bankIdsMapping.setOldBankId(item.getBankId());
//                        bankIdsMapping.setNewBankId(v2.getBankId());
//                        bankIdsMapping.setOldBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMapping.setNewBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMappingRepository.save(bankIdsMapping);
//
//                    }
//                });
//            }
//        };
//    }

    /**
     *
     * third migration
     *
     *
     * @param bankingRepository
     * @param bankingV2Repository
     * @param userRepository
     * @param bankIdsMappingRepository
     * @return
     */

//    @Bean
//    CommandLineRunner createNewCashAccountOnBankingV2ThirdMigration(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository) {
//        return args -> {
//            Calendar cal1 = Calendar.getInstance();
//            cal1.set(Calendar.MONTH, 7);
//            cal1.set(Calendar.DAY_OF_MONTH, 31);
//            System.out.println(cal1.getTime());
//
//            //Oct 7, 2026
//            Calendar cal2 = Calendar.getInstance();
//            cal2.add(Calendar.DAY_OF_MONTH, -1);
//            System.out.println(cal2.getTime());
//            System.out.println(cal1.getTime());
//
//            List<BankingV1> listCashAccounts = bankingRepository.findAllCashAccountsBetweenTwoDates(cal1.getTime(), cal2.getTime());
//            if (listCashAccounts != null) {
//                listCashAccounts.forEach(item -> {
//                    Users users = userRepository.findUserByUserId(item.getUserId());
//                    if (users != null) {
//                        BankingV2 bankingV2 = new BankingV2();
//                        bankingV2.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                        bankingV2.setBankName(null);
//                        bankingV2.setAccountNumber(null);
//                        bankingV2.setParentId(item.getParentId());
//                        bankingV2.setIfscCode(null);
//                        bankingV2.setBranchName(null);
//                        bankingV2.setAccountHolderName(null);
//                        bankingV2.setAccountType(BankAccountTypeV2.CASH.name());
//                        bankingV2.setBankAccountType(null);
//                        bankingV2.setCashAccountType(CashAccountType.PETTY_CASH.getValue());
//                        bankingV2.setResponsiblePerson(users.getUserId());
//                        bankingV2.setDescription(null);
//                        bankingV2.setUserId(users.getUserId());
//                        bankingV2.setHostelId(item.getHostelId());
//                        bankingV2.setTransactionType(BankPurpose.BOTH.name());
//                        if (item.getBalance() != null) {
//                            bankingV2.setBalance(Utils.roundOfDouble(item.getBalance()));
//                        }
//                        else {
//                            bankingV2.setBalance(0.0);
//                        }
//                        bankingV2.setActive(true);
//                        bankingV2.setDeleted(false);
//                        bankingV2.setDefaultAccount(item.isDefaultAccount());
//                        bankingV2.setCreatedBy(item.getCreatedBy());
//                        bankingV2.setUpdatedBy(item.getUpdatedBy());
//                        bankingV2.setCreatedAt(item.getCreatedAt());
//                        bankingV2.setUpdatedAt(item.getUpdatedAt());
//                        bankingV2.setPlatform(null);
//
//                        BankingV2 v2 = bankingV2Repository.save(bankingV2);
//
//                        BankIdsMapping bankIdsMapping = new BankIdsMapping();
//                        bankIdsMapping.setOldBankId(item.getBankId());
//                        bankIdsMapping.setNewBankId(v2.getBankId());
//                        bankIdsMapping.setOldBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMapping.setNewBankAccountType(BankAccountType.CASH.name());
//                        bankIdsMappingRepository.save(bankIdsMapping);
//
//                    }
//                });
//            }
//        };
//    }

//    @Bean
//    CommandLineRunner migrateBankAccount(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository) {
//        return args -> {
//            List<BankingV1> listBanks = bankingRepository.findAllBankAccounts();
//            if (listBanks != null) {
//                listBanks.forEach(item -> {
//                    Users users = userRepository.findUserByUserId(item.getUserId());
//                    if (users != null) {
//                        BankingV2 bankingV2 = new BankingV2();
//                        bankingV2.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                        bankingV2.setBankName(item.getBankName());
//                        bankingV2.setAccountNumber(item.getAccountNumber());
//                        bankingV2.setParentId(item.getParentId());
//                        bankingV2.setIfscCode(item.getIfscCode());
//                        bankingV2.setBranchName(item.getBranchName());
//                        bankingV2.setAccountHolderName(item.getAccountHolderName());
//                        bankingV2.setAccountType(BankAccountTypeV2.BANK.name());
//                        bankingV2.setBankAccountType(item.getAccountType());
//                        bankingV2.setCashAccountType(null);
//                        bankingV2.setResponsiblePerson(users.getUserId());
//                        bankingV2.setDescription(null);
//                        bankingV2.setUserId(users.getUserId());
//                        bankingV2.setHostelId(item.getHostelId());
//                        bankingV2.setTransactionType(BankPurpose.BOTH.name());
//                        if (item.getBalance() != null) {
//                            bankingV2.setBalance(Utils.roundOfDouble(item.getBalance()));
//                        }
//                        else {
//                            bankingV2.setBalance(0.0);
//                        }
//                        bankingV2.setActive(true);
//                        bankingV2.setDeleted(false);
//                        bankingV2.setDefaultAccount(item.isDefaultAccount());
//                        bankingV2.setCreatedBy(item.getCreatedBy());
//                        bankingV2.setUpdatedBy(item.getUpdatedBy());
//                        bankingV2.setCreatedAt(item.getCreatedAt());
//                        bankingV2.setUpdatedAt(item.getUpdatedAt());
//                        bankingV2.setPlatform(null);
//
//                        BankingV2 v2 = bankingV2Repository.save(bankingV2);
//
//                        BankIdsMapping bankIdsMapping = new BankIdsMapping();
//                        bankIdsMapping.setOldBankId(item.getBankId());
//                        bankIdsMapping.setNewBankId(v2.getBankId());
//                        bankIdsMapping.setOldBankAccountType(BankAccountType.BANK.name());
//                        bankIdsMapping.setNewBankAccountType(BankAccountType.BANK.name());
//                        bankIdsMappingRepository.save(bankIdsMapping);
//
//                    }
//                });
//            }
//        };
//    }

//    @Bean
//    CommandLineRunner deleteBankingV2(BankingV2Repository bankingV2Repository) {
//        return args -> {
//            List<BankingV2> findAll = bankingV2Repository.findAll();
//            if (findAll != null) {
//                bankingV2Repository.deleteAll();
//            }
//        };
//    }


//    @Bean
//    CommandLineRunner migrateUPIAccounts(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository) {
//        return args -> {
//            List<BankingV1> findAllUpiAccounts = bankingRepository.findAllUPIAccounts();
//            if (findAllUpiAccounts != null) {
//                findAllUpiAccounts.forEach(item -> {
//                    Users users = userRepository.findUserByUserId(item.getUserId());
//                    if (users != null) {
//                        BankingV1 bankingV1 = bankingRepository.findByHostelIdAndAccountNumber(item.getHostelId(), item.getAccountNumber());
//                        if (bankingV1 != null) {
//                            BankIdsMapping bankIdsMapping = bankIdsMappingRepository.findByOldBankId(bankingV1.getBankId());
//                            if (bankIdsMapping != null) {
//                                BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankIdsMapping.getNewBankId());
//                                if (bankingV2 != null) {
//                                    BankingMethods bankingMethods = new BankingMethods();
//                                    bankingMethods.setBank(bankingV2);
//                                    bankingMethods.setPaymentMethod(PaymentMethod.UPI.name());
//                                    bankingMethods.setUpiId(item.getUpiId());
//                                    bankingMethods.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                                    bankingMethods.setDescription(null);
//                                    bankingMethods.setLinkedUpiId(null);
//                                    bankingMethods.setQrImage(null);
//                                    bankingMethods.setHostelId(item.getHostelId());
//                                    if (item.getBalance() != null) {
//                                        bankingMethods.setBalance(item.getBalance());
//                                        if (bankingV2.getBalance() != null) {
//                                            bankingV2.setBalance(bankingV2.getBalance() + item.getBalance());
//                                        }
//                                    }
//                                    else {
//                                        bankingMethods.setBalance(0.0);
//                                    }
//
//                                    bankingMethods.setCreatedAt(item.getCreatedAt());
//                                    bankingMethods.setCreatedBy(item.getCreatedBy());
//                                    bankingMethods.setUpdatedAt(item.getUpdatedAt());
//                                    bankingMethods.setUpdatedBy(item.getUpdatedBy());
//
//                                    List<BankingMethods> listBankingMethods = new ArrayList<>();
//                                    listBankingMethods.add(bankingMethods);
//
//                                    bankingV2.setBankingMethods(listBankingMethods);
//
//                                    bankingV2Repository.save(bankingV2);
//
//                                    BankIdsMapping bim = new BankIdsMapping();
//                                    bim.setOldBankId(item.getBankId());
//                                    bim.setNewBankId(bankingV2.getBankId());
//                                    bim.setOldBankAccountType(BankAccountType.UPI.name());
//                                    bim.setNewBankAccountType(BankAccountType.BANK.name());
//
//                                    bim.setOldBankPaymentType(BankAccountType.UPI.name());
//                                    bim.setNewBankPaymentType(BankAccountType.UPI.name());
//
//                                    bankIdsMappingRepository.save(bim);
//
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//        };
//    }

//    @Bean
//    CommandLineRunner mapCardBank(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository, BankingMethodsRepository bankingMethodsRepository) {
//        return args -> {
//            List<BankingV1> findCardsAccount = bankingRepository.findAllCards();
//            if (findCardsAccount != null) {
//                List<BankingV1> debitCards = findCardsAccount
//                        .stream()
//                        .filter(i -> i.getDebitCardNumber() != null && !i.getDebitCardNumber().isEmpty())
//                        .toList();
//                if (debitCards != null) {
//                    debitCards.forEach(item -> {
//                        Users users = userRepository.findUserByUserId(item.getUserId());
//                        if (users != null) {
//                            BankingV1 bankingV1 = bankingRepository.findByHostelIdAndAccountNumber(item.getHostelId(), item.getAccountNumber());
//                            if (bankingV1 != null) {
//                                BankIdsMapping bankIdsMapping = bankIdsMappingRepository.findByOldBankId(bankingV1.getBankId());
//                                if (bankIdsMapping != null) {
//                                    BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankIdsMapping.getNewBankId());
//                                    if (bankingV2 != null) {
//                                        BankingMethods bankingMethods = new BankingMethods();
//                                        bankingMethods.setBank(bankingV2);
//                                        bankingMethods.setPaymentMethod(PaymentMethod.DEBIT_CARD.name());
//                                        bankingMethods.setDisplayName(NameUtils.getFullName(users.getFirstName(), users.getLastName()));
//                                        bankingMethods.setDescription(null);
//                                        bankingMethods.setCardNetwork(null);
//                                        bankingMethods.setCardNumber(item.getDebitCardNumber());
//                                        bankingMethods.setCardHolderName(item.getAccountHolderName());
//                                        bankingMethods.setHostelId(item.getHostelId());
//                                        if (item.getBalance() != null) {
//                                            bankingMethods.setBalance(item.getBalance());
//                                            if (bankingV2.getBalance() != null) {
//                                                bankingV2.setBalance(bankingV2.getBalance() + item.getBalance());
//                                            }
//                                        }
//                                        else {
//                                            bankingMethods.setBalance(0.0);
//                                        }
//
//                                        bankingMethods.setCreatedAt(item.getCreatedAt());
//                                        bankingMethods.setCreatedBy(item.getCreatedBy());
//                                        bankingMethods.setUpdatedAt(item.getUpdatedAt());
//                                        bankingMethods.setUpdatedBy(item.getUpdatedBy());
//
//                                        bankingMethodsRepository.save(bankingMethods);
//
////                                        List<BankingMethods> listBankingMethods = bankingV2.getBankingMethods();
////                                        if (listBankingMethods == null) {
////                                            listBankingMethods = new ArrayList<>();
////                                        }
////                                        listBankingMethods.add(bankingMethods);
//
////                                        bankingV2.setBankingMethods(listBankingMethods);
//
//                                        bankingV2Repository.save(bankingV2);
//
//                                        BankIdsMapping bim = new BankIdsMapping();
//                                        bim.setOldBankId(item.getBankId());
//                                        bim.setNewBankId(bankingV2.getBankId());
//                                        bim.setOldBankAccountType(BankAccountType.CARD.name());
//                                        bim.setNewBankAccountType(BankAccountType.BANK.name());
//
//                                        bim.setOldBankPaymentType(BankAccountType.CARD.name());
//                                        bim.setNewBankPaymentType(BankAccountType.CARD.name());
//                                        bim.setOldPaymentType("CARD");
//
//                                        bankIdsMappingRepository.save(bim);
//                                    }
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        };
//    }


    @Bean
    CommandLineRunner addCreditCard(BankingRepository bankingRepository, BankingV2Repository bankingV2Repository, UserRepository userRepository, BankIdsMappingRepository bankIdsMappingRepository, BankingMethodsRepository bankingMethodsRepository) {
        return args -> {
            List<BankingV1> findCardsAccount = bankingRepository.findAllCards();
        };
    }



}