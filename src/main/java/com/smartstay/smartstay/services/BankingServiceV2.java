package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.assets.AssetsBankingMapper;
import com.smartstay.smartstay.Wrappers.assets.AssetsBankingMethodMapper;
import com.smartstay.smartstay.Wrappers.bankings.*;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.dto.bank.OldBankToNewBankInfo;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.payloads.banking.AddBankingMethod;
import com.smartstay.smartstay.payloads.banking.AddMoneyV2;
import com.smartstay.smartstay.payloads.banking.CreditCardPayment;
import com.smartstay.smartstay.payloads.banking.MoneyTransferV2;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import com.smartstay.smartstay.repositories.BankingV2Repository;
import com.smartstay.smartstay.repositories.QrBankTypeRepository;
import com.smartstay.smartstay.responses.assets.BankForAssets;
import com.smartstay.smartstay.responses.banking.*;
import com.smartstay.smartstay.responses.customer.CustomerData;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BankingServiceV2 {

    private static final String QR_S3_FOLDER = "BankingMethods/QR";

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

    @Autowired
    private BankingMethodsRepository bankingMethodsRepository;
    @Autowired
    private BankingMethodService bankingMethodService;

    @Autowired
    private QrBankTypeRepository qrBankTypeRepository;

    @Autowired
    private BankTransactionService transactionService;

    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    @Lazy
    private CustomersService customersService;
    @Autowired
    private UPITypes upiTypes;

    @Autowired
    @Lazy
    private InvoiceV1Service invoiceV1Service;

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
            if (payload.bankName() == null) {
                return new ResponseEntity<>(Utils.V2_BANK_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            if (payload.bankName().isEmpty()) {
                return new ResponseEntity<>(Utils.V2_BANK_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
            }

//            if (payload.accountNo() == null) {
//                return new ResponseEntity<>(Utils.V2_BANK_ACCOUNT_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
//            }
//            if (payload.accountNo().isEmpty()) {
//                return new ResponseEntity<>(Utils.V2_BANK_ACCOUNT_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
//            }

            if (trimToNull(payload.bankAccountType()) != null && !isValidBankAccountType(payload.bankAccountType())) {
                return new ResponseEntity<>(Utils.V2_BANK_ACCOUNT_TYPE_INVALID, HttpStatus.BAD_REQUEST);
            }
        } else if (accountType == BankAccountTypeV2.CASH) {
            if (!allPresent(payload.cashAccountType())) {
                return new ResponseEntity<>(Utils.V2_CASH_DETAILS_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            CashAccountType cashType = CashAccountType.fromValue(payload.cashAccountType());
            if (cashType == null) {
                return new ResponseEntity<>(Utils.V2_CASH_ACCOUNT_TYPE_INVALID, HttpStatus.BAD_REQUEST);
            }
            cashAccountTypeValue = cashType.getValue();
            responsiblePerson = trimToNull(payload.responsiblePerson());
        }
        else if (accountType == BankAccountTypeV2.CARD) {
            if (bankingMethodService.isExists(hostelId, payload.cardNumber())) {
                return new ResponseEntity<>(Utils.CARD_NO_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
        }
        else {
            return new ResponseEntity<>(Utils.INVALID_ACCOUNT_TYPE, HttpStatus.BAD_REQUEST);
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
        bank.setCardNumber(payload.cardNumber());
        if (accountType == BankAccountTypeV2.CARD) {
            bank.setLinkedBankId(payload.linkedBankId());
            bank.setBankAccountType(PaymentMethod.CREDIT_CARD.name());
        }
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
        if (accountType == BankAccountTypeV2.BANK) {
            bankingMethodService.addMethodToBankAccount(bankingV2, payload.openingBalance(), payload.accountNo(), payload.displayName(), payload.responsiblePerson(), hostelId);
        }
        else if (accountType == BankAccountTypeV2.CARD) {
            bankingMethodService.addMethodToCreditCard(bankingV2, payload.openingBalance(), payload.cardNumber(), payload.displayName(), payload.responsiblePerson(), hostelId, payload.creditLimit(), payload.billingCycle(), payload.cardNetwork());
        }

        if (payload.openingBalance() != null && payload.openingBalance() > 0) {
            double openingBalance = payload.openingBalance();
            BankTransactionsV1 transaction = new BankTransactionsV1();
            transaction.setBankId(bankingV2.getBankId());
            transaction.setHostelId(hostelId);
            transaction.setType("CREDIT");
            transaction.setSource(BankSource.DEPOSIT.name());
            transaction.setAccountBalance(openingBalance);
            transaction.setAmount(openingBalance);
            transaction.setTransactionDate(now);
            transaction.setCreatedAt(now);
            transaction.setIsDeleted(false);
            transaction.setCreatedBy(users.getUserId());
            transaction.setPlatform(authentication.getSource());
            transactionService.saveTransaction(transaction);
        }

        usersService.addUserLog(hostelId, bankingV2.getBankId(), ActivitySource.BANKING, ActivitySourceType.CREATE, users);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<?> getBanks(String hostelId) {
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

        List<BankingV2> content = bankingV2Repository.findBanksByHostelId(hostelId);

        List<String> personIds = content.stream()
                .map(BankingV2::getResponsiblePerson)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();
        List<Users> listResponsibleUsers = usersService.findUsersByUserIds(personIds);
        List<BankV2Response> listBanks = content
                .stream()
                .map(i -> new BankV2Mapper(listResponsibleUsers).apply(i))
                .toList();
        BankV2 bankV2 = new BankV2(hostelId, listBanks.size(), listBanks);

        return new ResponseEntity<>(bankV2, HttpStatus.OK);
    }

    public ResponseEntity<?> getResponsiblePersons(String hostelId) {
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

        String parentId = users.getParentId();

        List<UserHostel> userHostels = userHostelService.findAllByHostelIdAndParentId(hostelId, parentId);
        if (userHostels.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        List<String> userIds = userHostels.stream()
                .map(UserHostel::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Users> usersById = usersService.findUsersByUserIds(userIds).stream()
                .collect(Collectors.toMap(Users::getUserId, Function.identity(), (a, b) -> a));

        List<Integer> roleIds = usersById.values().stream()
                .map(Users::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> roleNameById = roleIds.isEmpty()
                ? Collections.emptyMap()
                : rolesService.findRolesByIdsAndHostelId(roleIds, hostelId).stream()
                        .collect(Collectors.toMap(RolesV1::getRoleId, RolesV1::getRoleName, (a, b) -> a));

        List<ResponsiblePersonResponse> response = userIds.stream()
                .map(usersById::get)
                .filter(Objects::nonNull)
                .map(user -> new ResponsiblePersonResponse(
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoleId(),
                        roleNameById.get(user.getRoleId()),
                        hostelId,
                        parentId))
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addBankingMethod(String hostelId, String bankId, AddBankingMethod payload,
            MultipartFile qrImage) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!isBankAccount(bank)) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_ONLY_FOR_BANK, HttpStatus.BAD_REQUEST);
        }

        if (payload.paymentMethod() == null) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_PAYMENT_METHOD_INVALID, HttpStatus.BAD_REQUEST);
        }

        String displayName = trimToNull(payload.displayName());
        if (displayName == null) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_DISPLAY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        String upiId = trimToNull(payload.upiId());
        String upiApp = payload.upiApp();
        String cardNumber = trimToNull(payload.cardNumber());
        String cardNetwork = payload.cardNetwork();
        String cardHolderName = trimToNull(payload.cardHolderName());
        String linkedUpiId = trimToNull(payload.linkedUpiId());

        Date billingCycle = null;
        switch (payload.paymentMethod()) {
            case "UPI" -> {
                if (upiId == null) {
                    return badRequest(Utils.BANKING_METHOD_UPI_ID_REQUIRED);
                }
                if (upiApp == null) {
                    return badRequest(Utils.BANKING_METHOD_UPI_APP_REQUIRED);
                }
            }
            case "CREDIT_CARD" -> {
                String cardError = validateCard(cardNumber, cardNetwork, cardHolderName);
                if (cardError != null) {
                    return badRequest(cardError);
                }
                if (isPresent(payload.billingCycle())) {
                    billingCycle = Utils.convertStringToDate(payload.billingCycle().trim());
                    if (billingCycle == null) {
                        return badRequest(Utils.BANKING_METHOD_BILLING_CYCLE_INVALID);
                    }
                }
            }
            case "DEBIT_CARD" -> {
                String cardError = validateCard(cardNumber, cardNetwork, cardHolderName);
                if (cardError != null) {
                    return badRequest(cardError);
                }
            }
            case "QR_CODE" -> {
                if (upiApp == null) {
                    return badRequest(Utils.BANKING_METHOD_UPI_APP_REQUIRED);
                }
                if (cardNumber == null) {
                    return badRequest(Utils.BANKING_METHOD_CARD_NUMBER_REQUIRED);
                }
                if (!isValidCardNumber(cardNumber)) {
                    return badRequest(Utils.BANKING_METHOD_CARD_NUMBER_INVALID);
                }
                if (linkedUpiId == null) {
                    return badRequest(Utils.BANKING_METHOD_LINKED_UPI_REQUIRED);
                }
                if (qrImage == null || qrImage.isEmpty()) {
                    return badRequest(Utils.BANKING_METHOD_QR_IMAGE_REQUIRED);
                }
                if (!isImage(qrImage)) {
                    return badRequest(Utils.BANKING_METHOD_IMAGE_INVALID);
                }
            }
        }

        if (payload.paymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())
                && bankingMethodsRepository.existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(bankId, hostelId, upiId)) {
            return badRequest(Utils.BANKING_METHOD_UPI_ID_EXISTS);
        }
        if ((payload.paymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name()) || payload.paymentMethod().equalsIgnoreCase(PaymentMethod.DEBIT_CARD.name()) || payload.paymentMethod().equalsIgnoreCase(PaymentMethod.QR_CODE.name()))
                && bankingMethodsRepository.existsByBank_BankIdAndHostelIdAndCardNumber(bankId, hostelId, cardNumber)) {
            return badRequest(Utils.BANKING_METHOD_CARD_NUMBER_EXISTS);
        }

        Date now = new Date();
        BankingMethods entity = new BankingMethods();
        entity.setBank(bank);
        entity.setPaymentMethod(payload.paymentMethod());
        entity.setDisplayName(displayName);
        entity.setDescription(trimToNull(payload.description()));
        entity.setHostelId(hostelId);
        entity.setUserId(user.getUserId());
        entity.setBalance(0.0);
        entity.setCreatedBy(user.getUserId());
        entity.setUpdatedBy(user.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        switch (payload.paymentMethod()) {
            case "UPI" -> {
                entity.setUpiId(upiId);
                entity.setUpiApp(upiApp);
                String imgQr = null;
                if (qrImage != null) {
                    imgQr = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(qrImage), QR_S3_FOLDER);
                }
                entity.setQrImage(imgQr);
            }
            case "CREDIT_CARD" -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(billingCycle);
                entity.setCardNumber(cardNumber);
                entity.setCardNetwork(cardNetwork);
                entity.setCardHolderName(cardHolderName);
                entity.setCreditLimit(payload.creditLimit());
                entity.setBillingCycle(cal.get(Calendar.DAY_OF_MONTH));
            }
            case "DEBIT_CARD" -> {
                entity.setCardNumber(cardNumber);
                entity.setCardNetwork(cardNetwork);
                entity.setCardHolderName(cardHolderName);
            }
            case "QR_CODE" -> {
                entity.setUpiApp(upiApp);
                entity.setCardNumber(cardNumber);
                entity.setLinkedUpiId(linkedUpiId);
                String imgQr = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(qrImage), QR_S3_FOLDER);
                entity.setQrImage(imgQr);
            }
        }

        BankingMethods saved = bankingMethodsRepository.save(entity);
        return new ResponseEntity<>(new BankingMethodsMapper(upiTypes).apply(saved), HttpStatus.CREATED);
    }

    public ResponseEntity<?> getBankingMethods(String hostelId, String bankId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!isBankAccount(bank)) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_ONLY_FOR_BANK, HttpStatus.BAD_REQUEST);
        }

        List<BankingMethods> methods = bankingMethodsRepository.findByBank_BankIdOrderByCreatedAtAsc(bankId);

        List<BankingMethodResponse> response = methods.stream()
                .map(method -> new BankingMethodsMapper(upiTypes).apply(method))
                .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addMoney(String hostelId, AddMoneyV2 payload) {
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
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (payload == null || payload.bankId() == null || payload.bankId().trim().isEmpty()) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (payload.amount() == null || payload.amount() <= 0) {
            return new ResponseEntity<>(Utils.ADD_MONEY_AMOUNT_INVALID, HttpStatus.BAD_REQUEST);
        }

        String bankId = payload.bankId().trim();
        double amount = payload.amount();

        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!bank.isActive()) {
            return new ResponseEntity<>(Utils.ADD_MONEY_ACCOUNT_INACTIVE, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        String userId = user.getUserId();
        String paymentMethodId = null;

        Date transactionDate;
        if (isPresent(payload.transactionDate())) {
            Date parsedDate = Utils.convertDmyStringToDate(payload.transactionDate());
            if (parsedDate == null) {
                return new ResponseEntity<>(Utils.ADD_MONEY_TRANSACTION_DATE_INVALID, HttpStatus.BAD_REQUEST);
            }
            transactionDate = isSameDay(parsedDate, now) ? now : parsedDate;
        } else {
            transactionDate = now;
        }

        if (isBankAccount(bank)) {
            String methodId = payload.paymentMethodId() != null ? payload.paymentMethodId().trim() : null;
            if (methodId != null && !methodId.isEmpty()) {
                BankingMethods method = bankingMethodsRepository.findById(methodId).orElse(null);
                if (method == null) {
                    return new ResponseEntity<>(Utils.ADD_MONEY_INVALID_PAYMENT_METHOD, HttpStatus.BAD_REQUEST);
                }
                if (method.getBank() == null || !bankId.equals(method.getBank().getBankId())) {
                    return new ResponseEntity<>(Utils.ADD_MONEY_PAYMENT_METHOD_MISMATCH, HttpStatus.BAD_REQUEST);
                }
                paymentMethodId = methodId;

                applyMethodDelta(method, amount, now, userId);
                applyBankDelta(bank, amount, now, userId);
            } else {
                applyBankDelta(bank, amount, now, userId);
            }
        } else {
            applyBankDelta(bank, amount, now, userId);
        }

        BankTransactionsV1 latest = transactionService.getLatestTransaction(bankId, hostelId);
        BankTransactionsV1 transaction = new BankTransactionsV1();
        transaction.setBankId(bankId);
        transaction.setHostelId(hostelId);
        transaction.setType("CREDIT");
        transaction.setSource(BankSource.DEPOSIT.name());
        transaction.setAccountBalance(latest != null && latest.getAccountBalance() != null
                ? latest.getAccountBalance() + amount : amount);
        transaction.setAmount(amount);
        transaction.setDescription(trimToNull(payload.description()));
        transaction.setTransactionNumber(trimToNull(payload.transactionId()));
        transaction.setTransactionDate(transactionDate);
        transaction.setCreatedAt(now);
        transaction.setIsDeleted(false);
        transaction.setCreatedBy(userId);
        transaction.setPlatform(authentication.getSource());
        transaction.setInvestorName(trimToNull(payload.investorName()));
        if (paymentMethodId != null) {
            transaction.setPaymentMethodId(paymentMethodId);
        }
        transactionService.saveTransaction(transaction);

        usersService.addUserLog(hostelId, bankId, ActivitySource.BANKING, ActivitySourceType.ADD_MONEY, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }


    @Transactional
    public ResponseEntity<?> moneyTransfer(String hostelId, MoneyTransferV2 payload) {
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
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (payload == null
                || payload.fromBankId() == null || payload.fromBankId().trim().isEmpty()
                || payload.toBankId() == null || payload.toBankId().trim().isEmpty()) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (payload.amount() == null || payload.amount() <= 0) {
            return new ResponseEntity<>(Utils.ADD_MONEY_AMOUNT_INVALID, HttpStatus.BAD_REQUEST);
        }

        String fromId = payload.fromBankId().trim();
        String toId = payload.toBankId().trim();
        double amount = payload.amount();

        if (fromId.equals(toId)) {
            return new ResponseEntity<>(Utils.TRANSFER_SAME_ACCOUNT, HttpStatus.BAD_REQUEST);
        }

        EndpointResult from = resolveEndpoint(hostelId, fromId, true);
        if (from.error() != null) {
            return new ResponseEntity<>(from.error(), HttpStatus.BAD_REQUEST);
        }
        EndpointResult to = resolveEndpoint(hostelId, toId, false);
        if (to.error() != null) {
            return new ResponseEntity<>(to.error(), HttpStatus.BAD_REQUEST);
        }
        TransferEndpoint source = from.endpoint();
        TransferEndpoint destination = to.endpoint();

        if (source.balance() < amount) {
            return new ResponseEntity<>(Utils.TRANSFER_INSUFFICIENT_BALANCE, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();

        Date transactionDate = resolveTransactionDate(payload.date(), now);
        if (transactionDate == null) {
            return new ResponseEntity<>(Utils.ADD_MONEY_TRANSACTION_DATE_INVALID, HttpStatus.BAD_REQUEST);
        }

        applyTransfer(source, destination, new TransferRequest(hostelId, amount, transactionDate,
                trimToNull(payload.notes()), null, user.getUserId(), now));

        usersService.addUserLog(hostelId, source.txnBankId(), ActivitySource.BANKING, ActivitySourceType.TRANSFER, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> creditCardPayment(String hostelId, CreditCardPayment payload) {
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
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (payload == null || !isPresent(payload.creditCardAccount()) || !isPresent(payload.paymentMethod())) {
            return badRequest(Utils.INVALID_BANK_ID);
        }
        if (payload.amount() == null || payload.amount() <= 0) {
            return badRequest(Utils.ADD_MONEY_AMOUNT_INVALID);
        }

        BankingV2 bankingV2ToAccount = bankingV2Repository.findByBankId(payload.creditCardAccount()).orElse(null);
        if (bankingV2ToAccount == null) {
            return new ResponseEntity<>(Utils.CREDIT_CARD_ACCOUNT_INVALID, HttpStatus.BAD_REQUEST);
        }
        if (!bankingV2ToAccount.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            return new ResponseEntity<>(Utils.CREDIT_CARD_ACCOUNT_INVALID, HttpStatus.BAD_REQUEST);
        }

        BankingV2 bankingV2FromAccount = bankingV2Repository.findByBankId(payload.paymentMethod()).orElse(null);
        if (bankingV2FromAccount == null) {
            return new ResponseEntity<>(Utils.V2_BANK_INVALID_PAYMENT_METHOD, HttpStatus.BAD_REQUEST);
        }

        String creditCardId = payload.creditCardAccount().trim();
        String sourceId = payload.paymentMethod().trim();
        double amount = payload.amount();

        if (creditCardId.equals(sourceId)) {
            return badRequest(Utils.TRANSFER_SAME_ACCOUNT);
        }

        EndpointResult from = resolveEndpoint(hostelId, sourceId, true);
        if (from.error() != null) {
            return badRequest(from.error());
        }

        EndpointResult to = resolveEndpoint(hostelId, creditCardId, false);
        if (to.error() != null) {
            return badRequest(to.error());
        }
        TransferEndpoint source = from.endpoint();
        TransferEndpoint creditCard = to.endpoint();

        Date now = new Date();
        Date settlementDate = resolveTransactionDate(payload.settlementDate(), now);
        if (settlementDate == null) {
            return badRequest(Utils.ADD_MONEY_TRANSACTION_DATE_INVALID);
        }

        String isDebitTransactionRecorded = transactionService.recordDebitTransactionForCCPayment(hostelId, bankingV2FromAccount, payload.paymentMethod(), amount, payload.description(), payload.transactionId(), settlementDate);
        if (isDebitTransactionRecorded == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

        boolean isCreditTransactionRecorded = transactionService.recortCreditTransactionForCCPaymeent(hostelId, bankingV2ToAccount, amount, isDebitTransactionRecorded, payload.description(), payload.transactionId(), settlementDate);

        double toAccountBalanceAmount = 0.0;
        if (bankingV2ToAccount.getBalance() != null) {
            toAccountBalanceAmount = bankingV2ToAccount.getBalance() + amount;
        }
        else {
            toAccountBalanceAmount = amount;
        }
        bankingV2ToAccount.setBalance(toAccountBalanceAmount);
        Date toAccountLastTransaction = settlementDate;
        if (bankingV2ToAccount.getLastTransaction() == null) {
            bankingV2ToAccount.setLastTransaction(toAccountLastTransaction);
            bankingV2ToAccount.setLastTransactionAmount(amount);
        }
        else {
            if (Utils.compareWithTwoDates(bankingV2ToAccount.getLastTransaction(), toAccountLastTransaction) < 0) {
                bankingV2ToAccount.setLastTransaction(toAccountLastTransaction);
                bankingV2ToAccount.setLastTransactionAmount(amount);
            }
        }
        bankingMethodService.creditCreditCardPayment(bankingV2ToAccount, amount, settlementDate);

        double fromAccountBalanceAmount = 0.0;
        if (bankingV2FromAccount.getBalance() != null) {
            fromAccountBalanceAmount = bankingV2FromAccount.getBalance() - amount;
        }
        else {
            fromAccountBalanceAmount = amount * -1;
        }
        bankingV2FromAccount.setBalance(fromAccountBalanceAmount);
        Date fromAccountLastTransaction = settlementDate;
        if (bankingV2FromAccount.getLastTransaction() == null) {
            bankingV2FromAccount.setLastTransaction(fromAccountLastTransaction);
            bankingV2FromAccount.setLastTransactionAmount(amount);
        }
        else {
            if (Utils.compareWithTwoDates(bankingV2ToAccount.getLastTransaction(), fromAccountLastTransaction) < 0) {
                bankingV2FromAccount.setLastTransaction(fromAccountLastTransaction);
                bankingV2FromAccount.setLastTransactionAmount(amount);
            }
        }
        bankingMethodService.creditDebitPayment(payload.paymentMethod(), amount, settlementDate);

        bankingV2Repository.save(bankingV2FromAccount);
        bankingV2Repository.save(bankingV2ToAccount);

        usersService.addUserLog(hostelId, creditCard.txnBankId(), ActivitySource.BANKING,
                ActivitySourceType.TRANSFER, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    private boolean isCreditCard(TransferEndpoint endpoint) {
        return !endpoint.direct() && endpoint.method().getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name());
    }

    private void applyTransfer(TransferEndpoint source, TransferEndpoint destination, TransferRequest request) {
        if (source.direct()) {
            applyBankDelta(source.directAccount(), -request.amount(), request.now(), request.userId());
        } else {
            applyMethodDelta(source.method(), -request.amount(), request.now(), request.userId());
            applyBankDelta(source.parentBank(), -request.amount(), request.now(), request.userId());
        }

        if (destination.direct()) {
            applyBankDelta(destination.directAccount(), request.amount(), request.now(), request.userId());
        } else {
            applyMethodDelta(destination.method(), request.amount(), request.now(), request.userId());
            applyBankDelta(destination.parentBank(), request.amount(), request.now(), request.userId());
        }

        BankTransactionsV1 sourceLatest = transactionService.getLatestTransaction(source.txnBankId(), request.hostelId());
        double debitBalance = sourceLatest != null && sourceLatest.getAccountBalance() != null
                ? sourceLatest.getAccountBalance() - request.amount()
                : source.balance() - request.amount();
        transactionService.saveTransaction(
                transferRow(source, BankTransactionType.DEBIT, debitBalance, request));

        BankTransactionsV1 destLatest = transactionService.getLatestTransaction(destination.txnBankId(), request.hostelId());
        double creditBalance = destLatest != null && destLatest.getAccountBalance() != null
                ? destLatest.getAccountBalance() + request.amount()
                : request.amount();
        transactionService.saveTransaction(
                transferRow(destination, BankTransactionType.CREDIT, creditBalance, request));
    }

    private BankTransactionsV1 transferRow(TransferEndpoint endpoint, BankTransactionType type,
                                           double accountBalance, TransferRequest request) {
        BankTransactionsV1 row = new BankTransactionsV1();
        row.setBankId(endpoint.txnBankId());
        row.setHostelId(request.hostelId());
        row.setType(type.name());
        row.setSource(BankSource.SELF_TRANSFER.name());
        row.setAccountBalance(accountBalance);
        row.setAmount(request.amount());
        row.setTransactionDate(request.transactionDate());
        row.setDescription(request.description());
        row.setTransactionNumber(request.transactionNumber());
        row.setCreatedAt(request.now());
        row.setIsDeleted(false);
        row.setCreatedBy(request.userId());
        if (endpoint.txnPaymentMethodId() != null) {
            row.setPaymentMethodId(endpoint.txnPaymentMethodId());
        }
        return row;
    }

    private Date resolveTransactionDate(String value, Date now) {
        if (!isPresent(value)) {
            return now;
        }
        Date parsed = Utils.convertDmyStringToDate(value);
        if (parsed == null) {
            parsed = new Date();
        }
        return isSameDay(parsed, now) ? now : Utils.convertToTimeStamp(parsed);
    }

    public boolean checkBankExist(String bankId) {
        if (bankId.startsWith("SSNBB-")) {
            String bId = bankId.replace("SSNBB-", "");
            BankingV2 bankingV2 = bankingV2Repository.findById(bId).orElse(null);
            return bankingV2 != null;
        }
       return bankingMethodService.findBankByMethodId(bankId);

    }

    public List<BankingV2> findAllBanksByHostelId(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        if (listBanks == null) {
            return new ArrayList<>();
        }

        return listBanks;
    }


    public List<BankForAssets> getAllBankForAssets(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        if (listBanks == null) {
            return new ArrayList<>();
        }
        List<BankForAssets> listBankForAssets = new ArrayList<>();
        List<BankingV2> filterOutBankAccount = listBanks
                .stream()
                .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                .toList();
        List<BankingV2> filterOtherThanBank = listBanks
                .stream()
                .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                .toList();

        if (filterOtherThanBank != null) {
            listBankForAssets.addAll(filterOtherThanBank
                    .stream()
                    .map(i -> new AssetsBankingMapper().apply(i))
                    .toList());
        }
        if (filterOutBankAccount != null) {
            filterOutBankAccount.forEach(item -> {
                if(item.getBankingMethods() != null) {
                    listBankForAssets.addAll(item.getBankingMethods()
                            .stream()
                            .map(i2 -> new AssetsBankingMethodMapper().apply(i2))
                            .toList());
                }

            });
        }

        return listBankForAssets;
    }


    public List<BankingV2> findAllByBankId(Set<String> bankIds) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByBankIds(bankIds.stream().toList());
        if (listBanks == null) {
            return null;
        }
        return listBanks;
    }

    public List<DebitsBank> getAllBanksForReturn(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        if (listBanks != null) {
            List<DebitsBank> listDebitBanks = new ArrayList<>();
            List<BankingV2> filterOutBankAccount = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            List<BankingV2> filterOtherThanBank = listBanks
                    .stream()
                    .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();

            if (filterOtherThanBank != null) {
                listDebitBanks.addAll(filterOtherThanBank
                        .stream()
                        .map(i -> new DebitBanksV2Mapper().apply(i))
                        .toList());
            }
            if (filterOutBankAccount != null) {
                filterOutBankAccount.forEach(item -> {
                    if(item.getBankingMethods() != null) {
                        listDebitBanks.addAll(item.getBankingMethods()
                                .stream()
                                .map(i2 -> new BankingMethodsMapperV2().apply(i2))
                                .toList());
                    }

                });
            }
            return listDebitBanks;
        }
        return new ArrayList<>();
    }

    public List<BookingBankInfo> getAllAccounts(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        List<BookingBankInfo> listBookingBankInfo = new ArrayList<>();
        if (listBanks != null) {
            List<BankingV2> filterOutBankAccount = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            List<BankingV2> filterOtherThanBank = listBanks
                    .stream()
                    .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            if (filterOtherThanBank != null) {
                listBookingBankInfo.addAll(filterOtherThanBank
                        .stream()
                        .map(i -> new BookingBankInfoMapper().apply(i)).toList());
            }
            if (filterOutBankAccount != null) {
                filterOutBankAccount.forEach(item -> {
                    if (item.getBankingMethods() != null) {
                        listBookingBankInfo.addAll(item.getBankingMethods()
                                .stream()
                                .map(i2 -> new BookingBankMethodsMapper().apply(i2)).toList());
                    }
                });
//                listBookingBankInfo.addAll(filterOtherThanBank
//                        .stream()
//                        .map(i -> new BookingBankMethodsMapper().apply(i)).toList());
            }


        }
        return listBookingBankInfo;
    }

    public List<RefundableBanks> initializeRefund(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        List<RefundableBanks> listBookingBankInfo = new ArrayList<>();
        if (listBanks != null) {
            List<BankingV2> filterOutBankAccount = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            List<BankingV2> filterOtherThanBank = listBanks
                    .stream()
                    .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            if (filterOtherThanBank != null) {
                listBookingBankInfo.addAll(filterOtherThanBank
                        .stream()
                        .map(i -> new RefundableBankMapper().apply(i)).toList());
            }
            if (filterOutBankAccount != null) {
                filterOutBankAccount.forEach(item -> {
                    if (item.getBankingMethods() != null) {
                        listBookingBankInfo.addAll(item.getBankingMethods()
                                .stream()
                                .map(i2 -> new RefundableBankingMethodsMapper().apply(i2)).toList());
                    }
                });
//                listBookingBankInfo.addAll(filterOtherThanBank
//                        .stream()
//                        .map(i -> new BookingBankMethodsMapper().apply(i)).toList());
            }


        }
        return listBookingBankInfo;
    }

    public List<BankInfoRecordPayments> getBankForReceivingPayments(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        List<BankInfoRecordPayments> listBookingBankInfo = new ArrayList<>();
        if (listBanks != null) {
            List<BankingV2> filterOutBankAccount = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            List<BankingV2> filterOtherThanBank = listBanks
                    .stream()
                    .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            if (filterOtherThanBank != null) {
                listBookingBankInfo.addAll(filterOtherThanBank
                        .stream()
                        .map(i -> new RecordPaymentBankMapper().apply(i)).toList());
            }
            if (filterOutBankAccount != null) {
                filterOutBankAccount.forEach(item -> {
                    if (item.getBankingMethods() != null) {
                        listBookingBankInfo.addAll(item.getBankingMethods()
                                .stream()
                                .map(i2 -> new RecordPaymentBankingMethodMapper().apply(i2)).toList());
                    }
                });
//                listBookingBankInfo.addAll(filterOtherThanBank
//                        .stream()
//                        .map(i -> new BookingBankMethodsMapper().apply(i)).toList());
            }


        }
        return listBookingBankInfo;
    }

    public OldBankToNewBankInfo getOldBankNewBankInfo(String s) {
        if (s.startsWith("SSNBB-")) {
            String bankId = s.replace("SSNBB-", "");
            BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankId).orElse(null);
            if (bankingV2 != null) {
                if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountTypeV2.CASH.name())) {

                    return new OldBankToNewBankInfo(bankingV2.getBankId(), null);
                }
                else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountTypeV2.CARD.name())) {
                    if (bankingV2.getBankAccountType().equalsIgnoreCase("CREDIT_CARD")) {
                        List<BankingMethods> paymentMethods = bankingV2
                                .getBankingMethods();
                        if (paymentMethods != null && !paymentMethods.isEmpty()) {
                            BankingMethods bm = paymentMethods.get(0);
                            return new OldBankToNewBankInfo(bankingV2.getBankId(), bm.getPaymentMethodId());
                        }
                    }
                }
            }
        }
        else {
            if (s.startsWith("SSBMB-")) {
                return bankingMethodService.BankInfo(s);
            }
        }

        return null;
    }

    public BankingV2 getBankDetails(String bankId) {
        return bankingV2Repository.findByBankId(bankId).orElse(null);
    }

    public double cancelBooking(String bankId, String paymentId, Double amount, String userId) {
        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return 0.0;
        }
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) || bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - amount);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            BankingV2 v2 = bankingV2Repository.save(bank);

            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
        else {
            bankingMethodService.updateBankInfo(bank, paymentId, amount);
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - amount);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            BankingV2 v2 = bankingV2Repository.save(bank);
            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
    }


    public List<DebitsBank> getAllBankForExpense(String hostelId) {
        List<BankingV2> listBanks = bankingV2Repository.findAllByHostelIdWithBankingMethods(hostelId);
        if (listBanks != null) {
            List<DebitsBank> listDebitBanks = new ArrayList<>();
            List<BankingV2> filterOutBankAccount = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();
            List<BankingV2> filterOtherThanBank = listBanks
                    .stream()
                    .filter(i -> !i.getAccountType().equalsIgnoreCase(BankAccountTypeV2.BANK.name()))
                    .toList();

            if (filterOtherThanBank != null) {
                listDebitBanks.addAll(filterOtherThanBank
                        .stream()
                        .map(i -> new DebitBanksV2Mapper().apply(i))
                        .toList());
            }
            if (filterOutBankAccount != null) {
                filterOutBankAccount.forEach(item -> {
                    if(item.getBankingMethods() != null) {
                        listDebitBanks.addAll(item.getBankingMethods()
                                .stream()
                                .map(i2 -> new BankingMethodsMapperV2().apply(i2))
                                .toList());
                    }

                });
            }
            return listDebitBanks;
        }
        return new ArrayList<>();
    }

    public double syncExpenseAccountBalance(String bankId, String paymentMethodId, Double amount, String userId) {
        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return 0.0;
        }
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) || bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - amount);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            BankingV2 v2 = bankingV2Repository.save(bank);
            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
        else {
            bankingMethodService.updateBankInfo(bank, paymentMethodId, amount);
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - amount);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            BankingV2 v2 = bankingV2Repository.save(bank);
            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
    }

    public double updateRetainerAmountToBank(Date paymentDate, Double amount, String bankId, String paymentMethodId) {
        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return 0.0;
        }
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) || bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current + amount);
            bank.setUpdatedAt(Utils.convertToTimeStamp(paymentDate));
            bank.setUpdatedBy(authentication.getName());
            BankingV2 v2 = bankingV2Repository.save(bank);
            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
        else {
            bankingMethodService.updateBankInfo(bank, paymentMethodId, amount);
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current + amount);
            bank.setUpdatedAt(Utils.convertToTimeStamp(paymentDate));
            bank.setUpdatedBy(authentication.getName());
            BankingV2 v2 = bankingV2Repository.save(bank);
            return v2.getBalance() == null ? 0.0 : v2.getBalance();
        }
    }

    public ResponseEntity<?> deleteBank(String hostelId, String bankId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.BAD_REQUEST);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.BAD_REQUEST);
        }

        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bankingV2 == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!bankingV2.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        bankingMethodService.deleteAllBankingMethods(bankingV2.getBankId());
        bankingV2.setDeleted(true);
        bankingV2Repository.save(bankingV2);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    public void deleteReceipt(Double paidAmount, String transactionType, String bankId, String paymentMethodId) {
        BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bankingV2 != null) {
            if (transactionType.equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
                if (bankingV2.getBalance() == null) {
                    bankingV2.setBalance(-1 * paidAmount);
                }
                else {
                    bankingV2.setBalance(bankingV2.getBalance() - paidAmount);
                }
            }

            bankingV2.setUpdatedBy(authentication.getName());
            bankingV2.setUpdatedAt(new Date());

            bankingV2Repository.save(bankingV2);

            if (paymentMethodId != null) {
                bankingMethodService.deleteReceipt(paidAmount, paymentMethodId);
            }
        }


