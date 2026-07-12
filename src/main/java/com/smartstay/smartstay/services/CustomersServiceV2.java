package com.smartstay.smartstay.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.Wrappers.customers.TransctionsForCustomerDetails;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.BookingInfo;
import com.smartstay.smartstay.dto.customer.CheckoutInfo;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.dto.customer.WalletTransactions;
import com.smartstay.smartstay.dto.documents.CustomerFiles;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.payloads.customer.Address;
import com.smartstay.smartstay.payloads.drafts.UpdateDrafts;
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
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomersServiceV2 {

    @Value("${ENVIRONMENT}")
    private String environment;
    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService userService;
    @Autowired
    private CustomerBillingRulesService customerBillingRulesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private CustomerDraftService customerDraftService;
    @Autowired
    private CustomersConfigService customersConfigService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
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
    private UploadFileToS3 uploadToS3;
    @Autowired
    private WhatsAppService whatsappService;

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

        List<Deductions> deductionsList = null;
        if (payloads.deductions() != null) {
            deductionsList = payloads.deductions()
                    .stream()
                    .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                    .toList();
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
        draft.setDeductions(deductionsList);
        draft.setProRate(payloads.proRate());
        draft.setShouldCollectFullRent(payloads.shouldCollectFullRent());
        draft.setCustomRent(payloads.customRent());
        draft.setCreatedAt(now);
        draft.setUpdatedAt(now);
        draft.setAadharPic(aadharImage);
        draft.setPanPic(panImage);
        // Optional vehicle details; persisted only when provided (columns stay NULL otherwise).
        if (payloads.vehicleDetails() != null) {
            draft.setVehicleType(payloads.vehicleDetails().vehicleType());
            draft.setVehicleNumber(payloads.vehicleDetails().vehicleNumber());
            draft.setIsParkingSpaceRequired(payloads.vehicleDetails().isParkingSpaceRequired());
        }

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
            if (payloads.oneTimeDeduction() != null) {
                draft.setOneTimeDeductionJson(objectMapper.writeValueAsString(payloads.oneTimeDeduction()));
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

    /**
     * Updates an existing customer draft (identified by hostelId + customerId). Unlike the standard
     * update APIs, every field is applied as sent — null / empty / blank values overwrite existing data
     * so the user can clear previously entered draft information. No mandatory-field validation is
     * enforced (the controller omits {@code @Valid}); only entity-existence / data-integrity checks run.
     * The draft is never created here — a missing draft returns an error.
     */
    @Transactional
    public ResponseEntity<?> updateDraft(String hostelId, String customerId,
                                         MultipartFile profilePic,
                                         MultipartFile aadharPic,
                                         MultipartFile panPic,
                                         SaveDraftCustomerRequest payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (hostelId == null || hostelId.isBlank() || customerId == null || customerId.isBlank()) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
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

        // Locate the existing draft: the customer must exist, belong to this hostel, and still be a DRAFT.
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getHostelId() == null || !customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!CustomerStatus.DRAFT.name().equalsIgnoreCase(customers.getCurrentStatus())) {
            return new ResponseEntity<>(Utils.DRAFT_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Draft draft = draftsRepository.findById(customerId).orElse(null);
        if (draft == null) {
            return new ResponseEntity<>(Utils.DRAFT_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        // Data-integrity check (not a mandatory-field check): if a full bed selection is supplied it must
        // reference a real floor/room/bed in this hostel.
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

        // Images are binary, not clearable text fields: replace only when a new file is uploaded, otherwise
        // keep the existing image.
        if (profilePic != null && !profilePic.isEmpty()) {
            customers.setProfilePic(uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic), "users/profile"));
        }
        if (aadharPic != null && !aadharPic.isEmpty()) {
            draft.setAadharPic(uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(aadharPic), "users/profile"));
        }
        if (panPic != null && !panPic.isEmpty()) {
            draft.setPanPic(uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(panPic), "users/profile"));
        }

        String idProofJson;
        String addressJson;
        String bookingJson;
        String jobDetailsJson;
        String guardiansJson;
        String oneTimeDeductionJson;
        try {
            idProofJson = payloads.idProof() != null ? objectMapper.writeValueAsString(payloads.idProof()) : null;
            addressJson = payloads.address() != null ? objectMapper.writeValueAsString(payloads.address()) : null;
            bookingJson = payloads.booking() != null ? objectMapper.writeValueAsString(payloads.booking()) : null;
            jobDetailsJson = payloads.jobDetails() != null ? objectMapper.writeValueAsString(payloads.jobDetails()) : null;
            guardiansJson = payloads.guardians() != null ? objectMapper.writeValueAsString(payloads.guardians()) : null;
            oneTimeDeductionJson = payloads.oneTimeDeduction() != null ? objectMapper.writeValueAsString(payloads.oneTimeDeduction()) : null;
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Invalid JSON payload", HttpStatus.BAD_REQUEST);
        }
        List<Deductions> deductionsList = null;

        if (payloads.deductions() != null) {
            deductionsList = payloads.deductions()
                    .stream()
                    .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                    .toList();
        }


        // Basic customer fields on the draft — applied as sent so empty values clear the column.
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.emailId());
        customers.setExpJoiningDate(parseDraftDate(payloads.joiningDate()));
        customers.setLastUpdatedAt(new Date());
        customers.setUpdatedBy(loginId);

        // Draft detail fields — every value applied as sent (null/empty clears).
        draft.setJoiningDate(parseDraftDate(payloads.joiningDate()));
        draft.setBookingDate(parseDraftDate(payloads.bookingDate()));
        draft.setBookingAmount(payloads.bookingAmount());
        draft.setFloorId(payloads.floorId());
        draft.setRoomId(payloads.roomId());
        draft.setBedId(payloads.bedId());
        draft.setBankId(payloads.bankId());
        draft.setReferenceNumber(payloads.referenceNumber());
        draft.setAdvanceAmount(payloads.advanceAmount());
        draft.setRentalAmount(payloads.rentalAmount());
        draft.setStayType(payloads.stayType());
        draft.setProRate(payloads.proRate());
        draft.setShouldCollectFullRent(payloads.shouldCollectFullRent());
        draft.setCustomRent(payloads.customRent());
        draft.setIdProofJson(idProofJson);
        draft.setAddressJson(addressJson);
        draft.setBookingJson(bookingJson);
        draft.setJobDetailsJson(jobDetailsJson);
        draft.setGuardiansJson(guardiansJson);
        draft.setOneTimeDeductionJson(oneTimeDeductionJson);
        draft.setDeductions(deductionsList);
        // Vehicle details applied as sent; an omitted vehicleDetails object clears all three columns.
        VehicleDetails vehicle = payloads.vehicleDetails();
        draft.setVehicleType(vehicle != null ? vehicle.vehicleType() : null);
        draft.setVehicleNumber(vehicle != null ? vehicle.vehicleNumber() : null);
        draft.setIsParkingSpaceRequired(vehicle != null ? vehicle.isParkingSpaceRequired() : null);
        draft.setUpdatedAt(new Date());

        customersRepository.save(customers);
        draftsRepository.save(draft);

        return new ResponseEntity<>(Map.of(
                "message", Utils.UPDATED,
                "customerId", customers.getCustomerId(),
                "currentStatus", customers.getCurrentStatus()
        ), HttpStatus.OK);
    }

    /**
     * Parses a user-supplied draft date, returning {@code null} for null/blank input so an empty value
     * clears the stored date instead of failing.
     */
    private Date parseDraftDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Utils.stringToDate(value.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
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

//        List<Deductions> deductionsList = null;
//        if (payloads.deductions() != null) {
//            deductionsList = payloads.deductions()
//                    .stream()
//                    .map(i -> new Deductions(i.type(), i.amount(), 0.0))
//                    .toList();
//        }

        String fullName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        String initials = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());

        List<Deductions> listDeductionFromDraft = draft.getDeductions();

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
        List<NonRefundable> oneTimeDeduction = null;
        if (draft != null) {
            try {
                idProof = draft.getIdProofJson() != null ? objectMapper.readValue(draft.getIdProofJson(), IdProof.class) : null;
                address = draft.getAddressJson() != null ? objectMapper.readValue(draft.getAddressJson(), Address.class) : null;
                booking = draft.getBookingJson() != null ? objectMapper.readValue(draft.getBookingJson(), Booking.class) : null;
                jobDetails = draft.getJobDetailsJson() != null ? objectMapper.readValue(draft.getJobDetailsJson(), JobDetails.class) : null;
                guardians = draft.getGuardiansJson() != null ? objectMapper.readValue(draft.getGuardiansJson(), new TypeReference<List<Guardian>>() {
                }) : null;
                oneTimeDeduction = draft.getOneTimeDeductionJson() != null ? objectMapper.readValue(draft.getOneTimeDeductionJson(), new TypeReference<List<NonRefundable>>() {
                }) : null;
            } catch (JsonProcessingException e) {
                // Handle exception
            }
        }

        // Vehicle details returned only when the draft has any value stored (else null), consistent
        // with the other optional draft sub-objects.
        VehicleDetails vehicleDetails = (draft != null
                && (draft.getVehicleType() != null
                || draft.getVehicleNumber() != null
                || draft.getIsParkingSpaceRequired() != null))
                ? new VehicleDetails(draft.getVehicleType(), draft.getVehicleNumber(),
                draft.getIsParkingSpaceRequired())
                : null;

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
                listDeductionFromDraft,
                vehicleDetails,
                draft != null ? draft.getBankId() : null,
                draft != null ? draft.getReferenceNumber() : null,
                draft != null ? draft.getStayType() : null,
                draft != null ? draft.getBookingAmount() : null,
                booking != null ? booking.refuseAdvanceAmount() : null,
                draft != null ? draft.getProRate() : null,
                draft != null ? draft.getRentalAmount() : null,
                draft != null ? draft.getAdvanceAmount() : null,
                draft != null ? draft.getShouldCollectFullRent() : null,
                draft != null ? draft.getCustomRent() : null,
                oneTimeDeduction);

        return new ResponseEntity<>(details, HttpStatus.OK);
    }

