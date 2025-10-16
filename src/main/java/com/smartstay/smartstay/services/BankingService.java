package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Banking.BookingBankMapper;
import com.smartstay.smartstay.Wrappers.Banking.CashReturnMapper;
import com.smartstay.smartstay.Wrappers.BankingListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.dto.transaction.TransactionDto;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.CardType;
import com.smartstay.smartstay.payloads.banking.AddBank;
import com.smartstay.smartstay.payloads.banking.UpdateBank;
import com.smartstay.smartstay.payloads.banking.UpdateBankBalance;
import com.smartstay.smartstay.repositories.BankingRepository;
import com.smartstay.smartstay.responses.banking.BankList;
import com.smartstay.smartstay.responses.beds.Bank;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BankingService {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService usersService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private BankingRepository bankingV1Repository;


    @Autowired
    private BankTransactionService transactionService;

    public ResponseEntity<?> addNewBankAccount(String hostelId, AddBank addBank) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String accountType = null;
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            accountType = BankAccountType.BANK.name();
            if (addBank.accountNo() != null && !addBank.accountNo().isEmpty()) {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByAccountNumberAndHostelIdAndUserId(addBank.accountNo(), hostelId, authentication.getName());
                if (existingAccounts != null && !existingAccounts.isEmpty()) {
                    return new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                }

            }
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            accountType = BankAccountType.CARD.name();
            if (addBank.cardType().equalsIgnoreCase(CardType.DEBIT.name())) {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByDebitCardAndAccountType(addBank.cardNumber(), accountType, hostelId, authentication.getName());
                if (existingAccounts != null && !existingAccounts.isEmpty()) {
                        return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                }
            }
            else {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByCreditCardAndAccountType(addBank.cardNumber(), accountType, hostelId, authentication.getName());
                if (existingAccounts != null && !existingAccounts.isEmpty()) {
                    return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            boolean existsCashAccount = bankingV1Repository.existsCashAccountForHostel(hostelId, BankAccountType.CASH.name()) == 1;
            if (existsCashAccount){
                return  new ResponseEntity<>(Utils.CASH_ACCOUNT_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
            }
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            accountType = BankAccountType.UPI.name();

            if (addBank.upiId() != null && !addBank.upiId().isEmpty()) {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByUpiIdAndAccountType(addBank.upiId(), accountType, hostelId, authentication.getName());
                if (existingAccounts != null && !existingAccounts.isEmpty()) {
                    return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                }

            }

        }

        if (addBank.isDefault() != null && addBank.isDefault()) {
            BankingV1 defaultBank = bankingV1Repository.findByHostelIdAndIsDefaultAccountTrue(hostelId);
            if (defaultBank != null) {
                defaultBank.setDefaultAccount(false);
                bankingV1Repository.save(defaultBank);
            }
        }

        BankingV1 bankingV1 = new BankingV1();
        bankingV1.setBankName(addBank.bankName());
        bankingV1.setParentId(users.getParentId());
        bankingV1.setUserId(authentication.getName());
        bankingV1.setAccountNumber(addBank.accountNo());
        bankingV1.setIfscCode(addBank.ifscCode());
        bankingV1.setBranchName(addBank.branchName());
        bankingV1.setBranchCode(addBank.branchCode());
        bankingV1.setAccountHolderName(addBank.holderName());
        bankingV1.setTransactionType(BankPurpose.BOTH.name());
        bankingV1.setDescription(addBank.description());
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            bankingV1.setAccountType(BankAccountType.BANK.name());
        }
        else if (addBank.accountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankingV1.setAccountType(BankAccountType.CASH.name());
        }
        else if (addBank.accountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            bankingV1.setAccountType(BankAccountType.UPI.name());
        }
        else if (addBank.accountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            bankingV1.setAccountType(BankAccountType.CARD.name());
        }
        bankingV1.setUpiId(addBank.upiId());
        if (addBank.cardType().equalsIgnoreCase(CardType.CREDIT.name())) {
            bankingV1.setCreditCardNumber(addBank.cardNumber());
        }
        else if(addBank.cardType().equalsIgnoreCase(CardType.DEBIT.name())) {
            bankingV1.setDebitCardNumber(addBank.cardNumber());
        }
        if (addBank.isDefault() != null) {
            bankingV1.setDefaultAccount(addBank.isDefault());
        }
        bankingV1.setActive(true);
        bankingV1.setDeleted(false);
        bankingV1.setCreatedBy(users.getUserId());
        bankingV1.setCreatedAt(new Date());
        bankingV1.setHostelId(hostelId);

        BankingV1 v1 = bankingV1Repository.save(bankingV1);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public BankingV1 saveBankingData(BankingV1 bankingV1) {
        return bankingV1Repository.save(bankingV1);
    }

    public ResponseEntity<?> getAllBankAccounts(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<Bank> listBankings = bankingV1Repository.findByHostelId(hostelId)
                .stream()
                .map(i -> new BankingListMapper().apply(i))
                .collect(Collectors.toList());

        List<TransactionDto> transactions =  transactionService.getAllTransactions(hostelId);
        List<TransactionDto> listTransactions = new ArrayList<>();
        if (!transactions.isEmpty()) {
            listTransactions = transactions.stream()
                    .map(item -> {
                        Bank accountHolderBank = listBankings
                                .stream()
                                .filter(i -> i.bankingId().equalsIgnoreCase(item.bankId()))
                                .toList().get(0);
                        String accountHolder = accountHolderBank.accountHolderName() + "-" + accountHolderBank.accountType();
                        return new TransactionDto(item.transactionId(),
                                item.referenceNumber(),
                                item.amount(),
                                item.type(),
                                item.source(),
                                item.createdBy(),
                                item.createdAt(),
                                item.isCredit(),
                                item.bankId(),
                                accountHolder);
                    })
                    .toList();
        }

        BankList listBank = new BankList(listBankings, listTransactions);
        return new ResponseEntity<>(listBank, HttpStatus.OK);

    }

    public ResponseEntity<?>  updateBankAccount(String hostelId, String bankId, UpdateBank updateBank) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BankingV1 bankingV1 = bankingV1Repository.findBankingRecordByHostelIdAndBankId(hostelId, bankId);
        if (bankingV1 == null) {
            return new ResponseEntity<>(Utils.NO_ACCOUNT_NO_FOUND, HttpStatus.NO_CONTENT);
        }

        String accountType = updateBank.accountType() != null ? updateBank.accountType().toUpperCase() : null;

        boolean valid = switch (accountType) {
            case "BANK" -> handleBankAccountUpdate(updateBank, bankingV1, hostelId, bankId);
            case "CARD" -> handleCardAccountUpdate(updateBank, bankingV1, hostelId, bankId);
            case "CASH" -> { bankingV1.setAccountType(BankAccountType.CASH.name()); yield true; }
            case "UPI"  -> handleUpiAccountUpdate(updateBank, bankingV1, hostelId, bankId);
            default     -> false;
        };

        if (!valid) {
            return new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        if (isNotBlank(updateBank.holderName())) {
            bankingV1.setAccountHolderName(updateBank.holderName());
        }
        if (updateBank.isActive() != null) {
            bankingV1.setActive(updateBank.isActive());
        }
        if (updateBank.isDeleted() != null) {
            bankingV1.setDeleted(updateBank.isDeleted());
        }
        if (Boolean.TRUE.equals(updateBank.isDefault())) {
            resetDefaultBank(hostelId, bankId);
            bankingV1.setDefaultAccount(true);
        }

        if (isNotBlank(updateBank.description())) {
            bankingV1.setDescription(updateBank.description());
        }

        bankingV1.setUpdatedAt(new Date());
        bankingV1Repository.save(bankingV1);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?>  updateBankBalance(String hostelId, UpdateBankBalance balance) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BankingV1 bankingV1 = bankingV1Repository.findBankingRecordByHostelIdAndBankId(hostelId, balance.bankId());
        if (bankingV1 == null) {
            return new ResponseEntity<>(Utils.NO_ACCOUNT_NO_FOUND, HttpStatus.NO_CONTENT);
        }

        BankTransactionsV1 bankTransactionsV1 = transactionService.getTransaction(balance.bankId(), hostelId);
        if (bankTransactionsV1 == null) {
            return new ResponseEntity<>(Utils.NO_ACCOUNT_NO_FOUND, HttpStatus.NO_CONTENT);
        }
        if (balance.balance() != null){
            bankTransactionsV1.setAccountBalance(balance.balance());
        }
        transactionService.saveTransaction(bankTransactionsV1);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    private boolean handleBankAccountUpdate(UpdateBank updateBank, BankingV1 bankingV1, String hostelId, String bankId) {
        String accountType = BankAccountType.BANK.name();

        if (isNotBlank(updateBank.accountNo())) {
            List<String> existingAccounts = bankingV1Repository
                    .findBankIdsByAccountNumberAndAccountTypeNotEqualBankId(updateBank.accountNo(), accountType, bankId);
            if (existingAccounts !=null && !existingAccounts.isEmpty()) {
                return false;
            }

            bankingV1.setAccountNumber(updateBank.accountNo());
        }

        if (isNotBlank(updateBank.bankName()))  bankingV1.setBankName(updateBank.bankName());
        if (isNotBlank(updateBank.ifscCode()))  bankingV1.setIfscCode(updateBank.ifscCode());
        if (isNotBlank(updateBank.branchName())) bankingV1.setBranchName(updateBank.branchName());
        if (isNotBlank(updateBank.branchCode())) bankingV1.setBranchCode(updateBank.branchCode());
        return true;
    }

    private boolean handleCardAccountUpdate(UpdateBank updateBank, BankingV1 bankingV1, String hostelId, String bankId) {
        String accountType = BankAccountType.CARD.name();
        String cardType = updateBank.cardType() != null ? updateBank.cardType().toUpperCase() : "";

        if (isNotBlank(updateBank.cardNumber())) {
            List<String> existingAccounts;

            if (CardType.DEBIT.name().equals(cardType)) {
                existingAccounts = bankingV1Repository.findBankIdsByDebitCardAndAccountTypeNotEqualBankId(updateBank.cardNumber(), accountType, bankId);
            } else {
                existingAccounts = bankingV1Repository.findBankIdsByCreditCardAndAccountTypeNotEqualBankId(updateBank.cardNumber(), accountType, bankId);
            }

            if (existingAccounts != null && !existingAccounts.isEmpty()) {
                return false;
            }

            if (CardType.CREDIT.name().equals(cardType)) {
                bankingV1.setCreditCardNumber(updateBank.cardNumber());
            } else if (CardType.DEBIT.name().equals(cardType)) {
                bankingV1.setDebitCardNumber(updateBank.cardNumber());
            }
        }
        return true;
    }

    private boolean handleUpiAccountUpdate(UpdateBank updateBank, BankingV1 bankingV1, String hostelId, String bankId) {
        String accountType = BankAccountType.UPI.name();

        if (isNotBlank(updateBank.upiId())) {
            List<String> existingAccounts = bankingV1Repository
                    .findBankIdsByUpiIdAndAccountTypeNotEqualBankId(updateBank.upiId(), accountType, bankId);

            if (existingAccounts != null && !existingAccounts.isEmpty()) {
                return false;
            }
            bankingV1.setUpiId(updateBank.upiId());
        }
        return true;
    }

    private void resetDefaultBank(String hostelId, String currentBankId) {
            BankingV1 defaultBank = bankingV1Repository.findByHostelIdAndIsDefaultAccountTrue(hostelId);
            if (defaultBank != null && !defaultBank.getBankId().equals(currentBankId)) {
                defaultBank.setDefaultAccount(false);
                bankingV1Repository.save(defaultBank);
            }

    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public boolean checkBankExist(String bankId) {
        return bankingV1Repository.findByBankId(bankId) != null;
    }

    public boolean findBankingRecordByHostelIdAndBankId(String bankId,String hostelId) {
        return bankingV1Repository.findBankingRecordByHostelIdAndBankId(hostelId,bankId) != null;
    }

    public List<BookingBankInfo> getAllAccounts(String hostelId) {
        List<BankingV1> listBankAccounts = bankingV1Repository.findByHostelId(hostelId);

        List<BookingBankInfo> listBanks = listBankAccounts.stream()
                .map(item -> new BookingBankMapper().apply(item))
                .toList();
        return listBanks;
    }

    /**
     *
     * this should be used only at the time of initialize cancel booking.
     *
     * @param hostelId
     * @return
     */

    public List<DebitsBank> getAllBankForReturn(String hostelId) {
        List<BankingV1> listBankAccounts = bankingV1Repository.findByBankIdInAndActiveAccountDebit(hostelId);

        return listBankAccounts
                .stream()
                .map(item -> new CashReturnMapper().apply(item))
                .toList();

    }

    public void saveAllBankInfo(List<BankingV1> listBankings) {
        bankingV1Repository.saveAll(listBankings);
    }

    public List<String> getAllBanksFromParentId(String parentId) {
        return bankingV1Repository.findByParentId(parentId)
                .stream()
                .map(BankingV1::getBankId)
                .toList();
    }

    public List<BankingV1> findAllBanksById(Set<String> bankLists) {
        return bankingV1Repository.findByBankIdIn(bankLists.stream().toList());
    }
}
