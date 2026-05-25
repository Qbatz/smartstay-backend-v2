package com.smartstay.smartstay.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.Wrappers.customers.TransctionsForCustomerDetails;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Advance;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.CustomerCredentials;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Draft;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.KycDetails;
import com.smartstay.smartstay.dao.RentHistory;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.CustomerBedStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.BookingInfo;
import com.smartstay.smartstay.dto.customer.CheckoutInfo;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.dto.customer.WalletTransactions;
import com.smartstay.smartstay.dto.documents.CustomerFiles;
import com.smartstay.smartstay.payloads.customer.SaveDraftCustomerRequest;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.repositories.DraftsRepository;
import com.smartstay.smartstay.responses.customer.AdditionalContacts;
import com.smartstay.smartstay.responses.customer.Amenities;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.responses.customer.CustomerAddress;
import com.smartstay.smartstay.responses.customer.CustomerDetails;
import com.smartstay.smartstay.responses.customer.CustomerSearchResponse;
import com.smartstay.smartstay.responses.customer.HostelInformation;
import com.smartstay.smartstay.responses.customer.KycInformations;
import com.smartstay.smartstay.responses.customer.AdvanceInfo;
import com.smartstay.smartstay.dto.customer.WalletInfo;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomersServiceV2 {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService userService;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private DraftsRepository draftsRepository;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private CustomerCredentialsService ccs;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private FloorsService floorsService;

    @Autowired
    private RoomsService roomsService;

    @Autowired
    private BedsService bedsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingsService bookingsService;

    @Autowired
    private InvoiceV1Service invoiceService;

    @Autowired
    private CustomersBedHistoryService bedHistory;

    @Autowired
    @Lazy
    private AmenitiesService amenitiesService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    @Lazy
    private AmenityRequestService amenityRequestService;

    @Autowired
    private BankingService bankingService;

    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;

    @Autowired
    @Lazy
    private CustomerDocumentsService customerDocumentsService;

    @Autowired
    private AdditionalContactService additionalContactService;

    public ResponseEntity<?> searchCustomersByMobile(String hostelId, String search) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (search == null || search.trim().length() < 4) {
            return new ResponseEntity<>("Minimum 4 digits required for search", HttpStatus.BAD_REQUEST);
        }

        List<Customers> matchedCustomers = customersRepository.searchByMobileAndHostelId(hostelId, search.trim());
        if (matchedCustomers == null || matchedCustomers.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        List<CustomerSearchResponse> result = matchedCustomers
                .stream()
                .map(c -> new CustomerSearchResponse(
                        c.getCustomerId(),
                        NameUtils.getFullName(c.getFirstName(), c.getLastName()),
                        c.getFirstName(),
                        c.getLastName(),
                        c.getProfilePic(),
                        NameUtils.getInitials(c.getFirstName(), c.getLastName()),
                        "+91 " + c.getMobile(),
                        c.getEmailId()))
                .toList();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> saveDraft(String hostelId, SaveDraftCustomerRequest payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.WALK_IN.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        String mobileStatus = "";
        String emailStatus = "";

        if (customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(payloads.mobile(), hostelId, List.of(CustomerStatus.VACATED.name()))) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
        }

        if (Utils.checkNullOrEmpty(payloads.emailId())) {
            if (customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(payloads.emailId(), hostelId, List.of(CustomerStatus.VACATED.name()))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }
        }

        if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
            return new ResponseEntity<>(Map.of(
                    "mobileStatus", mobileStatus,
                    "emailStatus", emailStatus,
                    "message", "Validation failed"
            ), HttpStatus.BAD_REQUEST);
        }

        if (payloads.floorId() != null && payloads.roomId() != null && payloads.bedId() != null) {
            if (!floorsService.checkFloorExistForHostel(payloads.floorId(), hostelId)) {
                return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
            }
            if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
                return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
            }
            if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), hostelId)) {
                return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
            }
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setCountry(1L);
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.emailId());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.DRAFT.name());
        customers.setHostelId(hostelId);
        customers.setCreatedBy(loginId);
        customers.setCreatedAt(new Date());
        customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
        customers.setProfilePic(null);

        if (Utils.checkNullOrEmpty(payloads.joiningDate())) {
            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            customers.setExpJoiningDate(joiningDate);
        }

        CustomerCredentials customerCredentials = ccs.addCustomerCredentials(payloads.mobile());
        if (customerCredentials != null) {
            customers.setXuid(customerCredentials.getXuid());
        }

        Customers savedCustomer = customersRepository.save(customers);

        String deductionsJson = null;
        if (payloads.deductions() != null && !payloads.deductions().isEmpty()) {
            try {
                deductionsJson = objectMapper.writeValueAsString(payloads.deductions());
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>("Invalid deductions payload", HttpStatus.BAD_REQUEST);
            }
        }

        Date now = new Date();
        Draft draft = new Draft();
        draft.setCustomerId(savedCustomer.getCustomerId());
        draft.setHostelId(hostelId);
        if (Utils.checkNullOrEmpty(payloads.joiningDate())) {
            draft.setJoiningDate(Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        }
        if (Utils.checkNullOrEmpty(payloads.bookingDate())) {
            draft.setBookingDate(Utils.stringToDate(payloads.bookingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        }
        draft.setBookingAmount(payloads.bookingAmount());
        draft.setFloorId(payloads.floorId());
        draft.setRoomId(payloads.roomId());
        draft.setBedId(payloads.bedId());
        draft.setBankId(payloads.bankId());
        draft.setReferenceNumber(payloads.referenceNumber());
        draft.setAdvanceAmount(payloads.advanceAmount());
        draft.setRentalAmount(payloads.rentalAmount());
        draft.setStayType(payloads.stayType());
        draft.setDeductionsJson(deductionsJson);
        draft.setProRate(payloads.proRate());
        draft.setCreatedAt(now);
        draft.setUpdatedAt(now);

        draftsRepository.save(draft);

        return new ResponseEntity<>(Map.of(
                "message", Utils.CREATED,
                "customerId", savedCustomer.getCustomerId(),
                "currentStatus", savedCustomer.getCurrentStatus()
        ), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> deleteDraft(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (hostelId == null || hostelId.isBlank() || customerId == null || customerId.isBlank()) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name())) {
            return new ResponseEntity<>("Only draft customers can be deleted with this endpoint", HttpStatus.BAD_REQUEST);
        }

        Optional<Draft> draftRow = draftsRepository.findById(customerId);
        if (draftRow.isPresent() && !draftRow.get().getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        draftRow.ifPresent(d -> draftsRepository.deleteById(customerId));

        if (customers.getXuid() != null) {
            List<Customers> listCustomersBasedOnXuid = customersRepository.findByXuid(customers.getXuid());
            if (listCustomersBasedOnXuid.size() == 1) {
                ccs.deleteCustomer(customers.getXuid());
            }
        }
        customersRepository.delete(customers);

        userService.addUserLog(hostelId, customerId, ActivitySource.CUSTOMERS, ActivitySourceType.DELETE, users);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Same response shape as {@link CustomersService#getCustomerDetails(String)}, but {@link HostelInformation} and
     * {@link BookingInfo} are filled from {@link Draft} when the customer is in {@link CustomerStatus#DRAFT}.
     */
    public ResponseEntity<?> getDraftCustomerDetails(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = userService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customerId == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name())) {
            return new ResponseEntity<>("Customer is not in draft status", HttpStatus.BAD_REQUEST);
        }

        Draft draft = draftsRepository.findById(customerId).orElse(null);

        String fullName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        String initials = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());

        boolean isNewRentAvailable = false;
        double newRentAmount = 0.0;
        String newRentLabelHint = null;
        List<RentHistory> rentHistories = bookingsService.getNewRentAmount(customerId, new Date());
        List<InvoiceResponse> invoiceResponseList = invoiceService.getInvoiceResponseList(customers.getCustomerId());
        InvoiceResponse advanceInvoice = invoiceResponseList.stream().filter(inv -> "ADVANCE".equalsIgnoreCase(inv.invoiceType())).limit(1).findFirst().orElse(null);
        if (rentHistories != null) {
            List<RentHistory> listNewRentHistory = rentHistories.stream().filter(i -> Utils.compareWithTwoDates(i.getStartsFrom(), new Date()) > 0).toList();
            if (!listNewRentHistory.isEmpty()) {
                isNewRentAvailable = true;
                RentHistory rh = listNewRentHistory.get(listNewRentHistory.size() - 1);
                if (rh != null) {
                    newRentAmount = rh.getRent();
                    newRentLabelHint = "Rent Update Scheduled, Effective from " + Utils.dateToString(rh.getStartsFrom());
                }
            }
        }

        HostelInformation hostelInformation = null;
        BookingInfo bookingInfo = null;
        Advance advance = customers.getAdvance();
        List<Deductions> listDeductionFromAdvance = advance != null ? advance.getDeductions() : null;
        List<Deductions> listDeductionFromDraft = parseDraftDeductionsJson(draft != null ? draft.getDeductionsJson() : null);
        List<Deductions> deductionSource = (listDeductionFromAdvance != null && !listDeductionFromAdvance.isEmpty())
                ? listDeductionFromAdvance
                : listDeductionFromDraft;

        AdvanceInfo advanceInfo = null;
        List<Deductions> otherDeductionBreakup = null;
        String bookingId = null;
        double maintenance = 0;
        double otherDeductions = 0;
        double advanceAmount = 0;
        if (advance != null) {
            advanceAmount = advance.getAdvanceAmount();
            if (deductionSource != null) {
                maintenance = deductionSource.stream()
                        .filter(item -> item.getType() != null && item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                        .sum();
                otherDeductions = deductionSource.stream()
                        .filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                        .sum();
                otherDeductionBreakup = deductionSource.stream().filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance")).collect(Collectors.toList());
            }
            advanceInfo = CustomersService.toAdvanceInfoResponse(advance, advanceInvoice, draft != null && draft.getBookingAmount() != null ? draft.getBookingAmount() : 0.0);
        } else if (draft != null && draft.getAdvanceAmount() != null && draft.getAdvanceAmount() > 0) {
            advanceAmount = draft.getAdvanceAmount();
            if (deductionSource != null) {
                maintenance = deductionSource.stream()
                        .filter(item -> item.getType() != null && item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                        .sum();
                otherDeductions = deductionSource.stream()
                        .filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                        .sum();
                otherDeductionBreakup = deductionSource.stream().filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance")).collect(Collectors.toList());
            }
        }

        BedDetails bedDetails = null;
        if (draft != null && draft.getBedId() != null) {
            bedDetails = bedsService.getBedDetails(draft.getBedId());
        }

        double advanceForHostel = advanceAmount;
        if (advanceForHostel == 0 && draft != null && draft.getAdvanceAmount() != null) {
            advanceForHostel = draft.getAdvanceAmount();
        }

        String joiningDateStr = "";
        if (draft != null && draft.getJoiningDate() != null) {
            joiningDateStr = Utils.dateToString(draft.getJoiningDate());
        } else if (customers.getExpJoiningDate() != null) {
            joiningDateStr = Utils.dateToString(customers.getExpJoiningDate());
        }

        if (draft != null) {
            Integer roomId = draft.getRoomId();
            Integer floorId = draft.getFloorId();
            Integer bedId = draft.getBedId();
            String roomName = bedDetails != null ? bedDetails.getRoomName() : null;
            String floorName = bedDetails != null ? bedDetails.getFloorName() : null;
            String bedName = bedDetails != null ? bedDetails.getBedName() : null;
            if (roomId == null && bedDetails != null) {
                roomId = bedDetails.getRoomId();
            }
            if (floorId == null && bedDetails != null) {
                floorId = bedDetails.getFloorId();
            }
            if (bedId == null && bedDetails != null) {
                bedId = bedDetails.getBedId();
            }

            hostelInformation = new HostelInformation(
                    roomName,
                    roomId,
                    floorName,
                    floorId,
                    bedName,
                    bedId,
                    joiningDateStr,
                    CustomerStatus.DRAFT.name(),
                    advanceForHostel,
                    otherDeductions,
                    maintenance,
                    draft.getRentalAmount(),
                    otherDeductionBreakup);

            String bookedBed = bedName != null ? bedName : (bedId != null ? String.valueOf(bedId) : null);
            String bookedFloor = floorName != null ? floorName : (floorId != null ? String.valueOf(floorId) : null);
            String bookedRoom = roomName != null ? roomName : (roomId != null ? String.valueOf(roomId) : null);
            String bookingDateStr = draft.getBookingDate() != null ? Utils.dateToString(draft.getBookingDate()) : "";
            bookingInfo = new BookingInfo(bookingDateStr, draft.getBookingAmount(), bookedBed, bookedFloor, bookedRoom);
        }

        CustomerAddress address = new CustomerAddress(customers.getStreet(), customers.getHouseNo(), customers.getLandmark(), customers.getPincode(), customers.getCity(), customers.getState());
        KycDetails kycDetails = customers.getKycDetails();
        KycInformations kycInfo;
        if (kycDetails == null) {
            kycInfo = new KycInformations(customers.getKycStatus(), null, null, null);
        } else {
            kycInfo = new KycInformations(kycDetails.getCurrentStatus(), null, null, null);
        }

        CheckoutInfo checkoutInfo = null;

        List<BedHistory> listBeds = bedHistory.getCustomersBedHistory(customers.getCustomerId());
        List<Amenities> amenities = amenitiesService.getAmenitiesByCustomerId(customerId);
        List<TransactionDto> listTransactions = transactionService.getTranactionInfoByCustomerId(customerId);
        List<AmenityRequestDTO> listRequestedAmenities = amenityRequestService.getRequestedAmenities(customerId, customers.getHostelId());

        List<String> invoicesIds = listTransactions.stream().map(TransactionDto::invoiceId).toList();
        Set<String> bankIds = listTransactions.stream().map(TransactionDto::bankId).collect(Collectors.toSet());
        List<InvoicesV1> listOfInvoices = invoiceService.findInvoices(invoicesIds);
        List<BankingV1> listOFBankings = bankingService.findAllBanksById(bankIds);
        List<String> userIds = listOFBankings.stream().map(BankingV1::getUserId).toList();
        List<Users> listUsers = userService.findAllUsersFromUserId(userIds);

        List<com.smartstay.smartstay.responses.customer.TransactionDto> listTransactionResponse = listTransactions.stream().map(i -> new TransctionsForCustomerDetails(listOfInvoices, listOFBankings, listUsers).apply(i)).toList();

        List<WalletTransactions> walletTransactions = customerWalletHistoryService.getWalletTransactions(customerId);
        double walletAmount = 0.0;
        if (customers.getWallet() != null && customers.getWallet().getAmount() != null) {
            walletAmount = Utils.roundOffWithTwoDigit(customers.getWallet().getAmount());
        }

        WalletInfo walletInfo = new WalletInfo(walletAmount, walletTransactions);
        CustomerFiles customerFiles = customerDocumentsService.getCustomerFiles(customerId);
        List<AdditionalContacts> additionalContacts = additionalContactService.getAdditionalContact(customers.getHostelId(), customerId);

        boolean isJoiningDateEditable = !bedHistory.hasReassignedHistory(customerId);

        String createdDate = Utils.dateToString(customers.getCreatedAt());
        String createdTime = Utils.dateToTime(customers.getCreatedAt());
        String createdAt = Utils.dateToDateTime(customers.getCreatedAt());
        String createdBy = customers.getCreatedBy();
        String createdByName = null;
        String createdByInitials = null;
        String createdByPic = null;
        if (createdBy != null && !createdBy.isEmpty()) {
            Users createdByUser = userService.findUserByUserId(createdBy);
            if (createdByUser != null) {
                createdByName = Utils.fullName(createdByUser.getFirstName(), createdByUser.getLastName());
                createdByInitials = Utils.getInitials(createdByUser.getFirstName(), createdByUser.getLastName());
                createdByPic = createdByUser.getProfileUrl();
            }
        }

        CustomerDetails details = new CustomerDetails(customers.getCustomerId(),
                customers.getHostelId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName,
                customers.getEmailId(),
                customers.getMobile(),
                "91",
                initials,
                customers.getProfilePic(),
                bookingId,
                isNewRentAvailable,
                newRentAmount,
                newRentLabelHint,
                customers.getCurrentStatus(),
                address,
                hostelInformation,
                kycInfo,
                advanceInfo,
                checkoutInfo,
                bookingInfo,
                invoiceResponseList,
                listBeds,
                listTransactionResponse,
                amenities,
                listRequestedAmenities,
                walletInfo,
                customerFiles,
                additionalContacts,
                isJoiningDateEditable,
                createdDate,
                createdTime,
                createdAt,
                createdBy,
                createdByName,
                createdByInitials,
                createdByPic,
                null);

        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    private List<Deductions> parseDraftDeductionsJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Deductions>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }
}