//    private List<Deductions> parseDraftDeductionsJson(String json) {
//        if (json == null || json.isBlank()) {
//            return null;
//        }
//        try {
//            return objectMapper.readValue(json, new TypeReference<List<Deductions>>() {
//            });
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public ResponseEntity<?> updateDraftInformations(String hostelId, String customerId, UpdateDrafts updateDrafts) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = userService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.WALK_IN.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (!hostelV1.getHostelId().equalsIgnoreCase(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        return customerDraftService.updateDraftInfo(customerId, updateDrafts);


    }

    public ResponseEntity<?> checkinCustomer(String hostelId, String customerId, CheckInRequestNew payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!Utils.checkNullOrEmpty(customerId)) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (!floorsService.checkFloorExistForHostel(payloads.floorId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.UNAUTHORIZED);
        }

        if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.UNAUTHORIZED);
        }

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.UNAUTHORIZED);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
        }


        String date = payloads.joiningDate().replace("/", "-");
        if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
            return new ResponseEntity<>(Utils.CHECK_IN_FUTURE_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (bedsService.isBedAvailable(payloads.bedId(), user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {
            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), joiningDate);
            BillingDates currentBillDate = getCurrentBillDateForCheckin(hostelV1.getHostelId(), joiningDate, billingDates);

            Double deductionAmount = 0.0;
            Double refundableAmount = 0.0;
            double totalAdvanceAmount = 0.0;
            double rentAmount = 0.0;
            boolean shouldCollectFullRent = false;
            List<Deductions> listDeductions = null;
            if (payloads.deductions() != null) {
                listDeductions = payloads.deductions()
                        .stream()
                        .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                        .toList();
                deductionAmount = payloads
                        .deductions()
                        .stream()
                        .mapToDouble(i -> {
                            if (i.amount() != null) {
                                return i.amount();
                            }
                            return 0.0;
                        })
                        .sum();
            }
            if (payloads.refundableAmount() != null) {
                refundableAmount = payloads.refundableAmount();
            }

            totalAdvanceAmount = refundableAmount + deductionAmount;

            Advance advance = customers.getAdvance();
            if (advance == null) {
                advance = new Advance();
            }

            advance.setAdvanceAmount(refundableAmount);
            advance.setCustomers(customers);
            advance.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            advance.setCreatedBy(userId);
            advance.setCreatedAt(new Date());
            advance.setDeductions(listDeductions);
            advance.setInvoiceDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            advance.setUpdatedAt(new Date());

            customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            customers.setAdvance(advance);

            Customers savedCustomer = customersRepository.save(customers);

            bedsService.addUserToBed(payloads.bedId(), payloads.joiningDate().replace("/", "-"), savedCustomer.getCustomerId());

            if (payloads.shouldCollectFullRent() == null || !payloads.shouldCollectFullRent()) {
                rentAmount = payloads.rentalAmount();
            }

            CheckInRequest checkInRequest = new CheckInRequest(payloads.floorId(),
                    payloads.bedId(),
                    payloads.roomId(),
                    payloads.joiningDate(),
                    totalAdvanceAmount,
                    payloads.rentalAmount(),
                    payloads.stayType(),
                    payloads.deductions(),
                    shouldCollectFullRent);
            bookingsService.addCheckin(customers, checkInRequest);
            customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);

            if (totalAdvanceAmount > 0) {
                invoiceV1Service.addAdvanceInvoice(customerId, totalAdvanceAmount, InvoiceType.ADVANCE.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, deductionAmount, listDeductions);
            }

            if (billingDates.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
                if (billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                    return setupJoiningDateBasisCheckin(currentBillDate, joiningDate, customers, user, payloads.rentalAmount(), payloads.oneTimeDeduction());
                } else {
                    CheckInRequest invoiceRequest = getCurrentCycleInvoiceRequest(checkInRequest, joiningDate, currentBillDate);
                    calculateRentAndCreateRentalInvoice(customers, invoiceRequest, payloads.shouldCollectFullRent(), payloads.customRent(), payloads.oneTimeDeduction());
                    if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                        whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                    }
                    userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            } else {
                if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                    whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                }
                userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }
        } else {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }
    }

    private BillingDates getCurrentBillDateForCheckin(String hostelId, Date joiningDate, BillingDates billingDates) {
        if (billingDates != null && billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
            return hostelService.getJoiningBasedCurrentMonthBillingDate(joiningDate, hostelId, new Date());
        }
        return hostelService.getCurrentBillStartAndEndDates(hostelId);
    }

    private CheckInRequest getCurrentCycleInvoiceRequest(CheckInRequest request, Date joiningDate, BillingDates currentBillDate) {
        if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
            String currentCycleStartDate = Utils.dateToString(currentBillDate.currentBillStartDate());
            return new CheckInRequest(request.floorId(), request.bedId(), request.roomId(), currentCycleStartDate, request.advanceAmount(), request.rentalAmount(), request.stayType(), request.deductions(), Boolean.TRUE.equals(request.proRate()));
        }
        return request;
    }

    public void calculateRentAndCreateRentalInvoice(Customers customers, CheckInRequest payloads, Boolean shouldCollectFullRent, Double customRent, List<NonRefundable> ontimeDeductions) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 != null) {

            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

            BillingDates billingDates = hostelService.getBillingRuleOnDate(customers.getHostelId(), joiningDate);


            Calendar c = Calendar.getInstance();
            c.setTime(joiningDate);

            boolean collectFullRent = false;
            if (shouldCollectFullRent != null) {
                collectFullRent = shouldCollectFullRent;
            }

            double totalRentalAmount = 0.0;
            double rentAlone = 0.0;
            double deductionAmount = 0.0;
            List<Deductions> listDeductions = null;

            if (collectFullRent) {
                if (customRent != null && customRent != 0.0) {
                    rentAlone = customRent;
                } else {
                    rentAlone = payloads.rentalAmount();
                }
            } else {
                rentAlone = payloads.rentalAmount();
            }

            if (ontimeDeductions != null) {
                listDeductions = ontimeDeductions
                        .stream()
                        .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                        .toList();
                deductionAmount = ontimeDeductions
                        .stream()
                        .mapToDouble(i -> {
                            if (i.amount() != null) {
                                return i.amount();
                            }
                            return 0.0;
                        })
                        .sum();
            }

            totalRentalAmount = rentAlone + deductionAmount;

            if (Utils.compareWithTwoDates(joiningDate, billingDates.currentBillStartDate()) >= 0) {
                if (billingDates.hasGracePeriod()) {
                    Date gracePeriodEndingDate = Utils.addDaysToDate(billingDates.currentBillStartDate(), billingDates.gracePeriodDays() - 1);
                    if (Utils.compareWithTwoDates(joiningDate, gracePeriodEndingDate) <= 0) {
                        double finalRent = payloads.rentalAmount();
                        invoiceV1Service.addInvoice(customers.getCustomerId(), finalRent + deductionAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, rentAlone, deductionAmount, listDeductions);
                    } else {
                        if (collectFullRent) {
                            invoiceV1Service.addInvoice(customers.getCustomerId(), totalRentalAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, rentAlone, deductionAmount, listDeductions);
                        } else {
                            long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                            long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
                            double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
                            double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
                            if (finalRent > payloads.rentalAmount()) {
                                finalRent = payloads.rentalAmount();
                            }
                            invoiceV1Service.addInvoice(customers.getCustomerId(), finalRent + deductionAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, finalRent, deductionAmount, listDeductions);

                        }
                    }
                } else {
                    if (collectFullRent) {
                        invoiceV1Service.addInvoice(customers.getCustomerId(), totalRentalAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, rentAlone, deductionAmount, listDeductions);
                    } else {
                        long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                        long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
                        double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
                        double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
                        if (finalRent > payloads.rentalAmount()) {
                            finalRent = payloads.rentalAmount();
                        }
                        invoiceV1Service.addInvoice(customers.getCustomerId(), finalRent + deductionAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, finalRent, deductionAmount, listDeductions);

                    }
                }
            } else {
                long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
                double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
                double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
                if (finalRent > payloads.rentalAmount()) {
                    finalRent = payloads.rentalAmount();
                }

                invoiceV1Service.addInvoice(customers.getCustomerId(), finalRent + deductionAmount, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, finalRent, deductionAmount, listDeductions);

            }
        }

    }

    private ResponseEntity<?> setupJoiningDateBasisCheckin(BillingDates currentBillDate, Date joiningDate, Customers customers, Users users, double rentalAmount, List<NonRefundable> oneTimeDeductions) {
        if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) >= 0 && Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillEndDate()) <= 0) {
            double deductionAmount = 0.0;
            List<Deductions> listDeductions = null;
            if (oneTimeDeductions != null) {
                deductionAmount = oneTimeDeductions
                        .stream()
                        .mapToDouble(i -> {
                            if (i.amount() != null) {
                                return i.amount();
                            }
                            return 0.0;
                        })
                        .sum();
                listDeductions = new ArrayList<>(oneTimeDeductions
                        .stream()
                        .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                        .toList());
            }
            double totalRentalAmount = rentalAmount + deductionAmount;
            //should generate the invoice
            //send welcome message on whatsapp
            invoiceV1Service.createNewInvoiceCurrentMonthJoining(customers, joiningDate, totalRentalAmount, currentBillDate, listDeductions, deductionAmount);
            if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
            }

        } else if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
            invoiceV1Service.createNewInvoiceCurrentMonthJoining(customers, currentBillDate.currentBillStartDate(), rentalAmount, currentBillDate);
            if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
            }
        }
