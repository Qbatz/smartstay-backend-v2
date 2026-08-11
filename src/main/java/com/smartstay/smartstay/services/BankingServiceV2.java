package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.bankings.AllPaymentMethodsMapper;
import com.smartstay.smartstay.Wrappers.bankings.BankTransactionListMapper;
import com.smartstay.smartstay.Wrappers.bankings.BankingMethodsMapper;
import com.smartstay.smartstay.Wrappers.bankings.BankingV2Mapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.QrBankType;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.UserHostel;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.BankAccountTypeV2;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.BankSource;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.CashAccountType;
import com.smartstay.smartstay.ennum.DateFilter;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.payloads.banking.AddBankingMethod;
import com.smartstay.smartstay.payloads.banking.AddMoneyV2;
import com.smartstay.smartstay.payloads.banking.MoneyTransferV2;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import com.smartstay.smartstay.repositories.BankingV2Repository;
import com.smartstay.smartstay.repositories.QrBankTypeRepository;
import com.smartstay.smartstay.responses.banking.BankOverviewResponse;
import com.smartstay.smartstay.responses.banking.BankTransactionListResponse;
import com.smartstay.smartstay.responses.banking.BankTransactionResponse;
import com.smartstay.smartstay.responses.banking.MonthOverview;
import com.smartstay.smartstay.responses.banking.MonthMetric;
import com.smartstay.smartstay.responses.banking.OverviewFilterOptions;
import com.smartstay.smartstay.responses.banking.FilterOption;
import com.smartstay.smartstay.responses.banking.TransactionFilterOptions;
import com.smartstay.smartstay.responses.banking.BankV2ListResponse;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.responses.banking.BankingMethodResponse;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;
import com.smartstay.smartstay.responses.banking.ResponsiblePersonResponse;
import com.smartstay.smartstay.responses.banking.TransferInitializeResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private QrBankTypeRepository qrBankTypeRepository;

    @Autowired
    private BankTransactionService transactionService;

    @Autowired
    private UploadFileToS3 uploadToS3;

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

        String responsiblePersonName = getUserName(bankingV2.getResponsiblePerson());
        return new ResponseEntity<>(new BankingV2Mapper().apply(bankingV2, responsiblePersonName), HttpStatus.CREATED);
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
        List<BankingV2> content = bankPage.getContent();

        List<String> personIds = content.stream()
                .map(BankingV2::getResponsiblePerson)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> personNameById = personIds.isEmpty()
                ? Collections.emptyMap()
                : usersService.findUsersByUserIds(personIds).stream()
                        .collect(Collectors.toMap(Users::getUserId, this::fullName, (a, b) -> a));

        BankingV2Mapper mapper = new BankingV2Mapper();
        List<BankV2Response> banks = content.stream()
                .map(bank -> mapper.apply(bank, bank.getResponsiblePerson() != null
                        ? personNameById.get(bank.getResponsiblePerson()) : null))
                .collect(Collectors.toList());

        BankV2ListResponse response = new BankV2ListResponse(
                bankPage.getTotalElements(),
                bankPage.getPageable().getPageNumber() + 1,
                bankPage.getTotalPages(),
                pageSize,
                banks);
        return new ResponseEntity<>(response, HttpStatus.OK);
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

        BankingV2 bank = validBankOrNull(hostelId, bankId);
        if (bank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (!isBankAccount(bank)) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_ONLY_FOR_BANK, HttpStatus.BAD_REQUEST);
        }

        PaymentMethod method = PaymentMethod.fromValue(payload.paymentMethod());
        if (method == null) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_PAYMENT_METHOD_INVALID, HttpStatus.BAD_REQUEST);
        }

        String displayName = trimToNull(payload.displayName());
        if (displayName == null) {
            return new ResponseEntity<>(Utils.BANKING_METHOD_DISPLAY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        String upiId = trimToNull(payload.upiId());
        Integer upiApp = payload.upiApp();
        String cardNumber = trimToNull(payload.cardNumber());
        Integer cardNetwork = payload.cardNetwork();
        String cardHolderName = trimToNull(payload.cardHolderName());
        String linkedUpiId = trimToNull(payload.linkedUpiId());

        Date billingCycle = null;
        switch (method) {
            case UPI -> {
                if (upiId == null) {
                    return badRequest(Utils.BANKING_METHOD_UPI_ID_REQUIRED);
                }
                if (upiApp == null) {
                    return badRequest(Utils.BANKING_METHOD_UPI_APP_REQUIRED);
                }
            }
            case CREDIT_CARD -> {
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
            case DEBIT_CARD -> {
                String cardError = validateCard(cardNumber, cardNetwork, cardHolderName);
                if (cardError != null) {
                    return badRequest(cardError);
                }
            }
            case QR_CODE -> {
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

        if (method == PaymentMethod.UPI
                && bankingMethodsRepository.existsByBank_BankIdAndHostelIdAndUpiIdIgnoreCase(bankId, hostelId, upiId)) {
            return badRequest(Utils.BANKING_METHOD_UPI_ID_EXISTS);
        }
        if ((method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD || method == PaymentMethod.QR_CODE)
                && bankingMethodsRepository.existsByBank_BankIdAndHostelIdAndCardNumber(bankId, hostelId, cardNumber)) {
            return badRequest(Utils.BANKING_METHOD_CARD_NUMBER_EXISTS);
        }

        Date now = new Date();
        BankingMethods entity = new BankingMethods();
        entity.setBank(bank);
        entity.setPaymentMethod(method);
        entity.setDisplayName(displayName);
        entity.setDescription(trimToNull(payload.description()));
        entity.setHostelId(hostelId);
        entity.setUserId(user.getUserId());
        entity.setBalance(0.0);
        entity.setCreatedBy(user.getUserId());
        entity.setUpdatedBy(user.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        switch (method) {
            case UPI -> {
                entity.setUpiId(upiId);
                entity.setUpiApp(upiApp);
            }
            case CREDIT_CARD -> {
                entity.setCardNumber(cardNumber);
                entity.setCardNetwork(cardNetwork);
                entity.setCardHolderName(cardHolderName);
                entity.setCreditLimit(payload.creditLimit());
                entity.setBillingCycle(billingCycle);
            }
            case DEBIT_CARD -> {
                entity.setCardNumber(cardNumber);
                entity.setCardNetwork(cardNetwork);
                entity.setCardHolderName(cardHolderName);
            }
            case QR_CODE -> {
                entity.setUpiApp(upiApp);
                entity.setCardNumber(cardNumber);
                entity.setLinkedUpiId(linkedUpiId);
                entity.setQrImage(uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(qrImage), QR_S3_FOLDER));
            }
        }

        BankingMethods saved = bankingMethodsRepository.save(entity);
        return new ResponseEntity<>(new BankingMethodsMapper().apply(saved), HttpStatus.CREATED);
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

        List<Integer> upiAppIds = methods.stream()
                .map(BankingMethods::getUpiApp)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> upiAppImageById = upiAppIds.isEmpty()
                ? Collections.emptyMap()
                : qrBankTypeRepository.findAllById(upiAppIds).stream()
                        .filter(qr -> qr.getImage() != null)
                        .collect(Collectors.toMap(QrBankType::getId, QrBankType::getImage, (a, b) -> a));

        BankingMethodsMapper mapper = new BankingMethodsMapper();
        List<BankingMethodResponse> response = methods.stream()
                .map(method -> mapper.apply(method, method.getUpiApp() != null
                        ? upiAppImageById.get(method.getUpiApp()) : null))
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
            Date parsedDate = Utils.convertYmdStringToDate(payload.transactionDate());
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
        String userId = user.getUserId();

        Date transactionDate;
        if (isPresent(payload.date())) {
            Date parsedDate = Utils.convertYmdStringToDate(payload.date());
            if (parsedDate == null) {
                return new ResponseEntity<>(Utils.ADD_MONEY_TRANSACTION_DATE_INVALID, HttpStatus.BAD_REQUEST);
            }
            transactionDate = isSameDay(parsedDate, now) ? now : parsedDate;
        } else {
            transactionDate = now;
        }
        String description = trimToNull(payload.notes());

        if (source.direct()) {
            applyBankDelta(source.directAccount(), -amount, now, userId);
        } else {
            applyMethodDelta(source.method(), -amount, now, userId);
            applyBankDelta(source.parentBank(), -amount, now, userId);
        }

        if (destination.direct()) {
            applyBankDelta(destination.directAccount(), amount, now, userId);
        } else {
            applyMethodDelta(destination.method(), amount, now, userId);
            applyBankDelta(destination.parentBank(), amount, now, userId);
        }

        BankTransactionsV1 sourceLatest = transactionService.getLatestTransaction(source.txnBankId(), hostelId);
        BankTransactionsV1 debit = new BankTransactionsV1();
        debit.setBankId(source.txnBankId());
        debit.setHostelId(hostelId);
        debit.setType(BankTransactionType.DEBIT.name());
        debit.setSource(BankSource.SELF_TRANSFER.name());
        debit.setAccountBalance(sourceLatest != null && sourceLatest.getAccountBalance() != null
                ? sourceLatest.getAccountBalance() - amount : source.balance() - amount);
        debit.setAmount(amount);
        debit.setTransactionDate(transactionDate);
        debit.setDescription(description);
        debit.setCreatedAt(now);
        debit.setIsDeleted(false);
        debit.setCreatedBy(userId);
        if (source.txnPaymentMethodId() != null) {
            debit.setPaymentMethodId(source.txnPaymentMethodId());
        }
        transactionService.saveTransaction(debit);

        BankTransactionsV1 destLatest = transactionService.getLatestTransaction(destination.txnBankId(), hostelId);
        BankTransactionsV1 credit = new BankTransactionsV1();
        credit.setBankId(destination.txnBankId());
        credit.setHostelId(hostelId);
        credit.setType(BankTransactionType.CREDIT.name());
        credit.setSource(BankSource.SELF_TRANSFER.name());
        credit.setAccountBalance(destLatest != null && destLatest.getAccountBalance() != null
                ? destLatest.getAccountBalance() + amount : amount);
        credit.setAmount(amount);
        credit.setTransactionDate(transactionDate);
        credit.setDescription(description);
        credit.setCreatedAt(now);
        credit.setIsDeleted(false);
        credit.setCreatedBy(userId);
        if (destination.txnPaymentMethodId() != null) {
            credit.setPaymentMethodId(destination.txnPaymentMethodId());
        }
        transactionService.saveTransaction(credit);

        usersService.addUserLog(hostelId, source.txnBankId(), ActivitySource.BANKING, ActivitySourceType.TRANSFER, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
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
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (bankId != null && validBankOrNull(hostelId, bankId) == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }

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
            startDate = Utils.convertStringToDate(fromDateParam.trim());
            Date toDate = Utils.convertStringToDate(toDateParam.trim());
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
        Map<String, BankingV2> bankById = bankIds.isEmpty()
                ? Collections.emptyMap()
                : bankingV2Repository.findAllById(bankIds).stream()
                        .collect(Collectors.toMap(BankingV2::getBankId, Function.identity(), (a, b) -> a));

        List<String> methodIds = transactions.stream()
                .map(BankTransactionsV1::getPaymentMethodId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        Map<String, BankingMethods> methodById = methodIds.isEmpty()
                ? Collections.emptyMap()
                : bankingMethodsRepository.findAllById(methodIds).stream()
                        .collect(Collectors.toMap(BankingMethods::getPaymentMethodId, Function.identity(), (a, b) -> a));

        Set<Integer> qrTypeIds = new HashSet<>();
        methodById.values().forEach(method -> {
            if (method.getCardNetwork() != null) {
                qrTypeIds.add(method.getCardNetwork());
            }
            if (method.getUpiApp() != null) {
                qrTypeIds.add(method.getUpiApp());
            }
        });
        Map<Integer, String> qrNameById = qrTypeIds.isEmpty()
                ? Collections.emptyMap()
                : qrBankTypeRepository.findAllById(qrTypeIds).stream()
                        .collect(Collectors.toMap(QrBankType::getId, QrBankType::getName, (a, b) -> a));

        Set<String> userIds = new HashSet<>();
        transactions.forEach(txn -> {
            if (isPresent(txn.getCreatedBy())) {
                userIds.add(txn.getCreatedBy());
            }
        });
        bankById.values().forEach(bank -> {
            if (isPresent(bank.getResponsiblePerson())) {
                userIds.add(bank.getResponsiblePerson());
            }
        });
        Map<String, String> userNameById = userIds.isEmpty()
                ? Collections.emptyMap()
                : usersService.findUsersByUserIds(new ArrayList<>(userIds)).stream()
                        .collect(Collectors.toMap(Users::getUserId, this::fullName, (a, b) -> a));

        BankTransactionListMapper mapper = new BankTransactionListMapper();
        List<BankTransactionResponse> items = transactions.stream().map(txn -> {
            BankingV2 bank = txn.getBankId() != null ? bankById.get(txn.getBankId()) : null;
            BankingMethods method = txn.getPaymentMethodId() != null ? methodById.get(txn.getPaymentMethodId()) : null;
            String createdByName = txn.getCreatedBy() != null ? userNameById.get(txn.getCreatedBy()) : null;
            String responsiblePersonName = (bank != null && bank.getResponsiblePerson() != null)
                    ? userNameById.get(bank.getResponsiblePerson()) : null;
            String cardNetwork = (method != null && method.getCardNetwork() != null)
                    ? qrNameById.get(method.getCardNetwork()) : null;
            String upiApp = (method != null && method.getUpiApp() != null)
                    ? qrNameById.get(method.getUpiApp()) : null;
            return mapper.map(txn, bank, method, createdByName, responsiblePersonName, cardNetwork, upiApp);
        }).collect(Collectors.toList());

        List<PaymentMethodOptionResponse> bankList = bankId == null
                ? buildBankList(hostelId) : Collections.emptyList();
        BankTransactionListResponse response = new BankTransactionListResponse(
                txnPage.getTotalElements(), pageNumber, txnPage.getTotalPages(), pageSize,
                buildTransactionFilterOptions(), bankList, items);
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

        BankingV2 bank = validBankOrNull(hostelId, bankId);
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

        PaymentMethodOptionResponse fromBank = null;
        List<PaymentMethodOptionResponse> toBanks = new ArrayList<>();

        if (sourcePaymentMethodId != null) {
            for (PaymentMethodOptionResponse option : buildAllPaymentMethods(hostelId)) {
                if (sourcePaymentMethodId.equals(option.paymentMethodId())) {
                    fromBank = option;
                } else {
                    toBanks.add(option);
                }
            }
        } else {
            for (PaymentMethodOptionResponse option : buildTransferOptions(hostelId)) {
                if (bankId.equals(option.bankId())) {
                    if (option.paymentMethodId() == null) {
                        fromBank = option;
                    }
                } else {
                    toBanks.add(option);
                }
            }
        }

        if (fromBank == null) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new TransferInitializeResponse(fromBank, toBanks), HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllPaymentMethods(String hostelId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(buildAllPaymentMethods(hostelId), HttpStatus.OK);
    }

    public List<PaymentMethodOptionResponse> buildAllPaymentMethods(String hostelId) {
        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
        List<PaymentMethodOptionResponse> response = new ArrayList<>();

        for (BankingV2 cash : ctx.cashAccounts()) {
            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById())));
        }

        for (BankingV2 bank : ctx.bankAccounts()) {
            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
            if (methods == null || methods.isEmpty()) {
                continue;
            }
            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
            for (BankingMethods method : methods) {
                response.add(mapper.bankMethod(bank, method,
                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx), personName));
            }
        }

        return response;
    }

    private List<PaymentMethodOptionResponse> buildTransferOptions(String hostelId) {
        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
        List<PaymentMethodOptionResponse> response = new ArrayList<>();

        for (BankingV2 cash : ctx.cashAccounts()) {
            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById())));
        }

        for (BankingV2 bank : ctx.bankAccounts()) {
            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
            response.add(mapper.bankAccount(bank, personName));
            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
            if (methods == null) {
                continue;
            }
            for (BankingMethods method : methods) {
                response.add(mapper.bankMethod(bank, method,
                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx), personName));
            }
        }

        return response;
    }

    public List<PaymentMethodOptionResponse> buildBankList(String hostelId) {
        PaymentMethodContext ctx = loadPaymentMethodContext(hostelId);
        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
        List<PaymentMethodOptionResponse> response = new ArrayList<>();

        List<String> accountBankIds = new ArrayList<>();
        ctx.cashAccounts().forEach(account -> accountBankIds.add(account.getBankId()));
        ctx.bankAccounts().forEach(account -> accountBankIds.add(account.getBankId()));
        Map<String, Date> lastTxnByBankId = transactionService.getLatestTransactionDates(accountBankIds);

        for (BankingV2 cash : ctx.cashAccounts()) {
            response.add(mapper.cash(cash, responsiblePersonName(cash, ctx.personById(), ctx.roleNameById()),
                    Utils.dateToTableFormat(lastTxnByBankId.get(cash.getBankId()))));
        }

        for (BankingV2 bank : ctx.bankAccounts()) {
            String personName = responsiblePersonName(bank, ctx.personById(), ctx.roleNameById());
            response.add(mapper.bankAccount(bank, personName,
                    Utils.dateToTableFormat(lastTxnByBankId.get(bank.getBankId()))));
            List<BankingMethods> methods = ctx.methodsByBankId().get(bank.getBankId());
            if (methods == null) {
                continue;
            }
            for (BankingMethods method : methods) {
                if (method.getPaymentMethod() != PaymentMethod.CREDIT_CARD) {
                    continue;
                }
                response.add(mapper.bankMethod(bank, method,
                        cardNetworkName(method, ctx), upiAppName(method, ctx), qrCardImage(method, ctx),
                        personName, creditCardDueDate(method)));
            }
        }

        return response;
    }

    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String creditCardDueDate(BankingMethods method) {
        if (method.getBillingCycle() == null) {
            return null;
        }
        int billingDay = method.getBillingCycle().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
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
        methodsByBankId.values().forEach(methods -> methods.forEach(method -> {
            if (method.getCardNetwork() != null) {
                qrTypeIds.add(method.getCardNetwork());
            }
            if (method.getUpiApp() != null) {
                qrTypeIds.add(method.getUpiApp());
            }
        }));
        Map<Integer, QrBankType> qrTypeById = qrTypeIds.isEmpty()
                ? Collections.emptyMap()
                : qrBankTypeRepository.findAllById(qrTypeIds).stream()
                        .collect(Collectors.toMap(QrBankType::getId, Function.identity()));

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

        return new PaymentMethodContext(cashAccounts, bankAccounts, methodsByBankId, qrTypeById, personById, roleNameById);
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
            Map<String, List<BankingMethods>> methodsByBankId,
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

    private String validateCard(String cardNumber, Integer cardNetwork, String cardHolderName) {
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
}
