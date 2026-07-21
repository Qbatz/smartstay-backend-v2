package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.banking.BankingV2Mapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.BankAccountTypeV2;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.CashAccountType;
import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.repositories.BankingV2Repository;
import com.smartstay.smartstay.responses.banking.BankV2ListResponse;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankingServiceV2 {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService usersService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private BankingV2Repository bankingV2Repository;

    @Transactional
    public ResponseEntity<?> addBank(String hostelId, AddBankV2 payload) {
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
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        // accountType must be BANK or CASH.
        BankAccountTypeV2 accountType = BankAccountTypeV2.fromValue(payload.accountType());
        if (accountType == null) {
            return new ResponseEntity<>(Utils.V2_ACCOUNT_TYPE_INVALID, HttpStatus.BAD_REQUEST);
        }

        String accountNo = trimToNull(payload.accountNo());

        // CASH-only fields, validated only when the account type is CASH.
        String cashAccountTypeValue = null;
        String responsiblePerson = null;

        if (accountType == BankAccountTypeV2.BANK) {
            // All bank details are mandatory for a BANK account.
            if (!allPresent(payload.holderName(), payload.bankName(), payload.displayName(),
                    payload.branchName(), accountNo, payload.ifscCode(), payload.bankAccountType())) {
                return new ResponseEntity<>(Utils.V2_BANK_DETAILS_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            if (!isValidBankAccountType(payload.bankAccountType())) {
                return new ResponseEntity<>(Utils.V2_BANK_ACCOUNT_TYPE_INVALID, HttpStatus.BAD_REQUEST);
            }
        } else if (accountType == BankAccountTypeV2.CASH) {
            if (!allPresent(payload.cashAccountType(), payload.responsiblePerson())) {
                return new ResponseEntity<>(Utils.V2_CASH_DETAILS_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            CashAccountType cashType = CashAccountType.fromValue(payload.cashAccountType());
            if (cashType == null) {
                return new ResponseEntity<>(Utils.V2_CASH_ACCOUNT_TYPE_INVALID, HttpStatus.BAD_REQUEST);
            }
            cashAccountTypeValue = cashType.getValue();
            responsiblePerson = trimToNull(payload.responsiblePerson());
        }

        // Duplicate account-number guard (within the hostel) when an account number is supplied.
        if (accountNo != null
                && bankingV2Repository.existsByHostelIdAndAccountNumberAndIsDeletedFalse(hostelId, accountNo)) {
            return new ResponseEntity<>(Utils.ACCOUNT_NO_ALREAY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        BankingV2 bank = new BankingV2();
        bank.setDisplayName(trimToNull(payload.displayName()));
        bank.setBankName(trimToNull(payload.bankName()));
        bank.setAccountNumber(accountNo);
        bank.setParentId(users.getParentId());
        bank.setIfscCode(trimToNull(payload.ifscCode()));
        bank.setBranchName(trimToNull(payload.branchName()));
        bank.setAccountHolderName(trimToNull(payload.holderName()));
        bank.setAccountType(accountType.name());
        bank.setBankAccountType(trimToNull(payload.bankAccountType()));
        bank.setCashAccountType(cashAccountTypeValue);
        bank.setResponsiblePerson(responsiblePerson);
        bank.setDescription(payload.description());
        bank.setUserId(users.getUserId());
        bank.setHostelId(hostelId);
        bank.setTransactionType(BankPurpose.BOTH.name());
        bank.setBalance(payload.openingBalance() != null ? payload.openingBalance() : 0.0);
        bank.setActive(true);
        bank.setDeleted(false);
        bank.setDefaultAccount(Boolean.TRUE.equals(payload.isDefault()));
        bank.setCreatedBy(users.getUserId());
        bank.setUpdatedBy(users.getUserId());
        bank.setCreatedAt(now);
        bank.setUpdatedAt(now);
        bank.setPlatform(authentication.getSource());

        BankingV2 bankingV2 = bankingV2Repository.save(bank);

        usersService.addUserLog(hostelId, bankingV2.getBankId(), ActivitySource.BANKING, ActivitySourceType.CREATE, users);

        return new ResponseEntity<>(new BankingV2Mapper().apply(bankingV2), HttpStatus.CREATED);
    }

    public ResponseEntity<?> getBanks(String hostelId, Integer page, Integer size) {
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

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        Page<BankingV2> bankPage = bankingV2Repository.findBanksByHostelId(hostelId, pageable);
        BankingV2Mapper mapper = new BankingV2Mapper();
        List<BankV2Response> banks = bankPage.getContent().stream().map(mapper).collect(Collectors.toList());

        BankV2ListResponse response = new BankV2ListResponse(
                bankPage.getTotalElements(),
                bankPage.getPageable().getPageNumber() + 1,
                bankPage.getTotalPages(),
                pageSize,
                banks);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String trimToNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    private boolean allPresent(String... values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidBankAccountType(String value) {
        String trimmed = value == null ? "" : value.trim();
        return "Savings".equalsIgnoreCase(trimmed) || "Current".equalsIgnoreCase(trimmed);
    }
}