//        else {
//            if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
//                //for old month joining and new invoice for current month
//                invoiceService.createNewInvoiceForCurrentMonth(customers, joiningDate, rentalAmount, currentBillDate);
//            }
//        }

        customerBillingRulesService.addCustomerBillingRule(customers.getCustomerId(), customers.getHostelId(), joiningDate);
        userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, users);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> checkinBookedCustomer(String hostelId, String customerId, CheckInBookedCustomerNew payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!Utils.checkNullOrEmpty(customerId)) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 bookingsV1 = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        Integer bedId = 0;
        if (payloads.bedId() != null) {
            if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), customers.getHostelId())) {
                return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.UNAUTHORIZED);
            }

            bedId = payloads.bedId();
        } else {
            bedId = bookingsV1.getBedId();

        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
        }


        String date = payloads.joiningDate().replace("/", "-");
        if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
            return new ResponseEntity<>(Utils.CHECK_IN_FUTURE_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.isBedAvailable(bedId, user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }

        Integer roomId = 0;
        Integer floorId = 0;

        Beds beds = bedsService.findBedById(bedId);
        if (beds != null) {
            roomId = beds.getRoomId();
            floorId = beds.getBedId();
        }

        Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), joiningDate);
        BillingDates currentBillDate = getCurrentBillDateForCheckin(hostelV1.getHostelId(), joiningDate, billingDates);

        Double deductionAmount = 0.0;
        Double refundableAmount = 0.0;
        double totalAdvanceAmount = 0.0;
        double rentAmount = 0.0;
        boolean shouldCollectFullRent = false;
        List<Deductions> listDeductions = null;
        if (payloads.deductions() != null) {
            listDeductions = payloads.deductions()
                    .stream()
                    .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                    .toList();
            deductionAmount = payloads
                    .deductions()
                    .stream()
                    .mapToDouble(i -> {
                        if (i.amount() != null) {
                            return i.amount();
                        }
                        return 0.0;
                    })
                    .sum();
        }
        if (payloads.refundableAmount() != null) {
            refundableAmount = payloads.refundableAmount();
        }

        totalAdvanceAmount = refundableAmount + deductionAmount;

        Advance advance = customers.getAdvance();
        if (advance == null) {
            advance = new Advance();
        }

        advance.setAdvanceAmount(refundableAmount);
        advance.setCustomers(customers);
        advance.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
        advance.setCreatedBy(userId);
        advance.setCreatedAt(new Date());
        advance.setDeductions(listDeductions);
        advance.setInvoiceDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
        advance.setUpdatedAt(new Date());

        customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customers.setJoiningDate(Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        customers.setAdvance(advance);

        Customers savedCustomer = customersRepository.save(customers);

        bedsService.addUserToBed(bedId, payloads.joiningDate().replace("/", "-"), savedCustomer.getCustomerId());

        if (payloads.shouldCollectFullRent() == null || !payloads.shouldCollectFullRent()) {
            rentAmount = payloads.rentalAmount();
        }

        CheckInRequest checkInRequest = new CheckInRequest(floorId,
                bedId,
                roomId,
                payloads.joiningDate(),
                totalAdvanceAmount,
                payloads.rentalAmount(),
                payloads.stayType(),
                payloads.deductions(),
                shouldCollectFullRent);
        bookingsService.addBookedCheckIn(customers, checkInRequest);
        customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);

        if (totalAdvanceAmount > 0) {
            invoiceV1Service.addAdvanceInvoice(customerId, totalAdvanceAmount, InvoiceType.ADVANCE.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, deductionAmount, listDeductions);
        }

        if (billingDates.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
            if (billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                return setupJoiningDateBasisCheckin(currentBillDate, joiningDate, customers, user, payloads.rentalAmount(), payloads.oneTimeDeduction());
            } else {
                CheckInRequest invoiceRequest = getCurrentCycleInvoiceRequest(checkInRequest, joiningDate, currentBillDate);
                calculateRentAndCreateRentalInvoice(customers, invoiceRequest, payloads.shouldCollectFullRent(), payloads.customRent(), payloads.oneTimeDeduction());
                if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                    whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                }
                userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }
        } else {
            if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
            }
            userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
    }

}
