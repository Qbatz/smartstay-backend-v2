package com.smartstay.smartstay.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.Wrappers.customers.TransctionsForCustomerDetails;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.Advance;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.CustomerCredentials;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Draft;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.KycDetails;
import com.smartstay.smartstay.dao.RentHistory;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.BookingInfo;
import com.smartstay.smartstay.dto.customer.CheckoutInfo;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.dto.customer.WalletTransactions;
import com.smartstay.smartstay.dto.documents.CustomerFiles;
import com.smartstay.smartstay.payloads.customer.Address;
import com.smartstay.smartstay.payloads.customer.Booking;
import com.smartstay.smartstay.payloads.customer.Guardian;
import com.smartstay.smartstay.payloads.customer.IdProof;
import com.smartstay.smartstay.payloads.customer.JobDetails;
import com.smartstay.smartstay.payloads.customer.SaveDraftCustomerRequest;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.repositories.DraftsRepository;
import com.smartstay.smartstay.responses.customer.AdditionalContacts;
import com.smartstay.smartstay.responses.customer.AdvanceInfo;
import com.smartstay.smartstay.responses.customer.Amenities;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.responses.customer.CustomerAddress;
import com.smartstay.smartstay.responses.customer.CustomerDetails;
import com.smartstay.smartstay.responses.customer.CustomerSearchResponse;
import com.smartstay.smartstay.responses.customer.DraftDetails;
import com.smartstay.smartstay.responses.customer.HostelInformation;
import com.smartstay.smartstay.responses.customer.KycInformations;
import com.smartstay.smartstay.dto.customer.WalletInfo;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.CustomerBedStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private UploadFileToS3 uploadToS3;

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
    public ResponseEntity<?> saveDraft(String hostelId,
                                       MultipartFile profilePic,
                                       MultipartFile aadharPic,
                                       MultipartFile panPic,
                                       SaveDraftCustomerRequest payloads) {
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

        String profileImage = null;
        if (profilePic != null && !profilePic.isEmpty()) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic), "users/profile");
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
        customers.setProfilePic(profileImage);

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

        String aadharImage = null;
        if (aadharPic != null && !aadharPic.isEmpty()) {
            aadharImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(aadharPic), "users/profile");
        }

        String panImage = null;
        if (panPic != null && !panPic.isEmpty()) {
            panImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(panPic), "users/profile");
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
        draft.setAadharPic(aadharImage);
        draft.setPanPic(panImage);

        try {
            if (payloads.idProof() != null) {
                draft.setIdProofJson(objectMapper.writeValueAsString(payloads.idProof()));
            }
            if (payloads.address() != null) {
                draft.setAddressJson(objectMapper.writeValueAsString(payloads.address()));
            }
            if (payloads.booking() != null) {
                draft.setBookingJson(objectMapper.writeValueAsString(payloads.booking()));
            }
            if (payloads.jobDetails() != null) {
                draft.setJobDetailsJson(objectMapper.writeValueAsString(payloads.jobDetails()));
            }
            if (payloads.guardians() != null) {
                draft.setGuardiansJson(objectMapper.writeValueAsString(payloads.guardians()));
            }
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Invalid JSON payload", HttpStatus.BAD_REQUEST);
        }

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

        List<Deductions> listDeductionFromDraft = parseDraftDeductionsJson(draft != null ? draft.getDeductionsJson() : null);

        List<Deductions> otherDeductionBreakup = null;
        double maintenance = 0;
        double otherDeductions = 0;
        if (listDeductionFromDraft != null) {
            maintenance = listDeductionFromDraft.stream()
                    .filter(item -> item.getType() != null && item.getType().equalsIgnoreCase("maintenance"))
                    .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                    .sum();
            otherDeductions = listDeductionFromDraft.stream()
                    .filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance"))
                    .mapToDouble(item -> item.getAmount() != null ? item.getAmount() : 0.0)
                    .sum();
            otherDeductionBreakup = listDeductionFromDraft.stream().filter(item -> item.getType() != null && !item.getType().equalsIgnoreCase("maintenance")).collect(Collectors.toList());
        }

        BedDetails bedDetails = null;
        if (draft != null && draft.getBedId() != null) {
            bedDetails = bedsService.getBedDetails(draft.getBedId());
        }

        String joiningDateStr = "";
        if (draft != null && draft.getJoiningDate() != null) {
            joiningDateStr = Utils.dateToString(draft.getJoiningDate());
        } else if (customers.getExpJoiningDate() != null) {
            joiningDateStr = Utils.dateToString(customers.getExpJoiningDate());
        }

        HostelInformation hostelInformation = null;
        BookingInfo bookingInfo = null;
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
                    draft.getAdvanceAmount(),
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

        IdProof idProof = null;
        Address address = null;
        Booking booking = null;
        JobDetails jobDetails = null;
        List<Guardian> guardians = null;
        if (draft != null) {
            try {
                idProof = draft.getIdProofJson() != null ? objectMapper.readValue(draft.getIdProofJson(), IdProof.class) : null;
                address = draft.getAddressJson() != null ? objectMapper.readValue(draft.getAddressJson(), Address.class) : null;
                booking = draft.getBookingJson() != null ? objectMapper.readValue(draft.getBookingJson(), Booking.class) : null;
                jobDetails = draft.getJobDetailsJson() != null ? objectMapper.readValue(draft.getJobDetailsJson(), JobDetails.class) : null;
                guardians = draft.getGuardiansJson() != null ? objectMapper.readValue(draft.getGuardiansJson(), new TypeReference<List<Guardian>>() {}) : null;
            } catch (JsonProcessingException e) {
                // Handle exception
            }
        }

        DraftDetails details = new DraftDetails(
                customers.getCustomerId(),
                customers.getHostelId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName,
                customers.getEmailId(),
                customers.getMobile(),
                "91",
                initials,
                customers.getProfilePic(),
                customers.getCurrentStatus(),
                hostelInformation,
                bookingInfo,
                bedDetails,
                idProof,
                address,
                booking,
                jobDetails,
                guardians,
                draft.getPanPic(),
                draft.getAadharPic(),
                listDeductionFromDraft);

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
