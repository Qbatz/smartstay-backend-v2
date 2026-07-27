package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.banking.AllPaymentMethodsMapper;
import com.smartstay.smartstay.Wrappers.banking.BankingMethodsMapper;
import com.smartstay.smartstay.Wrappers.Banking.BankingV2Mapper;
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
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.payloads.banking.AddBankingMethod;
import com.smartstay.smartstay.payloads.banking.AddMoneyV2;
import com.smartstay.smartstay.payloads.banking.MoneyTransferV2;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import com.smartstay.smartstay.repositories.BankingV2Repository;
import com.smartstay.smartstay.repositories.QrBankTypeRepository;
import com.smartstay.smartstay.responses.banking.BankV2ListResponse;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.responses.banking.BankingMethodResponse;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;
import com.smartstay.smartstay.responses.banking.ResponsiblePersonResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
            transactionService.saveTransaction(transaction);
        }

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
//        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

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

        BankingMethodsMapper mapper = new BankingMethodsMapper();
        List<BankingMethodResponse> response = bankingMethodsRepository
                .findByBank_BankIdOrderByCreatedAtAsc(bankId)
                .stream()
                .map(mapper)
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

        if (isBankAccount(bank)) {
            String methodId = payload.paymentMethodId() != null ? payload.paymentMethodId().trim() : null;
            if (methodId == null || methodId.isEmpty()) {
                return new ResponseEntity<>(Utils.ADD_MONEY_PAYMENT_METHOD_REQUIRED, HttpStatus.BAD_REQUEST);
            }
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

        BankTransactionsV1 latest = transactionService.getLatestTransaction(bankId, hostelId);
        BankTransactionsV1 transaction = new BankTransactionsV1();
        transaction.setBankId(bankId);
        transaction.setHostelId(hostelId);
        transaction.setType("CREDIT");
        transaction.setSource(BankSource.DEPOSIT.name());
        transaction.setAccountBalance(latest != null && latest.getAccountBalance() != null
                ? latest.getAccountBalance() + amount : amount);
        transaction.setAmount(amount);
        transaction.setTransactionDate(now);
        transaction.setCreatedAt(now);
        transaction.setIsDeleted(false);
        transaction.setCreatedBy(userId);
        if (paymentMethodId != null) {
            transaction.setSourceId(paymentMethodId);
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


        if (source.cash()) {
            applyBankDelta(source.cashAccount(), -amount, now, userId);
        } else {
            applyMethodDelta(source.method(), -amount, now, userId);
            applyBankDelta(source.parentBank(), -amount, now, userId);
        }

        if (destination.cash()) {
            applyBankDelta(destination.cashAccount(), amount, now, userId);
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
        debit.setTransactionDate(now);
        debit.setCreatedAt(now);
        debit.setIsDeleted(false);
        debit.setCreatedBy(userId);
        if (source.txnSourceId() != null) {
            debit.setSourceId(source.txnSourceId());
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
        credit.setTransactionDate(now);
        credit.setCreatedAt(now);
        credit.setIsDeleted(false);
        credit.setCreatedBy(userId);
        if (destination.txnSourceId() != null) {
            credit.setSourceId(destination.txnSourceId());
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

    private void applyMethodDelta(BankingMethods method, double delta, Date now, String userId) {
        double current = method.getBalance() != null ? method.getBalance() : 0.0;
        method.setBalance(current + delta);
        method.setUpdatedAt(now);
        method.setUpdatedBy(userId);
        bankingMethodsRepository.save(method);
    }

    private EndpointResult resolveEndpoint(String hostelId, String identifier, boolean isSource) {
        String invalid = isSource ? Utils.TRANSFER_INVALID_SOURCE : Utils.TRANSFER_INVALID_DESTINATION;

        Optional<BankingV2> bankOpt = bankingV2Repository.findById(identifier);
        if (bankOpt.isPresent()) {
            BankingV2 bank = bankOpt.get();
            if (bank.isDeleted() || !hostelId.equals(bank.getHostelId())) {
                return EndpointResult.fail(invalid);
            }
            if (BankAccountTypeV2.BANK.name().equalsIgnoreCase(bank.getAccountType())) {
                return EndpointResult.fail(Utils.TRANSFER_BANK_ACCOUNT_NOT_ALLOWED);
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

    private record TransferEndpoint(BankingV2 cashAccount, BankingMethods method, BankingV2 parentBank) {
        boolean cash() {
            return cashAccount != null;
        }

        double balance() {
            Double balance = cash() ? cashAccount.getBalance() : method.getBalance();
            return balance != null ? balance : 0.0;
        }

        String txnBankId() {
            return cash() ? cashAccount.getBankId() : parentBank.getBankId();
        }

        String txnSourceId() {
            return cash() ? null : method.getPaymentMethodId();
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
    public ResponseEntity<?> getAllPaymentMethods(String hostelId) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        List<BankingV2> accounts = bankingV2Repository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
        if (accounts.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

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

        AllPaymentMethodsMapper mapper = new AllPaymentMethodsMapper();
        List<PaymentMethodOptionResponse> response = new ArrayList<>();

        for (BankingV2 cash : cashAccounts) {
            response.add(mapper.cash(cash, responsiblePersonName(cash, personById, roleNameById)));
        }

        for (BankingV2 bank : bankAccounts) {
            List<BankingMethods> methods = methodsByBankId.get(bank.getBankId());
            if (methods == null || methods.isEmpty()) {
                continue;
            }
            String personName = responsiblePersonName(bank, personById, roleNameById);
            for (BankingMethods method : methods) {
                QrBankType cardNetworkType = method.getCardNetwork() != null
                        ? qrTypeById.get(method.getCardNetwork()) : null;
                QrBankType upiAppType = method.getUpiApp() != null
                        ? qrTypeById.get(method.getUpiApp()) : null;
                String cardNetwork = cardNetworkType != null ? cardNetworkType.getName() : null;
                String upiApp = upiAppType != null ? upiAppType.getName() : null;
                String qrCardImage = cardNetworkType != null ? cardNetworkType.getImage()
                        : (upiAppType != null ? upiAppType.getImage() : null);
                response.add(mapper.bankMethod(bank, method, cardNetwork, upiApp, qrCardImage, personName));
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
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
