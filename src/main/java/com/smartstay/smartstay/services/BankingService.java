package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BankingListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.CardType;
import com.smartstay.smartstay.payloads.banking.AddBank;
import com.smartstay.smartstay.repositories.BankingRepository;
import com.smartstay.smartstay.responses.beds.Bank;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
    private HostelBankingService hostelBankingMapper;



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
                List<String> existingAccounts = bankingV1Repository.findBankIdsByAccountNumberAndAccountType(addBank.accountNo(), accountType);
                if (existingAccounts != null) {
                    if (hostelBankingMapper.checkBankAccountExists(existingAccounts, hostelId)) {
                        return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                }

            }
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            accountType = BankAccountType.CARD.name();
            if (addBank.cardType().equalsIgnoreCase(CardType.DEBIT.name())) {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByDebitCardAndAccountType(addBank.cardNumber(), accountType);
                if (existingAccounts != null) {
                    if (hostelBankingMapper.checkBankAccountExists(existingAccounts, hostelId)) {
                        return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                }
            }
            else {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByCreditCardAndAccountType(addBank.cardNumber(), accountType);
                if (existingAccounts != null) {
                    if (hostelBankingMapper.checkBankAccountExists(existingAccounts, hostelId)) {
                        return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            accountType = BankAccountType.CASH.name();
        }
        if (addBank.accountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            accountType = BankAccountType.UPI.name();

            if (addBank.upiId() != null && !addBank.upiId().isEmpty()) {
                List<String> existingAccounts = bankingV1Repository.findBankIdsByUpiIdAndAccountType(addBank.upiId(), accountType);
                if (existingAccounts != null) {
                    if (hostelBankingMapper.checkBankAccountExists(existingAccounts, hostelId)) {
                        return  new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                }

            }

        }

        if (addBank.isDefault() != null && addBank.isDefault()) {
            List<String> listHostelBanks = hostelBankingMapper.getAllBanksAccountNoBasedOnHostel(hostelId);
            if (listHostelBanks != null) {
                BankingV1 defaultBank = bankingV1Repository.findByBankIdInAndIsDefaultAccountTrue(listHostelBanks);
                if (defaultBank != null) {
                    defaultBank.setDefaultAccount(false);
                    bankingV1Repository.save(defaultBank);
                }
            }
        }

        BankingV1 bankingV1 = new BankingV1();
        bankingV1.setBankName(addBank.bankName());
        bankingV1.setAccountNumber(addBank.accountNo());
        bankingV1.setIfscCode(addBank.ifscCode());
        bankingV1.setBranchName(addBank.branchName());
        bankingV1.setBranchCode(addBank.branchCode());
        bankingV1.setAccountHolderName(addBank.holderName());
        bankingV1.setTransactionType(BankPurpose.BOTH.name());
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

        BankingV1 v1 = bankingV1Repository.save(bankingV1);

        return hostelBankingMapper.addBankToHostel(hostelId, v1.getBankId());
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

        List<String> listMapping = hostelBankingMapper.getAllBanksAccountNoBasedOnHostel(hostelId);
        if (listMapping == null) {
            return new ResponseEntity<>(Utils.NO_ACCOUNT_NO_FOUND, HttpStatus.NO_CONTENT);
        }

        List<Bank> listBankings = bankingV1Repository.findByBankIdIn(listMapping)
                .stream()
                .map(i -> new BankingListMapper().apply(i))
                .collect(Collectors.toList());

        return new ResponseEntity<>(listBankings, HttpStatus.OK);

    }
}
