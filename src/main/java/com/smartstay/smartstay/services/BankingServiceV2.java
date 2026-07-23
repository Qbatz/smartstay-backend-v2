package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.banking.BankingMethodsMapper;
import com.smartstay.smartstay.Wrappers.banking.BankingV2Mapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.UserHostel;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.BankAccountTypeV2;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.CashAccountType;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.payloads.banking.AddBankingMethod;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import com.smartstay.smartstay.repositories.BankingV2Repository;
import com.smartstay.smartstay.responses.banking.BankV2ListResponse;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.responses.banking.BankingMethodResponse;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