//        if (transactionType.equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
//            bankingServiceV2.syncAccountBalance(bankId, -amount, authentication.getName());
//        }
    }

    public void updateAccountBalanceByDeleteExpense(String hostelId, String bankId, Double amount, String paymentMethodId) {
        if (paymentMethodId != null) {
            bankingMethodService.deleteExpensePayment(hostelId, paymentMethodId, amount);
        }
        BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bankingV2 != null) {
            if (bankingV2.getBalance() != null) {
                bankingV2.setBalance(bankingV2.getBalance() + amount);
            }
            else {
                bankingV2.setBalance(amount);
            }
            bankingV2Repository.save(bankingV2);
        }

    }

    public void updateAccountBalanceByDeletingAssets(String bankId, String paymentMethodId, Double amount) {
        if (paymentMethodId != null) {
            bankingMethodService.deleteAssetsPayment(paymentMethodId, amount);
        }
        BankingV2 bankingV2 = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bankingV2 != null) {
            if (bankingV2.getBalance() != null) {
                bankingV2.setBalance(bankingV2.getBalance() + amount);
            }
            else {
                bankingV2.setBalance(amount);
            }
            bankingV2Repository.save(bankingV2);
        }
    }

    private record TransferRequest(String hostelId, double amount, Date transactionDate, String description,
                                   String transactionNumber, String userId, Date now) {
    }

    private void applyBankDelta(BankingV2 bank, double delta, Date now, String userId) {
        double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
        bank.setBalance(current + delta);
        bank.setUpdatedAt(now);
        bank.setUpdatedBy(userId);
        bankingV2Repository.save(bank);
    }

    @Transactional
    public void syncAccountBalance(String bankId, double signedDelta, String userId) {
        String resolvedBankId = trimToNull(bankId);
        if (resolvedBankId == null || signedDelta == 0) {
            return;
        }
        BankingV2 bank = bankingV2Repository.findById(resolvedBankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return;
        }
        applyBankDelta(bank, signedDelta, new Date(), userId);
    }

    public void syncAccountBalance(String bankId, String bankingMethod, double signedDelta, String userId) {

        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return;
        }
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) || bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - signedDelta);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            bankingV2Repository.save(bank);
        }
        else {
            bankingMethodService.updateBankInfo(bank, bankingMethod, signedDelta);
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - signedDelta);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(userId);
            bankingV2Repository.save(bank);
        }

    }

    public Double updateBalanceForAssets(Double price, String name, String bankId, String bankMethodId) {
        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
        if (bank == null || bank.isDeleted()) {
            return null;
        }
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - price);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(authentication.getName());
            BankingV2 bankingV2 = bankingV2Repository.save(bank);
            return bankingV2.getBalance();
        }
        else {
            bankingMethodService.updateBankInfo(bank, bankMethodId, price);
            double current = bank.getBalance() != null ? bank.getBalance() : 0.0;
            bank.setBalance(current - price);
            bank.setUpdatedAt(new Date());
            bank.setUpdatedBy(authentication.getName());
            BankingV2 bankingV2 = bankingV2Repository.save(bank);
            return bankingV2.getBalance();
        }
    }

    private void applyMethodDelta(BankingMethods method, double delta, Date now, String userId) {
        double current = method.getBalance() != null ? method.getBalance() : 0.0;
        method.setBalance(current + delta);
        method.setUpdatedAt(now);
        method.setUpdatedBy(userId);
        bankingMethodsRepository.save(method);
    }

    @Transactional
    public void debitExpenseBalances(String bankId, String paymentMethodId, double amount, String userId) {
        String resolvedBankId = trimToNull(bankId);
        if (resolvedBankId == null || amount <= 0) {
            return;
        }
        Date now = new Date();
        BankingV2 bank = bankingV2Repository.findById(resolvedBankId).orElse(null);
        if (bank != null && !bank.isDeleted()) {
            applyBankDelta(bank, -amount, now, userId);
        }
        String methodId = trimToNull(paymentMethodId);
        if (methodId != null) {
            BankingMethods method = bankingMethodsRepository.findById(methodId).orElse(null);
            if (method != null && method.getBank() != null
                    && resolvedBankId.equals(method.getBank().getBankId())) {
                applyMethodDelta(method, -amount, now, userId);
            }
        }
    }

    @Transactional
    public void recordVendorExpenseDebit(String bankId, String paymentMethodId, double amount,
            String fallbackHostelId, String userId) {
        String resolvedBankId = trimToNull(bankId);
        if (resolvedBankId == null || amount <= 0) {
            return;
        }
        Date now = new Date();

        BankingV2 bank = bankingV2Repository.findById(resolvedBankId).orElse(null);
        if (bank != null && !bank.isDeleted()) {
            applyBankDelta(bank, -amount, now, userId);
        }

        String methodId = trimToNull(paymentMethodId);
        if (methodId != null) {
            BankingMethods method = bankingMethodsRepository.findById(methodId).orElse(null);
            if (method != null && method.getBank() != null
                    && resolvedBankId.equals(method.getBank().getBankId())) {
                applyMethodDelta(method, -amount, now, userId);
            }
        }

        String hostelId = bank != null ? bank.getHostelId() : fallbackHostelId;
        BankTransactionsV1 latest = transactionService.getLatestTransaction(resolvedBankId, hostelId);
        BankTransactionsV1 transaction = new BankTransactionsV1();
        transaction.setBankId(resolvedBankId);
        transaction.setHostelId(hostelId);
        transaction.setType("DEBIT");
        transaction.setSource(BankSource.EXPENSE.name());
        transaction.setAccountBalance(latest != null && latest.getAccountBalance() != null
                ? latest.getAccountBalance() - amount : -amount);
        transaction.setAmount(amount);
        transaction.setTransactionDate(now);
        transaction.setCreatedAt(now);
        transaction.setIsDeleted(false);
        transaction.setCreatedBy(userId);
        transaction.setPlatform(authentication.getSource());
        if (methodId != null) {
            transaction.setPaymentMethodId(methodId);
        }
        transactionService.saveTransaction(transaction);
    }

    private EndpointResult resolveEndpoint(String hostelId, String identifier, boolean isSource) {
        String invalid = isSource ? Utils.TRANSFER_INVALID_SOURCE : Utils.TRANSFER_INVALID_DESTINATION;

        Optional<BankingV2> bankOpt = bankingV2Repository.findById(identifier);
        if (bankOpt.isPresent()) {
            BankingV2 bank = bankOpt.get();
            if (bank.isDeleted() || !hostelId.equals(bank.getHostelId())) {
                return EndpointResult.fail(invalid);
            }
            if (!bank.isActive()) {
                return EndpointResult.fail(Utils.TRANSFER_ACCOUNT_INACTIVE);
            }
            return EndpointResult.ok(new TransferEndpoint(bank, null, null));
        }

        Optional<BankingMethods> methodOpt = bankingMethodsRepository.findById(identifier);
        if (methodOpt.isPresent()) {
            BankingMethods method = methodOpt.get();
            BankingV2 parent = method.getBank();
            if (parent == null || parent.isDeleted()
                    || !hostelId.equals(parent.getHostelId())
                    || !hostelId.equals(method.getHostelId())) {
                return EndpointResult.fail(invalid);
            }
            if (!parent.isActive()) {
                return EndpointResult.fail(Utils.TRANSFER_ACCOUNT_INACTIVE);
            }
            return EndpointResult.ok(new TransferEndpoint(null, method, parent));
        }

        return EndpointResult.fail(invalid);
    }

    private record TransferEndpoint(BankingV2 directAccount, BankingMethods method, BankingV2 parentBank) {
        boolean direct() {
            return directAccount != null;
        }

        double balance() {
            Double balance = direct() ? directAccount.getBalance() : method.getBalance();
            return balance != null ? balance : 0.0;
        }

        String txnBankId() {
            return direct() ? directAccount.getBankId() : parentBank.getBankId();
        }

        String txnPaymentMethodId() {
            return direct() ? null : method.getPaymentMethodId();
        }
    }

    private record EndpointResult(TransferEndpoint endpoint, String error) {
        static EndpointResult ok(TransferEndpoint endpoint) {
            return new EndpointResult(endpoint, null);
        }

        static EndpointResult fail(String error) {
            return new EndpointResult(null, error);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getBankOverview(String hostelId, String bankId, String dateFilterParam) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }

        DateFilter filter = DateFilter.LAST_6_MONTHS;
        if (isPresent(dateFilterParam)) {
            filter = DateFilter.fromValue(dateFilterParam);
            if (filter == null || filter == DateFilter.ALL || filter == DateFilter.CUSTOM) {
                return new ResponseEntity<>(Utils.OVERVIEW_DATE_FILTER_INVALID, HttpStatus.BAD_REQUEST);
            }
        }

        YearMonth currentMonth = YearMonth.now();
        YearMonth monthTo = currentMonth;
        YearMonth monthFrom = switch (filter) {
            case THIS_MONTH -> currentMonth;
            case LAST_3_MONTHS -> currentMonth.minusMonths(2);
            default -> currentMonth.minusMonths(5); // LAST_6_MONTHS
        };
        Date startDate = startOfMonth(monthFrom);

        List<BankTransactionsV1> transactions =
                transactionService.getOverviewTransactions(hostelId, bankId, startDate);

        Map<YearMonth, Map<String, Double>> sourceByMonth = new HashMap<>();
        Map<YearMonth, Double> netChangeByMonth = new HashMap<>();
        Map<String, Double> summaryTotals = new HashMap<>();
        BankTransactionsV1 earliest = null;
        for (BankTransactionsV1 txn : transactions) {
            if (txn.getCreatedAt() == null) {
                continue;
            }
            YearMonth ym = toYearMonth(txn.getCreatedAt());
            double amount = txn.getAmount() != null ? txn.getAmount() : 0.0;
            if (txn.getSource() != null) {
                sourceByMonth.computeIfAbsent(ym, key -> new HashMap<>()).merge(txn.getSource(), amount, Double::sum);
                summaryTotals.merge(txn.getSource(), amount, Double::sum);
            }
            netChangeByMonth.merge(ym, signedAmount(txn), Double::sum);
            if (earliest == null || txn.getCreatedAt().before(earliest.getCreatedAt())) {
                earliest = txn;
            }
        }

        double currentBalance = bank.getBalance() != null ? bank.getBalance() : 0.0;
        double openingBalance = openingBalanceFor(earliest, currentBalance);

        List<MonthOverview> monthData = new ArrayList<>();
        double runningOpening = openingBalance;
        int monthCount = 0;
        double netChangeSum = 0.0;
        String highestMonthName = null;
        double highestMonthValue = 0.0;
        String lowestMonthName = null;
        double lowestMonthValue = 0.0;
        for (YearMonth ym = monthFrom; !ym.isAfter(monthTo); ym = ym.plusMonths(1)) {
            Map<String, Double> monthSources = sourceByMonth.getOrDefault(ym, Collections.emptyMap());
            double opening = runningOpening;
            double netChange = netChangeByMonth.getOrDefault(ym, 0.0);
            double closing = opening + netChange;
            String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthData.add(new MonthOverview(
                    monthName,
                    closing,
                    opening,
                    monthSources.getOrDefault(BankSource.INVOICE.name(), 0.0),
                    monthSources.getOrDefault(BankSource.ASSETS.name(), 0.0),
                    monthSources.getOrDefault(BankSource.BOOKING_REFUND.name(), 0.0),
                    monthSources.getOrDefault(BankSource.DEPOSIT.name(), 0.0),
                    monthSources.getOrDefault(BankSource.SELF_TRANSFER.name(), 0.0),
                    monthSources.getOrDefault(BankSource.RENT_REFUND.name(), 0.0),
                    monthSources.getOrDefault(BankSource.EXPENSE.name(), 0.0)));

            netChangeSum += netChange;
            if (highestMonthName == null || netChange > highestMonthValue) {
                highestMonthValue = netChange;
                highestMonthName = monthName;
            }
            if (lowestMonthName == null || netChange < lowestMonthValue) {
                lowestMonthValue = netChange;
                lowestMonthName = monthName;
            }
            monthCount++;
            runningOpening = closing;
        }
        Double averageMonthly = monthCount > 0 ? Utils.roundOffWithTwoDigit(netChangeSum / monthCount) : 0.0;
        MonthMetric highestMonth = new MonthMetric(highestMonthName, Utils.roundOffWithTwoDigit(highestMonthValue));
        MonthMetric lowestMonth = new MonthMetric(lowestMonthName, Utils.roundOffWithTwoDigit(lowestMonthValue));

        BankOverviewResponse response = new BankOverviewResponse(
                new OverviewFilterOptions(buildOverviewDateFilterOptions()),
                currentBalance,
                openingBalance,
                summaryTotals.getOrDefault(BankSource.INVOICE.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.ASSETS.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.BOOKING_REFUND.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.DEPOSIT.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.SELF_TRANSFER.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.RENT_REFUND.name(), 0.0),
                summaryTotals.getOrDefault(BankSource.EXPENSE.name(), 0.0),
                monthData,
                averageMonthly,
                highestMonth,
                lowestMonth,
                bank.getAccountType(),
                bank.getCashAccountType(),
                bank.getDisplayName(),
                bank.getBankName(),
                bank.getAccountHolderName(),
                bank.getAccountNumber(),
                bank.getIfscCode(),
                bank.getBranchName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Date startOfMonth(YearMonth yearMonth) {
        return Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private YearMonth toYearMonth(Date date) {
        return YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private List<FilterOption> buildDateFilterOptions() {
        return Arrays.stream(DateFilter.values())
                .map(filter -> new FilterOption(toDisplayName(filter.name()), filter.name()))
                .collect(Collectors.toList());
    }

    private List<FilterOption> buildOverviewDateFilterOptions() {
        return Arrays.stream(DateFilter.values())
                .filter(filter -> filter != DateFilter.ALL && filter != DateFilter.CUSTOM)
                .map(filter -> new FilterOption(toDisplayName(filter.name()), filter.name()))
                .collect(Collectors.toList());
    }

    private double signedAmount(BankTransactionsV1 txn) {
        double amount = txn.getAmount() != null ? txn.getAmount() : 0.0;
        return BankTransactionType.CREDIT.name().equalsIgnoreCase(txn.getType()) ? amount : -amount;
    }

    private double openingBalanceFor(BankTransactionsV1 earliest, double currentBalance) {
        if (earliest == null) {
            return currentBalance;
        }
        double earliestBalance = earliest.getAccountBalance() != null ? earliest.getAccountBalance() : 0.0;
        return earliestBalance - signedAmount(earliest);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllTransactions(String hostelId, Integer page, Integer size,
            String dateFilterParam, String sourceParam, String fromDateParam, String toDateParam) {
        return listTransactions(hostelId, null, page, size, dateFilterParam, sourceParam, fromDateParam, toDateParam);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllBankTransactions(String hostelId, String bankId, Integer page, Integer size,
            String dateFilterParam, String sourceParam, String fromDateParam, String toDateParam) {
        return listTransactions(hostelId, bankId, page, size, dateFilterParam, sourceParam, fromDateParam, toDateParam);
    }

    private ResponseEntity<?> listTransactions(String hostelId, String bankId, Integer page, Integer size,
            String dateFilterParam, String sourceParam, String fromDateParam, String toDateParam) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (bankId != null && validBankOrNull(hostelId, bankId) == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }


        List<BankingV2> listBanks = bankingV2Repository.findBanksByHostelId(hostelId);
        if (listBanks == null || listBanks.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        List<BankV2List> bankingListV2 = listBanks
                .stream()
                .map(i -> new BankingV2ListMapper().apply(i))
                .toList();


        DateFilter dateFilter = DateFilter.ALL;
        if (isPresent(dateFilterParam)) {
            dateFilter = DateFilter.fromValue(dateFilterParam);
            if (dateFilter == null) {
                return new ResponseEntity<>(Utils.TRANSACTION_DATE_FILTER_INVALID, HttpStatus.BAD_REQUEST);
            }
        }
        String source = null;
        if (isPresent(sourceParam)) {
            BankSource bankSource = parseSource(sourceParam);
            if (bankSource == null) {
                return new ResponseEntity<>(Utils.TRANSACTION_SOURCE_INVALID, HttpStatus.BAD_REQUEST);
            }
            source = bankSource.name();
        }

        Date startDate;
        Date endDate = null;
        if (dateFilter == DateFilter.CUSTOM) {
            if (!isPresent(fromDateParam) || !isPresent(toDateParam)) {
                return new ResponseEntity<>(Utils.TRANSACTION_CUSTOM_DATES_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            startDate = Utils.stringToDate(fromDateParam.trim().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            Date toDate = Utils.stringToDate(toDateParam.trim().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (startDate == null || toDate == null) {
                return new ResponseEntity<>(Utils.TRANSACTION_DATE_FORMAT_INVALID, HttpStatus.BAD_REQUEST);
            }
            endDate = endOfDay(toDate);
            if (startDate.after(endDate)) {
                return new ResponseEntity<>(Utils.TRANSACTION_DATE_RANGE_INVALID, HttpStatus.BAD_REQUEST);
            }
        } else {
            startDate = startDateFor(dateFilter);
        }

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<BankTransactionsV1> txnPage =
                transactionService.getTransactions(hostelId, bankId, startDate, endDate, source, pageable);
        List<BankTransactionsV1> transactions = txnPage.getContent();

        List<String> bankIds = transactions.stream()
                .map(BankTransactionsV1::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<BankingV2> listBanksByTransactions = bankingV2Repository.findAllByBankIds(bankIds);
//        Set<Integer> qrTypeIds = new HashSet<>();
//        methodById.values().forEach(method -> {
//            if (method.getCardNetwork() != null) {
//                qrTypeIds.add(method.getCardNetwork());
//            }
//            if (method.getUpiApp() != null) {
//                qrTypeIds.add(method.getUpiApp());
//            }
//        });
//        Map<Integer, String> qrNameById = qrTypeIds.isEmpty()
//                ? Collections.emptyMap()
//                : qrBankTypeRepository.findAllById(qrTypeIds).stream()
//                        .collect(Collectors.toMap(QrBankType::getId, QrBankType::getName, (a, b) -> a));

        Set<String> userIds = new HashSet<>();

        List<Users> listUsers = usersService.findUsersByUserIds(new ArrayList<>(userIds));
        List<BankTransactionResponse> items = transactions
                .stream()
                .map(txn -> new BankTransactionListMapper(listBanksByTransactions, listUsers).apply(txn))
                .collect(Collectors.toList());

//        List<PaymentMethodOptionResponse> bankList = bankId == null
//                ? buildBankList(hostelId) : Collections.emptyList();
        BankTransactionListResponse response = new BankTransactionListResponse(
                txnPage.getTotalElements(), pageNumber,
                txnPage.getTotalPages(), pageSize,
                hostelId,
                buildTransactionFilterOptions(), null, items, bankingListV2);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Date startDateFor(DateFilter filter) {
        Calendar calendar = Calendar.getInstance();
        switch (filter) {
            case THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTime();
            }
            case LAST_3_MONTHS -> {
                calendar.add(Calendar.MONTH, -3);
                return calendar.getTime();
            }
            case LAST_6_MONTHS -> {
                calendar.add(Calendar.MONTH, -6);
                return calendar.getTime();
            }
            default -> {
                return null;
            }
        }
    }

    private Date endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private boolean isSameDay(Date a, Date b) {
        Calendar first = Calendar.getInstance();
        first.setTime(a);
        Calendar second = Calendar.getInstance();
        second.setTime(b);
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private BankSource parseSource(String value) {
        for (BankSource bankSource : BankSource.values()) {
            if (bankSource.name().equalsIgnoreCase(value.trim())) {
                return bankSource;
            }
        }
        return null;
    }

    private String fullName(Users user) {
        String name = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return name.isEmpty() ? null : name;
    }

    private TransactionFilterOptions buildTransactionFilterOptions() {
        List<FilterOption> sources = Arrays.stream(BankSource.values())
                .map(source -> new FilterOption(toDisplayName(source.name()), source.name()))
                .collect(Collectors.toList());
        return new TransactionFilterOptions(buildDateFilterOptions(), sources);
    }

    private String toDisplayName(String enumName) {
        StringBuilder label = new StringBuilder();
        for (String word : enumName.toLowerCase().split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(" ");
            }
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

    private String getUserName(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        Users person = usersService.findUserByUserId(userId);
        return person != null ? fullName(person) : null;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> initializeTransfer(String hostelId, String bankId, String paymentMethodId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BankingV2 bank = bankingV2Repository.findByBankId(bankId).orElse(null);
//        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!bank.isActive()) {
            return new ResponseEntity<>(Utils.ADD_MONEY_ACCOUNT_INACTIVE, HttpStatus.BAD_REQUEST);
        }

        String sourcePaymentMethodId = null;
        if (isBankAccount(bank)) {
            String methodId = paymentMethodId != null ? paymentMethodId.trim() : null;
            if (methodId != null && !methodId.isEmpty()) {
                BankingMethods method = bankingMethodsRepository.findById(methodId).orElse(null);
                if (method == null) {
                    return new ResponseEntity<>(Utils.ADD_MONEY_INVALID_PAYMENT_METHOD, HttpStatus.BAD_REQUEST);
                }
                if (method.getBank() == null || !bankId.equals(method.getBank().getBankId())) {
                    return new ResponseEntity<>(Utils.ADD_MONEY_PAYMENT_METHOD_MISMATCH, HttpStatus.BAD_REQUEST);
                }
                sourcePaymentMethodId = methodId;
            }
        }

        List<BankingV2> listToBanks = bankingV2Repository.findBanksByHostelIdNotByBankId(hostelId, bankId);

//        List<PaymentMethodOptionResponse> listToBanks = bankingV2Repository

        List<PaymentMethodOptionResponse> toBanks = new ArrayList<>();
        if (listToBanks != null) {
            toBanks = listToBanks
                    .stream()
                    .map(i -> new BankingV2PaymentMethodResponse().apply(i))
                    .toList();
        }

        PaymentMethodOptionResponse fromBank = new BankingV2PaymentMethodResponse().apply(bank);


        TransferInitializeResponse response = new TransferInitializeResponse(hostelId, fromBank, toBanks);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllPaymentMethods(String hostelId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(buildAllPaymentMethods(hostelId), HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getCheckedInTenants(String hostelId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        List<CustomerData> tenants = customersService.getCheckedInCustomers(hostelId);

        List<String> customerIds = tenants.stream()
                .map(CustomerData::customerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, List<InvoicesList>> invoicesByCustomerId =
                invoiceV1Service.getOutstandingInvoicesByCustomerIds(hostelId, customerIds);

        List<CheckedInTenantResponse> response = tenants.stream()
                .map(tenant -> toCheckedInTenantResponse(tenant, invoicesByCustomerId))
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private CheckedInTenantResponse toCheckedInTenantResponse(CustomerData tenant,
                                                              Map<String, List<InvoicesList>> invoicesByCustomerId) {
        List<InvoicesList> listInvoices = invoicesByCustomerId.getOrDefault(
                tenant.customerId(), Collections.emptyList());

        return new CheckedInTenantResponse(
                tenant.firstName(),
                tenant.lastName(),
                tenant.fullName(),
                tenant.city(),
                tenant.state(),
                tenant.country(),
                tenant.mobile(),
                tenant.currentStatus(),
                tenant.emailId(),
                tenant.profilePic(),
                tenant.bedId(),
                tenant.floorId(),
                tenant.roomId(),
                tenant.customerId(),
                tenant.initials(),
                tenant.expectedJoiningDate(),
                tenant.actualJoining(),
                tenant.countryCode(),
                tenant.bookedAt(),
                tenant.bedName(),
                tenant.roomName(),
                tenant.floorName(),
                listInvoices);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getCreditCardInitialize(String hostelId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.BAD_REQUEST);
        }

//        Map<Boolean, List<PaymentMethodOptionResponse>> byCreditCard = buildAllPaymentMethods(hostelId)
//                .stream()
//                .collect(Collectors.partitioningBy(this::isCreditCard));

        List<BankingV2> creditCards = bankingV2Repository.findAllCreditCardByHostelId(hostelId);
        List<PaymentMethodOptionResponse> listPaymentMethods = bankingMethodService.getPaymentMethodsForCreditCard(hostelId);
        List<CreditCardInitialization> listInitializatioinBanks = null;
        if (creditCards != null) {
            listInitializatioinBanks = creditCards
                    .stream()
                    .map(i -> new BankingV2CreditCardInitializationMapper().apply(i))
                    .toList();
        }

        CreditCardInitializeResponse creditCardInitializeResponse = new CreditCardInitializeResponse(hostelId,
                listInitializatioinBanks,
                new ArrayList<>(),
                listPaymentMethods);

        return new ResponseEntity<>(creditCardInitializeResponse, HttpStatus.OK);
    }

    public List<PaymentMethodOptionResponse> buildAllPaymentMethods(String hostelId) {
//        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
//        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
//        List<PaymentMethodOptionResponse> response = new ArrayList<>();
//
//        for (BankingV2 cash : ctx.cashAccounts()) {
//            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById())));
//        }
//
//        for (BankingV2 bank : ctx.bankAccounts()) {
//            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
//            if (methods == null || methods.isEmpty()) {
//                continue;
//            }
//            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
//            for (BankingMethods method : methods) {
//                response.add(mapper.bankMethod(bank, method,
//                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx), personName));
//            }
//        }

        return new ArrayList<>();
    }

    private List<PaymentMethodOptionResponse> buildTransferOptions(String hostelId) {
//        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
//        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
//        List<PaymentMethodOptionResponse> response = new ArrayList<>();
//
//        for (BankingV2 cash : ctx.cashAccounts()) {
//            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById())));
//        }
//
//        for (BankingV2 bank : ctx.bankAccounts()) {
//            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
//            response.add(mapper.bankAccount(bank, personName));
//            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
//            if (methods == null) {
//                continue;
//            }
//            for (BankingMethods method : methods) {
//                response.add(mapper.bankMethod(bank, method,
//                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx), personName));
//            }
//        }

        return new ArrayList<>();
    }

//    public List<PaymentMethodOptionResponse> buildBankList(String hostelId) {
//        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
//        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
//        List<PaymentMethodOptionResponse> response = new ArrayList<>();
//
//        List<String> accountBankIds = new ArrayList<>();
//        ctx.cashAccounts().forEach(account -> accountBankIds.add(account.getBankId()));
//        ctx.bankAccounts().forEach(account -> accountBankIds.add(account.getBankId()));
//        Map<String, Date> lastTxnByBankId = transactionService.getLatestTransactionDates(accountBankIds);
//
//        for (BankingV2 cash : ctx.cashAccounts()) {
//            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById()),
//                    Utils.dateToTableFormat(lastTxnByBankId.get(cash.getBankId()))));
//        }
//
//        for (BankingV2 bank : ctx.bankAccounts()) {
//            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
//            response.add(mapper.bankAccount(bank, personName,
//                    Utils.dateToTableFormat(lastTxnByBankId.get(bank.getBankId()))));
//            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
//            if (methods == null) {
//                continue;
//            }
//            for (BankingMethods method : methods) {
//                if (method.getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name())) {
//                    continue;
//                }
//                response.add(mapper.bankMethod(bank, method,
//                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx),
//                        personName, creditCardDueDate(method)));
//            }
//        }
//
//        return response;
//    }

    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String creditCardDueDate(BankingMethods method) {
        if (method.getBillingCycle() == null) {
            return null;
        }
        int billingDay = method.getBillingCycle();
        LocalDate today = LocalDate.now();
        LocalDate due = today.withDayOfMonth(Math.min(billingDay, today.lengthOfMonth()));
        if (due.isBefore(today)) {
            LocalDate next = today.plusMonths(1);
            due = next.withDayOfMonth(Math.min(billingDay, next.lengthOfMonth()));
        }
        return due.format(DUE_DATE_FORMAT);
    }
    
    private PaymentMethodContext loadPaymentMethodContext(String hostelId) {
        List<BankingV2> accounts = bankingV2Repository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);

        List<BankingV2> cashAccounts = new ArrayList<>();
        List<BankingV2> bankAccounts = new ArrayList<>();
        for (BankingV2 account : accounts) {
            if (BankAccountTypeV2.CASH.name().equalsIgnoreCase(account.getAccountType())) {
                cashAccounts.add(account);
            } else if (BankAccountTypeV2.BANK.name().equalsIgnoreCase(account.getAccountType())) {
                bankAccounts.add(account);
            }
        }

        List<String> bankIds = bankAccounts.stream().map(BankingV2::getBankId).collect(Collectors.toList());
        Map<String, List<BankingMethods>> methodsByBankId = bankIds.isEmpty()
                ? Collections.emptyMap()
                : bankingMethodsRepository.findByBank_BankIdIn(bankIds).stream()
                        .collect(Collectors.groupingBy(method -> method.getBank().getBankId()));

        Set<Integer> qrTypeIds = new HashSet<>();
//        methodsByBankId.values().forEach(methods -> methods.forEach(method -> {
//            if (method.getCardNetwork() != null) {
//                qrTypeIds.add(method.getCardNetwork());
//            }
//            if (method.getUpiApp() != null) {
//                qrTypeIds.add(method.getUpiApp());
//            }
//        }));
//        Map<Integer, QrBankType> qrTypeById = qrTypeIds.isEmpty()
//                ? Collections.emptyMap()
//                : qrBankTypeRepository.findAllById(qrTypeIds).stream()
//                        .collect(Collectors.toMap(QrBankType::getId, Function.identity()));

        List<String> personIds = accounts.stream()
                .map(BankingV2::getResponsiblePerson)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        Map<String, Users> personById = personIds.isEmpty()
                ? Collections.emptyMap()
                : usersService.findUsersByUserIds(personIds).stream()
                        .collect(Collectors.toMap(Users::getUserId, Function.identity(), (a, b) -> a));
        List<Integer> roleIds = personById.values().stream()
                .map(Users::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> roleNameById = roleIds.isEmpty()
                ? Collections.emptyMap()
                : rolesService.findRolesByIdsAndHostelId(roleIds, hostelId).stream()
                        .collect(Collectors.toMap(RolesV1::getRoleId, RolesV1::getRoleName, (a, b) -> a));

        return new PaymentMethodContext(cashAccounts, bankAccounts, null, personById, roleNameById);
    }

    private String cardNetworkName(BankingMethods method, PaymentMethodContext ctx) {
        QrBankType type = method.getCardNetwork() != null ? ctx.qrTypeById().get(method.getCardNetwork()) : null;
        return type != null ? type.getName() : null;
    }

    private String upiAppName(BankingMethods method, PaymentMethodContext ctx) {
        QrBankType type = method.getUpiApp() != null ? ctx.qrTypeById().get(method.getUpiApp()) : null;
        return type != null ? type.getName() : null;
    }

    private String qrCardImage(BankingMethods method, PaymentMethodContext ctx) {
        QrBankType cardNetworkType = method.getCardNetwork() != null ? ctx.qrTypeById().get(method.getCardNetwork()) : null;
        if (cardNetworkType != null) {
            return cardNetworkType.getImage();
        }
        QrBankType upiAppType = method.getUpiApp() != null ? ctx.qrTypeById().get(method.getUpiApp()) : null;
        return upiAppType != null ? upiAppType.getImage() : null;
    }

    private record PaymentMethodContext(
            List<BankingV2> cashAccounts,
            List<BankingV2> bankAccounts,
            Map<Integer, QrBankType> qrTypeById,
            Map<String, Users> personById,
            Map<Integer, String> roleNameById) {
    }

    private String responsiblePersonName(BankingV2 bank, Map<String, Users> personById,
            Map<Integer, String> roleNameById) {
        String personId = bank.getResponsiblePerson();
        if (personId == null || personId.isEmpty()) {
            return null;
        }
        Users person = personById.get(personId);
        if (person == null) {
            return null;
        }
        String name = ((person.getFirstName() != null ? person.getFirstName() : "") + " "
                + (person.getLastName() != null ? person.getLastName() : "")).trim();
        String roleName = roleNameById.get(person.getRoleId());
        if (roleName != null && !roleName.isEmpty()) {
            return name.isEmpty() ? roleName : name + " - " + roleName;
        }
        return name.isEmpty() ? null : name;
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

    private BankingV2 validBankOrNull(String hostelId, String bankId) {
        if (bankId == null) {
            return null;
        }
        Optional<BankingV2> bankOpt = bankingV2Repository.findById(bankId);
        if (bankOpt.isEmpty()) {
            return null;
        }
        BankingV2 bank = bankOpt.get();
        if (bank.isDeleted() || !hostelId.equals(bank.getHostelId())) {
            return null;
        }
        return bank;
    }

    private boolean isBankAccount(BankingV2 bank) {
        return BankAccountTypeV2.BANK.name().equalsIgnoreCase(bank.getAccountType());
    }

    private String validateCard(String cardNumber, String cardNetwork, String cardHolderName) {
        if (cardNumber == null) {
            return Utils.BANKING_METHOD_CARD_NUMBER_REQUIRED;
        }
        if (!isValidCardNumber(cardNumber)) {
            return Utils.BANKING_METHOD_CARD_NUMBER_INVALID;
        }
        if (cardNetwork == null) {
            return Utils.BANKING_METHOD_CARD_NETWORK_REQUIRED;
        }
        if (cardHolderName == null) {
            return Utils.BANKING_METHOD_CARD_HOLDER_REQUIRED;
        }
        return null;
    }

    private Users currentUser() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return usersService.findUserByUserId(authentication.getName());
    }

    private ResponseEntity<?> badRequest(String message) {
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    private boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s", "").matches("\\d{4,}");
    }

    private boolean isImage(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    public void updateBankBalance(double paidAmount, String transactionType, String bankId, String transactionDate) {
        if (bankId.startsWith("SSNBB-")) {
            String bId = bankId.replace("SSNBB-", "");
            BankingV2 bankingV2 = bankingV2Repository.findById(bId).orElse(null);
            if (bankingV2 != null) {
                if (transactionType.equalsIgnoreCase(BankTransactionType.CREDIT.name())) {
                    if (bankingV2.getBalance() == null) {
                        bankingV2.setBalance(paidAmount);
                    }
                    else {
                        bankingV2.setBalance(paidAmount + bankingV2.getBalance());
                    }
                }
                Calendar cal = Calendar.getInstance();
                Date dt = null;
                if (transactionDate != null) {
                    dt = Utils.stringToDate(transactionDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                }
                else {
                    dt = new Date();
                }
                cal.setTime(dt);
                if (cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.HOUR) == 0) {
                    Calendar cal2 = Calendar.getInstance();
                    cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
                    cal.set(Calendar.HOUR, cal2.get(Calendar.HOUR));
                    cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
                }

                bankingV2.setUpdatedBy(authentication.getName());
                bankingV2.setUpdatedAt(cal.getTime());

                bankingV2Repository.save(bankingV2);
            }
        }
        else {
            String bId = bankingMethodService.creditAmount(paidAmount, bankId, transactionDate);

            BankingV2 bankingV2 = bankingV2Repository.findById(bId).orElse(null);
            if (bankingV2 != null) {
                if (transactionType.equalsIgnoreCase(BankTransactionType.CREDIT.name())) {
                    if (bankingV2.getBalance() == null) {
                        bankingV2.setBalance(paidAmount);
                    } else {
                        bankingV2.setBalance(paidAmount + bankingV2.getBalance());
                    }
                }
                Calendar cal = Calendar.getInstance();
                Date dt = null;
                if (transactionDate != null) {
                    dt = Utils.stringToDate(transactionDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                } else {
                    dt = new Date();
                }
                cal.setTime(dt);
                if (cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.HOUR) == 0) {
                    Calendar cal2 = Calendar.getInstance();
                    cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
                    cal.set(Calendar.HOUR, cal2.get(Calendar.HOUR));
                    cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
                }

                bankingV2.setUpdatedBy(authentication.getName());
                bankingV2.setUpdatedAt(cal.getTime());

                bankingV2Repository.save(bankingV2);
            }
        }

    }
}
