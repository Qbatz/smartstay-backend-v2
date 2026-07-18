package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.customers.CustomerMapperForBills;
import com.smartstay.smartstay.Wrappers.customers.TenantTableMapper;
import com.smartstay.smartstay.Wrappers.customers.TransctionsForCustomerDetails;
import com.smartstay.smartstay.Wrappers.retainer.CustomersListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.BedRoomFloor;
import com.smartstay.smartstay.dto.booking.CustomerInfo;
import com.smartstay.smartstay.dto.customer.CustomerData;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.dto.customer.*;
import com.smartstay.smartstay.dto.documents.CustomerFiles;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.dto.electricity.EBInfo;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.settlement.AvailableAmountToRedeem;
import com.smartstay.smartstay.dto.settlement.CurrentMonthOtherItems;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.ennum.InvoiceItems;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.events.AddRoomSettlementEbEvents;
import com.smartstay.smartstay.filterOptions.customers.FilterOptions;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.beds.CancelCheckout;
import com.smartstay.smartstay.payloads.beds.ChangeBed;
import com.smartstay.smartstay.payloads.customer.CustomerAdditionalContacts;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.repositories.DraftsRepository;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.responses.customer.CheckoutCustomers;
import com.smartstay.smartstay.responses.customer.*;
import com.smartstay.smartstay.responses.customer.KycAddressDetails;
import com.smartstay.smartstay.responses.retainer.CustomerListResponse;
import com.smartstay.smartstay.responses.settlement.DeductionsInfo;
import com.smartstay.smartstay.responses.settlement.DeductionsItem;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.FilterKeywords;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomersService {

    @Value("${ENVIRONMENT}")
    private String environment;
    @Autowired
    private UploadFileToS3 uploadToS3;
    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private DraftsRepository draftsRepository;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private ReasonService reasonService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UsersService userService;
    @Autowired
    private SettlementItemService settlementItemService;
    @Autowired
    private WhatsAppService whatsappService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private InvoiceV1Service invoiceService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private CustomersBedHistoryService bedHistory;
    @Autowired
    private CustomerCredentialsService ccs;
    @Autowired
    private CustomersConfigService customersConfigService;
    @Autowired
    private SettlementDetailsService settlementDetailsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private AdditionalContactService additionalContactService;
    @Autowired
    private CustomerBillingRulesService customerBillingRulesService;
    @Autowired
    private TableColumnService columnService;
    private KycServices kycServices;
    private ElectricityService electricityService;

    private AmenityRequestService amenityRequestService;
    private AmenitiesService amenitiesService;
    private CustomerDocumentsService customerDocumentsService;
    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    public void setKycServices(@Lazy KycServices kycServices) {
        this.kycServices = kycServices;
    }

    public static AdvanceInfo toAdvanceInfoResponse(Advance advance, InvoiceResponse invoicesV1, double bookingAmount) {
        if (advance == null) return null;
        boolean canEditAdvance = false;
        double maintenanceAmount = 0.0;
        double otherDeductionsAmount = 0.0;
        double invoicePaidAmount = 0.0;
        String paymentStatus = null;
        double paidAmount = 0.0;
        String dueDate = null;
        if (invoicesV1 != null) {
            invoicePaidAmount = invoicesV1.paidAmount();
            dueDate = invoicesV1.dueDate();
            paymentStatus = invoicesV1.paymentStatus();
            paidAmount = invoicesV1.paidAmount();
        }

        if (advance.getAdvanceAmount() > 0) {
            canEditAdvance = true;
        }

        if (advance.getDeductions() != null && !advance.getDeductions().isEmpty()) {
            for (Deductions d : advance.getDeductions()) {
                if (d.getType() == null || d.getAmount() == null) continue;

                String type = d.getType().trim().toLowerCase();
                if (type.equals("maintenance")) {
                    maintenanceAmount += d.getAmount();
                } else {
                    otherDeductionsAmount += d.getAmount();
                }
            }
        }

        double dueAmount = (advance.getAdvanceAmount() != 0) ? advance.getAdvanceAmount() - invoicePaidAmount : 0.0;

        return new AdvanceInfo(advance.getInvoiceDate() != null ? Utils.dateToString(advance.getInvoiceDate()) : null, dueDate, dueAmount, advance.getAdvanceAmount(), bookingAmount, paymentStatus, maintenanceAmount, otherDeductionsAmount, paidAmount, canEditAdvance);
    }

    @Autowired
    public void setCustomerDocumentsService(@Lazy CustomerDocumentsService customerDocumentsService) {
        this.customerDocumentsService = customerDocumentsService;
    }

    @Autowired
    public void setAmenitiesService(@Lazy AmenitiesService amenitiesService) {
        this.amenitiesService = amenitiesService;
    }

    @Autowired
    public void setAmenityRequestService(@Lazy AmenityRequestService amenityRequestService) {
        this.amenityRequestService = amenityRequestService;
    }

    @Autowired
    public void setElectricityService(@Lazy ElectricityService electricityService) {
        this.electricityService = electricityService;
    }

    public ResponseEntity<?> createCustomer(MultipartFile file, AddCustomer payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.mailId());
        customers.setHouseNo(payloads.houseNo());
        customers.setStreet(payloads.street());
        customers.setLandmark(payloads.landmark());
        customers.setPincode(payloads.pincode());
        customers.setCity(payloads.city());
        customers.setState(payloads.state());
        customers.setCountry(customers.getCountry());
        customers.setProfilePic(profileImage);
        customers.setKycStatus(KycStatus.PENDING.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCountry(1L);
        customers.setCreatedBy(user.getUserId());
        customers.setCreatedAt(new Date());

        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public ResponseEntity<?> assignBed(AssignBed assignBed) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);
        if (rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            Customers customers = customersRepository.findById(assignBed.customerId()).orElse(null);

            if (customers == null) {
                return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
            }

            if (!subscriptionService.validateSubscription(customers.getHostelId())) {
                return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
            }

            customers.setCurrentStatus(CustomerStatus.ACTIVE.name());
            Advance advanceAmount = new Advance();
            advanceAmount.setCustomers(customers);
            advanceAmount.setAdvanceAmount(assignBed.advanceAmount());
            advanceAmount.setCreatedBy(userId);
            advanceAmount.setCreatedAt(new Date());
            advanceAmount.setAdvanceAmount(assignBed.advanceAmount());
            if (Utils.compareWithTodayDate(Utils.stringToDate(assignBed.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT))) {
                advanceAmount.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            } else {
                advanceAmount.setStatus(AdvanceStatus.PENDING.name());
            }
            advanceAmount.setInvoiceDate(Utils.stringToDate(assignBed.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT));
            advanceAmount.setDueDate(Utils.stringToDate(assignBed.dueDate(), Utils.USER_INPUT_DATE_FORMAT));


            customersRepository.save(customers);

            bookingsService.assignBedToCustomer(assignBed);
            List<String> tenantId = new ArrayList<>();
            tenantId.add(customers.getCustomerId());

            userService.addUserLog(customers.getHostelId(), String.valueOf(assignBed.bedId()), ActivitySource.CUSTOMERS, ActivitySourceType.ASSIGN, user, tenantId);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

        } else {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

    }

    public ResponseEntity<?> getAllCheckInCustomers(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        return bookingsService.getAllCheckInCustomers(hostelId);
    }

    public List<Advance> getAdvancesForHostel(String hostelId) {
        return customersRepository.findAdvancesByHostelId(hostelId);
    }

    private void sortCustomersByFloorRoomBed(List<Customers> customers,
                                             Map<String, BookingsV1> bookingByCustomerId,
                                             Map<String, Draft> draftByCustomerId) {
        customers.sort((first, second) -> {
            int[] firstLocation = resolveFloorRoomBedIds(first.getCustomerId(), bookingByCustomerId, draftByCustomerId);
            int[] secondLocation = resolveFloorRoomBedIds(second.getCustomerId(), bookingByCustomerId, draftByCustomerId);
            int compare = Integer.compare(firstLocation[0], secondLocation[0]);
            if (compare != 0) {
                return compare;
            }
            compare = Integer.compare(firstLocation[1], secondLocation[1]);
            if (compare != 0) {
                return compare;
            }
            return Integer.compare(firstLocation[2], secondLocation[2]);
        });
    }

    private int[] resolveFloorRoomBedIds(String customerId,
                                         Map<String, BookingsV1> bookingByCustomerId,
                                         Map<String, Draft> draftByCustomerId) {
        int missingOrder = Integer.MAX_VALUE;
        BookingsV1 booking = bookingByCustomerId.get(customerId);
        if (booking != null && booking.getBedId() > 0) {
            return new int[]{
                    booking.getFloorId() > 0 ? booking.getFloorId() : missingOrder,
                    booking.getRoomId() > 0 ? booking.getRoomId() : missingOrder,
                    booking.getBedId()
            };
        }
        Draft draft = draftByCustomerId.get(customerId);
        if (draft != null && draft.getBedId() != null && draft.getBedId() > 0) {
            return new int[]{
                    draft.getFloorId() != null && draft.getFloorId() > 0 ? draft.getFloorId() : missingOrder,
                    draft.getRoomId() != null && draft.getRoomId() > 0 ? draft.getRoomId() : missingOrder,
                    draft.getBedId()
            };
        }
        return new int[]{missingOrder, missingOrder, missingOrder};
    }

    public List<CustomerData> searchAndGetCustomers(String hostelId, String name, List<String> types) {
        List<String> typeArray = new ArrayList<>();
        if (types == null || types.isEmpty()) {
            typeArray.add(CustomerStatus.NOTICE.name());
            typeArray.add(CustomerStatus.CHECK_IN.name());
            typeArray.add(CustomerStatus.BOOKED.name());
            typeArray.add(CustomerStatus.SETTLEMENT_GENERATED.name());
            typeArray.add(CustomerStatus.DRAFT.name());
        } else {
            types.forEach(t -> typeArray.add(t.toUpperCase()));
        }
        return customersRepository.getCustomerData(hostelId, name != null && !name.isBlank() ? name : null, typeArray);
    }

    public ResponseEntity<?> getAllCustomersForHostel(String hostelId, String name, List<String> type, Integer page, Integer size, List<String> periods, List<String> sharingType) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (authentication.getSource().equalsIgnoreCase("web")) {
            return getCustomerDetailsForWeb(hostelId, name, type, page, size, periods, sharingType);
        }

        List<CustomerData> customerData = searchAndGetCustomers(hostelId, name, type);
        HashMap<String, String> filterOption = new HashMap<>();
        List<com.smartstay.smartstay.responses.customer.CustomerData> listCustomers = customerData.stream().map(item -> {
            StringBuilder initials = new StringBuilder();
            StringBuilder fullName = new StringBuilder();
            if (item.getFirstName() != null) {
                initials.append(item.getFirstName().toUpperCase().charAt(0));
                fullName.append(item.getFirstName());
            }
            if (item.getLastName() != null && !item.getLastName().equalsIgnoreCase("")) {
                fullName.append(" ");
                fullName.append(item.getLastName());
                initials.append(item.getLastName().toUpperCase().charAt(0));

            } else {
                if (item.getFirstName().length() > 1) {
                    initials.append(item.getFirstName().toUpperCase().charAt(1));
                }
            }
            String currentStatus = null;
            if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
                currentStatus = "Booked";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                currentStatus = "Vacated";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                currentStatus = "Notice Period";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
                currentStatus = "Checked In";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
                currentStatus = "Inactive";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ACTIVE.name())) {
                currentStatus = "Active";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
                currentStatus = "Cancelled";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                currentStatus = "Settlement Generated";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name())) {
                currentStatus = "Draft";
            }

            if (!filterOption.containsKey(currentStatus)) {
                filterOption.put(currentStatus, currentStatus);
            }

            return new com.smartstay.smartstay.responses.customer.CustomerData(item.getFirstName(), item.getLastName(), fullName.toString(), item.getCity(), item.getState(), item.getCountry(), item.getMobile(), currentStatus, item.getEmailId(), item.getProfilePic(), item.getBedId(), item.getFloorId(), item.getRoomId(), item.getCustomerId(), initials.toString(), Utils.dateToString(item.getExpectedJoiningDate()), Utils.dateToString(item.getActualJoiningDate()), item.getCountryCode(), Utils.dateToString(item.getCreatedAt()), item.getBedName(), item.getRoomName(), item.getFloorName());
        }).collect(Collectors.toList());

        listCustomers.sort(Comparator
                .comparing(com.smartstay.smartstay.responses.customer.CustomerData::floorId, Comparator.nullsFirst(Utils::compareNumericIds))
                .thenComparing(com.smartstay.smartstay.responses.customer.CustomerData::roomId, Comparator.nullsFirst(Utils::compareNumericIds))
                .thenComparing(com.smartstay.smartstay.responses.customer.CustomerData::bedId, Comparator.nullsFirst(Utils::compareNumericIds)));

        CustomersList response = new CustomersList(hostelId, listCustomers.size(), null, listCustomers);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<?> getCustomerDetailsForWeb(String hostelId, String name, List<String> types, Integer page, Integer size, List<String> periodList, List<String> sharingTypeList) {

        List<String> typeArray = new ArrayList<>();
        if (types == null || types.isEmpty()) {
            typeArray.add(CustomerStatus.NOTICE.name());
            typeArray.add(CustomerStatus.CHECK_IN.name());
            typeArray.add(CustomerStatus.BOOKED.name());
            typeArray.add(CustomerStatus.SETTLEMENT_GENERATED.name());
            typeArray.add(CustomerStatus.DRAFT.name());
        } else {
            types.forEach(t -> typeArray.add(t.toUpperCase()));
        }

        Date startDate = null;
        Date endDate = null;
        List<String> customerIds = null;

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (periodList != null && !periodList.isEmpty()) {
            for (String period : periodList) {
                Date pStart = null;
                Date pEnd = null;
                if (period.equalsIgnoreCase(FilterKeywords.THIS_MONTH)) {
                    pStart = billingDates.currentBillStartDate();
                    pEnd = billingDates.currentBillEndDate();
                } else if (period.equalsIgnoreCase(FilterKeywords.LAST_MONTH)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, -1);
                    BillingDates billDatesBasedOnDate = hostelService.getBillingRuleOnDate(hostelId, calendar.getTime());
                    pStart = billDatesBasedOnDate.currentBillStartDate();
                    pEnd = billDatesBasedOnDate.currentBillEndDate();
                } else if (period.equalsIgnoreCase(FilterKeywords.LAST_3_MONTH)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, -3);
                    BillingDates billDatesBasedOnDate = hostelService.getBillingRuleOnDate(hostelId, calendar.getTime());
                    pStart = billDatesBasedOnDate.currentBillStartDate();
                    pEnd = billingDates.currentBillEndDate();
                } else if (period.equalsIgnoreCase(FilterKeywords.LAST_6_MONTH)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, -6);
                    BillingDates billDatesBasedOnDate = hostelService.getBillingRuleOnDate(hostelId, calendar.getTime());
                    pStart = billDatesBasedOnDate.currentBillStartDate();
                    pEnd = billingDates.currentBillEndDate();
                } else if (period.equalsIgnoreCase(FilterKeywords.LAST_1_YEAR)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.YEAR, -1);
                    BillingDates billDatesBasedOnDate = hostelService.getBillingRuleOnDate(hostelId, calendar.getTime());
                    pStart = billDatesBasedOnDate.currentBillStartDate();
                    pEnd = billingDates.currentBillEndDate();
                }
                // Union: take the earliest start and latest end across all selected periods
                if (pStart != null) {
                    startDate = (startDate == null || pStart.before(startDate)) ? pStart : startDate;
                }
                if (pEnd != null) {
                    endDate = (endDate == null || pEnd.after(endDate)) ? pEnd : endDate;
                }
            }

            customerIds = bookingsService.getCustomerIdsByStartAndEndDate(hostelId, startDate, endDate);
        }
        if (sharingTypeList != null && !sharingTypeList.isEmpty()) {
            List<Integer> shareTypeInts = sharingTypeList.stream()
                    .map(s -> {
                        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
            if (!shareTypeInts.isEmpty()) {
                List<String> sharingCustomerIds = bookingsService.getCustomerIdsBySharingTypes(hostelId, shareTypeInts);
                if (customerIds == null) {
                    customerIds = sharingCustomerIds;
                } else {
                    List<String> periodIds = customerIds;
                    customerIds = sharingCustomerIds.stream()
                            .filter(periodIds::contains)
                            .collect(java.util.stream.Collectors.toList());
                }
            }
        }

        Pageable pageableRequest = PageRequest.of(page - 1, size);

        List<String> listStatus = new ArrayList<>();
        listStatus.add(CustomerStatus.VACATED.name());
        listStatus.add(CustomerStatus.CHECK_IN.name());
        listStatus.add(CustomerStatus.NOTICE.name());
        listStatus.add(CustomerStatus.BOOKED.name());
        listStatus.add(CustomerStatus.SETTLEMENT_GENERATED.name());
        listStatus.add(CustomerStatus.DRAFT.name());


        List<Customers> listAllCustomersForCount = customersRepository.findCustomerByHostelId(hostelId, listStatus);
        Page<Customers> listCustomers = customersRepository.listCustomers(hostelId, name, typeArray, customerIds, pageableRequest);
        List<ColumnFilters> listColumns = columnService.getCustomerColumns(hostelId, FilterOptionsModule.MODULE_TENANT.name());

        int totalCustomers = (int) listCustomers.getTotalElements();
        int currentPage = listCustomers.getPageable().getPageNumber() + 1;
        int totalPages = listCustomers.getTotalPages();
        int vacatedCount = 0;
        int bookedCount = 0;
        int settlementGeneratedCount = 0;
        int noticePeriodCounts = 0;
        int checkedInCounts = 0;

        if (listAllCustomersForCount != null) {
            vacatedCount = (int) listAllCustomersForCount.stream().filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())).count();

            bookedCount = (int) listAllCustomersForCount.stream().filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())).count();

            settlementGeneratedCount = (int) listAllCustomersForCount.stream().filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())).count();
            noticePeriodCounts = (int) listAllCustomersForCount.stream().filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())).count();
            checkedInCounts = (int) listAllCustomersForCount.stream().filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())).count();
        }

        Summary summary = new Summary(totalCustomers, vacatedCount, bookedCount, settlementGeneratedCount, noticePeriodCounts, checkedInCounts);

        List<Customers> customersList = new ArrayList<>(listCustomers.getContent());
        List<ColumnFilters> activeColumns = listColumns.stream().filter(ColumnFilters::isSelected).sorted(Comparator.comparingInt(ColumnFilters::getOrder)).toList();
        List<String> tableColumns = activeColumns.stream().map(ColumnFilters::getFieldName).toList();

        boolean floorRoomBedNameIncluded = false;
        floorRoomBedNameIncluded = tableColumns.stream().anyMatch(i -> i.equalsIgnoreCase("Floor") || i.equalsIgnoreCase("Room") || i.equalsIgnoreCase("Bed"));

        List<String> listCustomerIds = listCustomers.stream().map(Customers::getCustomerId).toList();
        List<BookingsV1> listBookings = bookingsService.findByCustomerIds(listCustomerIds);
        List<Draft> draftRows = draftsRepository.findByCustomerIdIn(listCustomerIds);
        Map<String, Draft> draftByCustomerId = draftRows.stream().collect(Collectors.toMap(Draft::getCustomerId, Function.identity(), (a, b) -> a));
        Map<String, BookingsV1> bookingByCustomerId = listBookings.stream()
                .collect(Collectors.toMap(BookingsV1::getCustomerId, Function.identity(), (a, b) -> a));
        sortCustomersByFloorRoomBed(customersList, bookingByCustomerId, draftByCustomerId);
        List<Integer> bedIds = new ArrayList<>();
        for (BookingsV1 b : listBookings) {
            if (b.getBedId() > 0) {
                bedIds.add(b.getBedId());
            }
        }
        for (Draft d : draftRows) {
            if (d.getBedId() != null && d.getBedId() > 0) {
                bedIds.add(d.getBedId());
            }
        }
        List<BedDetails> listBedDetails;
        if (floorRoomBedNameIncluded) {
            listBedDetails = bedsService.getBedDetails(bedIds);
        } else {
            listBedDetails = new ArrayList<>();
        }

        List<List<Object>> listTenants = customersList.stream()
        .map(i -> new TenantTableMapper(listBedDetails, listBookings, tableColumns, draftByCustomerId).apply(i))
        .collect(Collectors.toCollection(ArrayList::new));


        FilterOptions filterOptions = getTenantFilterOptions(hostelId);

        CustomerWebResponse response = new CustomerWebResponse(totalCustomers, currentPage, totalPages, size, summary, filterOptions, tableColumns, listColumns, listTenants);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private FilterOptions getTenantFilterOptions(String hostelId) {
        FilterOptions filterOptions = new FilterOptions();
        List<FilterOptions.FilterItems> statusFilterItems = new ArrayList<>();
        statusFilterItems.add(new FilterOptions.FilterItems("Draft", CustomerStatus.DRAFT.name()));
        statusFilterItems.add(new FilterOptions.FilterItems("Booked", CustomerStatus.BOOKED.name()));
        statusFilterItems.add(new FilterOptions.FilterItems("Checked In", CustomerStatus.CHECK_IN.name()));
        statusFilterItems.add(new FilterOptions.FilterItems("Settlement Generated", CustomerStatus.SETTLEMENT_GENERATED.name()));
        statusFilterItems.add(new FilterOptions.FilterItems("Vacated", CustomerStatus.VACATED.name()));
        statusFilterItems.add(new FilterOptions.FilterItems("Notice", CustomerStatus.NOTICE.name()));

        List<FilterOptions.FilterItems> listPeriods = new ArrayList<>();
        listPeriods.add(new FilterOptions.FilterItems("This Month", FilterKeywords.THIS_MONTH));
        listPeriods.add(new FilterOptions.FilterItems("Last Month", FilterKeywords.LAST_MONTH));
        listPeriods.add(new FilterOptions.FilterItems("Last 3 Months", FilterKeywords.LAST_3_MONTH));
        listPeriods.add(new FilterOptions.FilterItems("Last 6 Months", FilterKeywords.LAST_6_MONTH));
        listPeriods.add(new FilterOptions.FilterItems("Last 1 Year", FilterKeywords.LAST_1_YEAR));

        List<Rooms> listRooms = roomsService.findByHostelId(hostelId);

        List<Integer> roomsBySharing = listRooms.stream().filter(i -> i.getSharingType() != null && i.getSharingType() > 0).map(Rooms::getSharingType).distinct().sorted().toList();
        List<FilterOptions.FilterItems> listSharingType = new ArrayList<>();
        roomsBySharing.forEach(item -> {
            if (item == 1) {
                listSharingType.add(new FilterOptions.FilterItems("Single sharing", item.toString()));
            } else if (item == 2) {
                listSharingType.add(new FilterOptions.FilterItems("Two sharing", item.toString()));
            } else if (item == 3) {
                listSharingType.add(new FilterOptions.FilterItems("Three sharing", item.toString()));
            } else {
                listSharingType.add(new FilterOptions.FilterItems(item + " sharing", item.toString()));
            }
        });

        filterOptions.setPeriods(listPeriods);
        filterOptions.setSharingType(listSharingType);
        filterOptions.setStatus(statusFilterItems);

        return filterOptions;
    }

    /**
     * For Booking flow.
     * <p>
     * Do not use anywhere else
     *
     * @param payloads
     * @param hostelId
     * @return
     */

    public ResponseEntity<?> createBooking(BookingRequest payloads, String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (!floorsService.checkFloorExistForHostel(payloads.floorId(), hostelId)) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), hostelId)) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
        }
        Date dt = Utils.stringToDate(payloads.bookingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (bedsService.isBedAvailableNew(payloads.bedId(), user.getParentId(), payloads.joiningDate())) {
            Customers customers = customersRepository.findById(payloads.customerId()).orElse(null);
            if (customers != null) {
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                    return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }
                customers.setKycStatus(KycStatus.PENDING.name());
                customers.setCurrentStatus(CustomerStatus.BOOKED.name());
                customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
                customers.setCountry(1L);
                customers.setCreatedBy(user.getUserId());
                customers.setCreatedAt(new Date());
                customers.setHostelId(hostelId);

                customers.setExpJoiningDate(joiningDate);

                String invoiceId = invoiceService.addBookingInvoice(customers.getCustomerId(), payloads.bookingAmount(), InvoiceType.BOOKING.name(), hostelId, customers.getMobile(), customers.getEmailId(), payloads.bankId(), payloads.referenceNumber(), dt);
//                List<TransactionV1> transactions = transactionService.addBookingAmount(customers, payloads.bookingAmount());
//                customers.setTransactions(transactions);
                customersRepository.save(customers);

                bookingsService.addBooking(hostelId, payloads);

                AddPayment addPayment = new AddPayment(payloads.bankId(), payloads.bookingDate(), payloads.referenceNumber(), payloads.bookingAmount());
                transactionService.recordPaymentForBooking(hostelId, invoiceId, addPayment);
                userService.addUserLog(hostelId, customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.BOOKING, user);
                return bedsService.assignCustomer(payloads.bedId(), payloads.joiningDate().replace("/", "-"));
            } else {
                return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }


    }

    /**
     * for check in the customers
     * for customers who are not booked
     */

    public ResponseEntity<?> addCheckIn(String customerId, CheckInRequest payloads) {

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

            Advance advance = customers.getAdvance();
            double deductionAmount = 0.0;

            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            } else {
                listDeductions = advance.getDeductions();
                if (listDeductions == null) {
                    listDeductions = new ArrayList<>();
                }
            }

            listDeductions.addAll(payloads.deductions().stream().map(item -> new Deductions(item.type(), item.amount(), 0.0)).toList());

            deductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();

            advance.setAdvanceAmount(payloads.advanceAmount());
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

            bookingsService.addCheckin(customers, payloads);
            customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);

            if (payloads.advanceAmount() > 0) {
                invoiceService.addAdvanceInvoice(customerId, payloads.advanceAmount(), InvoiceType.ADVANCE.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, deductionAmount, listDeductions);
            }

//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.DAY_OF_MONTH, day);

//            Date startateOfCurrentCycle = cal.getTime();

            if (billingDates.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
                if (billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                    return setupJoiningDateBasisCheckin(currentBillDate, joiningDate, customers, user, payloads.rentalAmount());
                } else {
                    CheckInRequest invoiceRequest = getCurrentCycleInvoiceRequest(payloads, joiningDate, currentBillDate);
                    calculateRentAndCreateRentalInvoice(customers, invoiceRequest);
                    if (Utils.isCurrentMonth(joiningDate)) {
                        whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                    }
                    userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            } else {
                if (Utils.isCurrentMonth(joiningDate)) {
                    whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                }
                userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }


        } else {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }


    }

    private ResponseEntity<?> setupJoiningDateBasisCheckin(BillingDates currentBillDate, Date joiningDate, Customers customers, Users users, double rentalAmount) {
        if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) >= 0 && Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillEndDate()) <= 0) {
            //should generate the invoice
            //send welcome message on whatsapp
            invoiceService.createNewInvoiceCurrentMonthJoining(customers, joiningDate, rentalAmount, currentBillDate);
            if (!environment.equalsIgnoreCase(Utils.ENVIRONMENT_LOCAL) && Utils.isCurrentMonth(joiningDate)) {
                whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
            }

        } else if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
            invoiceService.createNewInvoiceCurrentMonthJoining(customers, currentBillDate.currentBillStartDate(), rentalAmount, currentBillDate);
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

    public ResponseEntity<?> checkinBookedCustomer(String customerId, CheckInBookedCustomer checkinRequest) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        Customers customers = customersRepository.findById(customerId).orElse(null);
        BookingsV1 booking = bookingsService.findByBookingId(checkinRequest.bookingId());
        if (booking == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
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

        if (!floorsService.checkFloorExistForHostel(booking.getFloorId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        if (!roomsService.checkRoomExistForFloor(booking.getFloorId(), booking.getRoomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.checkBedExistForRoom(booking.getBedId(), booking.getRoomId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
        }

        String date = checkinRequest.joiningDate().replace("/", "-");

        Date joiningDate = Utils.stringToDate(checkinRequest.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        if (Utils.compareWithTwoDates(joiningDate, booking.getBookingDate()) < 0) {
            return new ResponseEntity<>(Utils.JOINING_DATE_CANNOT_BEFORE_BOOKING, HttpStatus.BAD_REQUEST);
        }

        if (bedsService.checkAvailabilityForCheckIn(booking.getBedId(), joiningDate) != null) {

            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());

            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));

            Advance advance = customers.getAdvance();

            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            } else {
                listDeductions = advance.getDeductions();
            }
            listDeductions.addAll(checkinRequest.deductions().stream().map(item -> new Deductions(item.type(), item.amount(), 0.0)).toList());

            double deductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();

            advance.setAdvanceAmount(checkinRequest.advanceAmount());
            advance.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            advance.setCreatedBy(userId);
            advance.setCreatedAt(new Date());
            advance.setDeductions(listDeductions);
            advance.setInvoiceDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            advance.setUpdatedAt(new Date());
            advance.setCustomers(customers);

            customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            customers.setAdvance(advance);

            Customers savedCustomer = customersRepository.save(customers);

            bedsService.addUserToBed(booking.getBedId(), date, savedCustomer.getCustomerId());

            CheckInRequest request = new CheckInRequest(booking.getFloorId(), booking.getBedId(), booking.getRoomId(), checkinRequest.joiningDate(), checkinRequest.advanceAmount(), checkinRequest.rentalAmount(), checkinRequest.stayType(), checkinRequest.deductions(), checkinRequest.proRate());

            bookingsService.checkInBookedCustomer(customers, request);

            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), joiningDate);
            BillingDates currentBillDate = getCurrentBillDateForCheckin(hostelV1.getHostelId(), joiningDate, billingDates);

//            Calendar calendar = Calendar.getInstance();
//            int dueDate = calendar.get(Calendar.DAY_OF_MONTH) + 5;
//
//            int day = 1;
//            if (hostelV1.getElectricityConfig() != null) {
//                day = hostelV1.getElectricityConfig().getBillDate();
//            }

            if (checkinRequest.advanceAmount() > 0) {
                invoiceService.addAdvanceInvoice(customerId, checkinRequest.advanceAmount(), InvoiceType.ADVANCE.name(), booking.getHostelId(), customers.getMobile(), customers.getEmailId(), date, billingDates, deductionAmount, listDeductions);
            }


            bedsService.addUserToBed(booking.getBedId(), date, savedCustomer.getCustomerId());
            customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);

            if (billingDates.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
                if (billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                    return setupJoiningDateBasisCheckin(currentBillDate, joiningDate, customers, user, checkinRequest.rentalAmount());
                } else {
                    CheckInRequest invoiceRequest = getCurrentCycleInvoiceRequest(request, joiningDate, currentBillDate);
                    calculateRentAndCreateRentalInvoice(customers, invoiceRequest);
                    if (Utils.isCurrentMonth(joiningDate)) {
                        whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                    }
                    userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
                }
            } else {
                if (Utils.isCurrentMonth(joiningDate)) {
                    whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
                }
                userService.addUserLog(hostelV1.getHostelId(), savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }

//            calculateRentAndCreateRentalInvoice(customers, request);
//            whatsappService.sendWelcomeMessage(customers.getMobile(), customers.getFirstName());
//            userService.addUserLog(hostelV1.getHostelId(), customerId, ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN, user);
//            return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }


    }

    public ResponseEntity<?> addCustomer(String hostelId, MultipartFile profilePic, com.smartstay.smartstay.payloads.customer.AddCustomer customerInfo) {
        if (authentication.isAuthenticated()) {
            String loginId = authentication.getName();
            Users user = userService.findUserByUserId(loginId);

            if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
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

            if (customerInfo.emailId() != null && !customerInfo.emailId().isEmpty() && customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(customerInfo.emailId(), hostelId, List.of("VACATED"))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }

            if (customerInfo.mobileNumber() != null && !customerInfo.mobileNumber().isEmpty() && customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(customerInfo.mobileNumber(), hostelId, List.of("VACATED"))) {
                mobileStatus = Utils.MOBILE_NO_EXISTS;
            }

            if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                Map<String, String> map = Map.of("mobileStatus", mobileStatus, "emailStatus", emailStatus, "message", "Validation failed");
                return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
            }

            String profileImage = null;
            if (profilePic != null) {
                profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic), "users/profile");
            }

            Customers customers = new Customers();
            customers.setFirstName(customerInfo.firstName());
            customers.setLastName(customerInfo.lastName());
            customers.setCountry(1L);
            customers.setMobile(customerInfo.mobileNumber());
            customers.setEmailId(customerInfo.emailId());
            // Optional ID proof details; stored as-is (null when not provided).
            customers.setIdProofType(customerInfo.idProofType());
            customers.setIdProofNo(customerInfo.idProofNo());
            customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.INACTIVE.name());
            customers.setHostelId(hostelId);
            customers.setCreatedBy(loginId);
            customers.setCreatedAt(new Date());
            customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
            customers.setProfilePic(profileImage);

            if (customerInfo.address() != null) {
                if (Utils.checkNullOrEmpty(customerInfo.address().houseNo())) {
                    customers.setHouseNo(customerInfo.address().houseNo());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().street())) {
                    customers.setStreet(customerInfo.address().street());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().landmark())) {
                    customers.setLandmark(customerInfo.address().landmark());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().pincode())) {
                    customers.setPincode(customerInfo.address().pincode());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().state())) {
                    customers.setState(customerInfo.address().state());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().city())) {
                    customers.setCity(customerInfo.address().city());
                }
            }

            CustomerCredentials customerCredentials = ccs.addCustomerCredentials(customerInfo.mobileNumber());
            if (customerCredentials != null) {
                customers.setXuid(customerCredentials.getXuid());
            }

            Customers savedCustomers = customersRepository.save(customers);
            userService.addUserLog(hostelId, savedCustomers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CREATE, user);
            // Return the generated customerId only after the record is successfully persisted.
            return new ResponseEntity<>(
                    new CreateCustomerResponse(Utils.CUSTOMER_CREATED, savedCustomers.getCustomerId()),
                    HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> addCustomerPartialInfo(String hostelId, AddCustomerPartialInfo customerInfo, MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);
        String mobileStatus = "";
        String emailStatus = "";

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.WALK_IN.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(customerInfo.mobile(), hostelId, List.of("VACATED"))) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
//            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(customerInfo.emailId())) {
            if (customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(customerInfo.emailId(), hostelId, List.of("VACATED"))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }
        }

        if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
            AddCustomerError error = new AddCustomerError(mobileStatus, emailStatus);

            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(customerInfo.firstName());
        customers.setLastName(customerInfo.lastName());
        customers.setCountry(1L);
        customers.setMobile(customerInfo.mobile());
        customers.setEmailId(customerInfo.emailId());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.INACTIVE.name());
        customers.setHostelId(hostelId);
        customers.setCreatedBy(loginId);
        customers.setCreatedAt(new Date());
        customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
        customers.setProfilePic(profileImage);


        CustomerCredentials customerCredentials = ccs.addCustomerCredentials(customerInfo.mobile());
        if (customerCredentials != null) {
            customers.setXuid(customerCredentials.getXuid());
        }
        Customers savedCustomer = customersRepository.save(customers);
        userService.addUserLog(hostelId, savedCustomer.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CREATE, user);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateCustomerInfo(String customerId, UpdateCustomerInfo updateInfo, MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!userHostelService.checkHostelAccess(loginId, customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (updateInfo != null) {
//            if (updateInfo.mobile() != null && !updateInfo.mobile().equalsIgnoreCase("")) {
//                if (customersRepository.findCustomersByMobile(customers.getCustomerId(), updateInfo.mobile()) > 0) {
//                    return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
//                }
//                customers.setMobile(updateInfo.mobile());
//            }

            String profileImage = null;
            if (file != null) {
                profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
                customers.setProfilePic(profileImage);
            }

            if (updateInfo.firstName() != null && !updateInfo.firstName().equalsIgnoreCase("")) {
                customers.setFirstName(updateInfo.firstName());
            }
            if (updateInfo.lastName() != null && !updateInfo.lastName().equalsIgnoreCase("")) {
                customers.setLastName(updateInfo.lastName());
            } else {
                if (customers.getLastName() != null) {
                    customers.setLastName(null);
                }
            }
            if (updateInfo.mailId() != null && !updateInfo.mailId().equalsIgnoreCase("")) {
                customers.setEmailId(updateInfo.mailId());
            } else {
                if (customers.getEmailId() != null) {
                    customers.setEmailId(null);
                }
            }
            if (updateInfo.houseNo() != null && !updateInfo.houseNo().equalsIgnoreCase("")) {
                customers.setHouseNo(updateInfo.houseNo());
            } else {
                if (customers.getHouseNo() != null) {
                    customers.setHouseNo(null);
                }
            }
            if (updateInfo.street() != null && !updateInfo.street().equalsIgnoreCase("")) {
                customers.setStreet(updateInfo.street());
            } else {
                if (customers.getStreet() != null) {
                    customers.setStreet(null);
                }
            }
            if (updateInfo.landmark() != null && !updateInfo.landmark().equalsIgnoreCase("")) {
                customers.setLandmark(updateInfo.landmark());
            } else {
                if (customers.getLandmark() != null) {
                    customers.setLandmark(null);
                }
            }
            if (updateInfo.pincode() != null) {
                customers.setPincode(updateInfo.pincode());
            } else {
                customers.setPincode(0);
            }
            if (updateInfo.city() != null && !updateInfo.city().equalsIgnoreCase("")) {
                customers.setCity(updateInfo.city());
            } else {
                if (customers.getCity() != null) {
                    customers.setCity(null);
                }
            }
            if (updateInfo.state() != null && !updateInfo.state().equalsIgnoreCase("")) {
                customers.setState(updateInfo.state());
            } else {
                if (customers.getState() != null) {
                    customers.setState(null);
                }
            }

            if (updateInfo.mobile() != null && !updateInfo.mobile().equalsIgnoreCase("")) {
                int cus = customersRepository.findCustomerByMobileAndHostelId(customers.getHostelId(), customerId, updateInfo.mobile());
                if (cus > 0) {
                    return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
                }
                customers.setMobile(updateInfo.mobile());
            }

            if (updateInfo.mobile() != null && !updateInfo.mobile().equalsIgnoreCase("")) {
                CustomerCredentials cc = ccs.updateCustomerMobile(updateInfo.mobile(), customers.getXuid());
                if (cc != null) {
                    customers.setXuid(cc.getXuid());
                }
            }

            customersRepository.save(customers);
            userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.UPDATE, user);
            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> requestNotice(String hostelId, CheckoutNotice checkoutNotice) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (checkoutNotice.customerId() == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customers = customersRepository.findById(checkoutNotice.customerId()).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ON_NOTICE, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 booking = bookingsService.getBookingsByCustomerId(checkoutNotice.customerId());
        if (booking == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }
        Date joiningDate = booking.getJoiningDate();
        Date requestDate = Utils.stringToDate(checkoutNotice.requestDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date checkoutDate = Utils.stringToDate(checkoutNotice.checkoutDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (Utils.compareWithTwoDates(requestDate, joiningDate) < 0) {
            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(checkoutDate, joiningDate) < 0) {
            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
//        if (Utils.compareWithTwoDates(checkoutDate, billingDates.currentBillStartDate()) < 0) {
//            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_BILLING_START_DATE + Utils.dateToString(billingDates.currentBillStartDate()), HttpStatus.BAD_REQUEST);
//        }
//
//        if (Utils.compareWithTwoDates(checkoutDate, requestDate) < 0) {
//            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_REQUEST_DATE, HttpStatus.BAD_REQUEST);
//        }

        customers.setCurrentStatus(CustomerStatus.NOTICE.name());


        bedsService.updateBedToNotice(bookingsService.getBedIdFromBooking(customers.getCustomerId(), hostelId), checkoutNotice.checkoutDate());
        bookingsService.moveToNotice(customers.getCustomerId(), checkoutNotice.checkoutDate(), checkoutNotice.requestDate(), checkoutNotice.reason());
        customersRepository.save(customers);

        userService.addUserLog(hostelId, customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.NOTICE, user);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public Customers getCustomerInformation(String customerId) {
        return customersRepository.findById(customerId).orElse(null);
    }

    public Customers markCustomerInactive(Customers customers) {
        customers.setCurrentStatus(CustomerStatus.CANCELLED_BOOKING.name());
        customers.setLastUpdatedAt(new Date());
        customers.setUpdatedBy(authentication.getName());

        return customersRepository.save(customers);
    }

    public ResponseEntity<?> getCustomerDetails(String customerId) {
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

        String fullName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        String initials = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());

        boolean isNewRentAvailable = false;
        double newRentAmount = 0.0;
        String newRentLabelHint = null;
        List<RentHistory> rentHistories = bookingsService.getNewRentAmount(customerId, new Date());
        CustomersBookingDetails bookingDetails = bookingsService.getCustomerBookingDetails(customers.getCustomerId());
        List<InvoiceResponse> invoiceResponseList = invoiceService.getInvoiceResponseList(customers.getCustomerId());
        boolean isSettlementGenerated = CustomerStatus.SETTLEMENT_GENERATED.name().equalsIgnoreCase(customers.getCurrentStatus());
        invoiceResponseList = invoiceResponseList.stream().map(inv -> {
            boolean isPaidOrPartial = "PAID".equalsIgnoreCase(inv.paymentStatus())
                    || "PARTIAL_PAYMENT".equalsIgnoreCase(inv.paymentStatus());
            boolean canUnpaid = isPaidOrPartial && !isSettlementGenerated;
            boolean isCancelled = inv.isCancelled();
            String cancelledDate = null;
            if (inv.isCancelled()) {
                cancelledDate = inv.cancelledOn();
            }
            return new InvoiceResponse(inv.invoiceId(), inv.invoiceNumber(), inv.invoiceType(), inv.invoiceDate(), inv.paymentStatus(), inv.totalAmount(), inv.dueAmount(), inv.paidAmount(), inv.dueDate(), inv.invoiceGeneratedDate(), inv.invoiceMode(), inv.isDiscounted(), inv.items(), canUnpaid, isCancelled, cancelledDate);
        }).toList();
        InvoiceResponse advanceInvoice = invoiceResponseList.stream().filter(inv -> "ADVANCE".equalsIgnoreCase(inv.invoiceType())).limit(1).findFirst().orElse(null);
        if (rentHistories != null) {
            List<RentHistory> listNewRentHistory = rentHistories.stream().filter(i -> Utils.compareWithTwoDates(i.getStartsFrom(), new Date()) > 0).toList();
            if (listNewRentHistory != null && !listNewRentHistory.isEmpty()) {
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
        List<Deductions> listDeduction = null;
        AdvanceInfo advanceInfo = null;
        List<Deductions> otherDeductionBreakup = null;
        String bookingId = null;
        double maintenance = 0;
        double otherDeductions = 0;
        double advanceAmount = 0;
        double netAdvanceAmount = 0;
        if (advance != null) {
            advanceAmount = advance.getAdvanceAmount();
            listDeduction = advance.getDeductions();
            if (listDeduction != null) {
                maintenance = listDeduction.stream().filter(item -> item.getType().equalsIgnoreCase("maintenance")).mapToDouble(Deductions::getAmount).sum();
                otherDeductions = listDeduction.stream().filter(item -> !item.getType().equalsIgnoreCase("maintenance")).mapToDouble(Deductions::getAmount).sum();
                otherDeductionBreakup = listDeduction.stream().filter(item -> !item.getType().equalsIgnoreCase("maintenance")).collect(Collectors.toList());

            }

            double totalDeductions = maintenance + otherDeductions;
            netAdvanceAmount = advanceAmount - totalDeductions;

        }
        if (bookingDetails != null) {
            bookingId = bookingDetails.getBookingId();
            advanceInfo = toAdvanceInfoResponse(advance, advanceInvoice, bookingDetails.getBookingAmount());

            hostelInformation = new HostelInformation(bookingDetails.getRoomName(), bookingDetails.getRoomId(), bookingDetails.getFloorName(), bookingDetails.getFloorId(), bookingDetails.getBedName(), bookingDetails.getBedId(), Utils.dateToString(bookingDetails.getJoiningDate()), bookingDetails.getCurrentStatus(), Utils.roundOffWithTwoDigit(netAdvanceAmount), otherDeductions, maintenance, bookingDetails.getRentAmount(), otherDeductionBreakup);

            if (bookingDetails.getIsBooked() != null && bookingDetails.getIsBooked()) {
                BookingsV1 bookingV1 = bookingsService.getBookingsByCustomerId(customerId);
                if (bookingV1 != null) {

                    CustomersBedHistory cbh = bedHistory.getCustomerBookedBed(bookingV1.getCustomerId());

                    BedDetails bedDetails = bedsService.getBedDetails(cbh.getBedId());
                    String bookedBedName = null;
                    String bookedFloorName = null;
                    String bookedRoomName = null;

                    if (bedDetails != null) {
                        bookedBedName = bedDetails.getBedName();
                        bookedFloorName = bedDetails.getFloorName();
                        bookedRoomName = bedDetails.getRoomName();
                    }

                    bookingInfo = new BookingInfo(Utils.dateToString(bookingV1.getBookingDate()), bookingV1.getBookingAmount(), bookedBedName, bookedFloorName, bookedRoomName);
                }
            }

        }

        CustomerAddress address = new CustomerAddress(customers.getStreet(), customers.getHouseNo(), customers.getLandmark(), customers.getPincode(), customers.getCity(), customers.getState());
        KycDetails kycDetails = customers.getKycDetails();
        KycInformations kycInfo = null;
        String kycDocumentFromDigio = null;
        if (kycDetails == null) {
            kycInfo = new KycInformations("PENDING", null, null, null, null,  null, null, null, null, null);
        }
        else {
            if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.REQUESTED.name()) || kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.WAITING_FOR_APPROVAL.name())) {
                kycDetails = kycServices.verifyStatus(customers);
            }
            if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
                com.smartstay.smartstay.dao.KycAddressDetails addressDetails = kycDetails.getAddressDetails();
                KycAddressDetails currentAddress = null;
                KycAddressDetails permanentAddress = null;
                if (addressDetails != null) {
                    String houseNumber = null;
                    String permanentStreetName = null;
                    String currentStreetName = null;

                    String permanentAddressHouseNumber = null;
                    String[] currentAddressArray = new String[0];
                    String[] permanentAddressArray = new String[0];

                    if (addressDetails.getCurrentAddress() != null) {
                        currentAddressArray = addressDetails.getCurrentAddress().split(",");
                        if (currentAddressArray.length > 1) {
                            houseNumber = currentAddressArray[1];
                        }
                        if (currentAddressArray.length > 2) {
                            currentStreetName = currentAddressArray[2];
                        }
                    }

                    if (addressDetails.getPermanentAddress() != null) {
                        permanentAddressArray = addressDetails.getPermanentAddress().split(",");
                        if (permanentAddressArray.length > 1) {
                            permanentAddressHouseNumber = permanentAddressArray[1];
                        }

                        if (permanentAddressArray.length > 2) {
                            permanentStreetName = permanentAddressArray[2];
                        }
                    }

                    currentAddress =  new KycAddressDetails(houseNumber, currentStreetName, addressDetails.getCurrentLocality(), addressDetails.getCurrentState(), addressDetails.getCurrentPincode(), addressDetails.getCurrentAddress());
                    permanentAddress = new KycAddressDetails(permanentAddressHouseNumber, permanentStreetName, addressDetails.getPermanentLocality(), addressDetails.getPermanentState(), addressDetails.getPermanentPincode(), addressDetails.getPermanentAddress());
                }

                kycDocumentFromDigio = kycDetails.getKycDocument();
                kycInfo = new KycInformations(KycStatus.VERIFIED.name(),
                        kycDetails.getIdPic(),
                        kycDetails.getAadhaarNumber(),
                        kycDetails.getNameInDocument(),
                        kycDetails.getDateOfBirth(),
                        Utils.dateToString(kycDetails.getCompletedAt()),
                        kycDetails.getKycDocument(),
                        kycDetails.getKycDocumentType(),
                        currentAddress,
                        permanentAddress);
            }
            else if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.WAITING_FOR_APPROVAL.name())) {
                kycInfo = new KycInformations(KycStatus.REQUESTED.name(), null, null, null, null, null, null, null, null, null);
            }
            else {
                kycInfo = new KycInformations(KycStatus.REQUESTED.name(), null, null, null,null, null, null, null, null, null);
            }

        }

        CheckoutInfo checkoutInfo = null;
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getCheckoutDate());
            checkoutInfo = new CheckoutInfo(Utils.dateToString(bookingDetails.getCheckoutDate()), Utils.dateToString(bookingDetails.getRequestedCheckoutDate()), Utils.dateToString(bookingDetails.getNoticeDate()), noticeDays, null);
        } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getRequestedCheckoutDate());
            checkoutInfo = new CheckoutInfo(null, Utils.dateToString(bookingDetails.getRequestedCheckoutDate()), Utils.dateToString(bookingDetails.getNoticeDate()), noticeDays, null);
        } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getRequestedCheckoutDate());
            checkoutInfo = new CheckoutInfo(null, Utils.dateToString(bookingDetails.getRequestedCheckoutDate()), Utils.dateToString(bookingDetails.getNoticeDate()), noticeDays, null);
        }


        List<BedHistory> listBeds = bedHistory.getCustomersBedHistory(customers.getCustomerId());
        List<Amenities> amenities = amenitiesService.getAmenitiesByCustomerId(customerId);
        List<TransactionDto> listTransactions = transactionService.getTranactionInfoByCustomerId(customerId);
        List<AmenityRequestDTO> listRequestedAmenities = amenityRequestService.getRequestedAmenities(customerId, customers.getHostelId());
        List<AvailableAmenities> listAvailableAmenities = amenitiesService.getAvailableAmenitiesExceptAvailed(customers.getHostelId(), amenities, listRequestedAmenities);
        List<String> invoicesIds = listTransactions.stream().map(TransactionDto::invoiceId).toList();
        Set<String> bankIds = listTransactions.stream().map(TransactionDto::bankId).collect(Collectors.toSet());
        List<InvoicesV1> listOfInvoices = invoiceService.findInvoices(invoicesIds);
        List<BankingV1> listOFBankings = bankingService.findAllBanksById(bankIds);
        List<String> userIds = listOFBankings.stream().map(BankingV1::getUserId).toList();
        List<Users> listUsers = userService.findAllUsersFromUserId(userIds);


        List<com.smartstay.smartstay.responses.customer.TransactionDto> listTransactionResponse = listTransactions.stream().map(i -> new TransctionsForCustomerDetails(listOfInvoices, listOFBankings, listUsers).apply(i)).toList();

        List<WalletTransactions> walletTransactions = customerWalletHistoryService.getWalletTransactions(customerId);
        double walletAmount = 0.0;
        if (customers.getWallet() != null) {
            if (customers.getWallet().getAmount() != null) {
                walletAmount = Utils.roundOffWithTwoDigit(customers.getWallet().getAmount());
            }
        }

        WalletInfo walletInfo = new WalletInfo(walletAmount, walletTransactions);
        CustomerFiles customerFiles = customerDocumentsService.getCustomerFiles(customerId, kycDocumentFromDigio);
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

        List<EffectiveMonth> effectiveFromMonth = null;
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());
        if (billingDates.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
            billingDates = hostelService.getJoiningBasedCurrentMonthBillingDate(customers.getJoiningDate(), customers.getHostelId(), new Date());
        }
        if (billingDates != null && billingDates.currentBillStartDate() != null) {
            int billingStartDay = Utils.findDateFromDate(billingDates.currentBillStartDate());
            effectiveFromMonth = Utils.getEffectiveBillingMonths(new Date(), customers.getJoiningDate(), billingStartDay);
        }

        CustomerDetails details = new CustomerDetails(customers.getCustomerId(), customers.getHostelId(), customers.getFirstName(), customers.getLastName(), fullName, customers.getEmailId(),
                customers.getMobile(), "91", initials, customers.getProfilePic(),
                bookingId, isNewRentAvailable, newRentAmount, newRentLabelHint,
                customers.getCurrentStatus(), address, hostelInformation,
                kycInfo, advanceInfo, checkoutInfo, bookingInfo,
                invoiceResponseList, listBeds, listTransactionResponse, amenities,
                listRequestedAmenities, listAvailableAmenities, walletInfo, customerFiles, additionalContacts,
                isJoiningDateEditable, createdDate, createdTime, createdAt, createdBy,
                createdByName, createdByInitials, createdByPic, effectiveFromMonth,
                customers.getIdProofType(), customers.getIdProofNo());

        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    public void calculateRentAndCreateRentalInvoice(Customers customers, CheckInRequest payloads) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 != null) {

            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

            BillingDates billingDates = hostelService.getBillingRuleOnDate(customers.getHostelId(), joiningDate);


            Calendar c = Calendar.getInstance();
            c.setTime(joiningDate);

            if (billingDates.hasGracePeriod()) {
                Date gracePeriodEndingDate = Utils.addDaysToDate(billingDates.currentBillStartDate(), billingDates.gracePeriodDays() - 1);
                if (payloads.proRate() != null && payloads.proRate()) {
                    long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                    long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
                    double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
                    double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
                    if (finalRent > payloads.rentalAmount()) {
                        finalRent = payloads.rentalAmount();
                    }

                    invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, 0);
                } else {
                    if (Utils.compareWithTwoDates(joiningDate, gracePeriodEndingDate) <= 0) {
                        double finalRent = payloads.rentalAmount();
                        invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, 0);
                    } else {
                        long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                        long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
                        double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
                        double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
                        if (finalRent > payloads.rentalAmount()) {
                            finalRent = payloads.rentalAmount();
                        }
                        invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, 0);

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

                invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates, 0);

            }
        }

    }

    public ResponseEntity<?> getInformationForFinalSettlement(String customerId, String leavingDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        BookingsV1 bookingDetails = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingDetails == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        return generateFinalSettlementCommon(customerId, bookingDetails, customers, leavingDate);
    }

    public ResponseEntity<?> getInformationForFinalSettlementNew(String customerId, String leavingDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        BookingsV1 bookingDetails = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingDetails == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        return generateFinalSettlementCommon(customerId, bookingDetails, customers, leavingDate);
    }

    /**
     * this is the common function to calculate settlement amount with EB
     * calculations.
     *
     * @param customerId
     * @param bookingDetails
     * @param customers
     * @param leavingDate
     * @return
     */
    private ResponseEntity<?> generateFinalSettlementCommon(String customerId, BookingsV1 bookingDetails, Customers customers, String leavingDate) {
        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());
        Date lDate = null;
        if (leavingDate != null) {
            lDate = Utils.stringToDate(leavingDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (billDate.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                BillingDates currentBillStartAndEndDates = hostelService.getJoiningBasedCurrentMonthBillingDate(customers.getJoiningDate(), customers.getHostelId(), new Date());
                if (Utils.compareWithTwoDates(lDate, currentBillStartAndEndDates.currentBillStartDate()) < 0) {
                    return new ResponseEntity<>(Utils.OLD_BILLING_CYCLE_SETTLEMENT_GENERATION_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (lDate != null) {
            if (Utils.compareWithTwoDates(lDate, new Date()) > 0) {
                return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
            }
//             if (Utils.compareWithTwoDates(lDate, billDate.currentBillStartDate()) < 0) {
//                    return new ResponseEntity<>(Utils.OLD_BILLING_CYCLE_SETTLEMENT_GENERATION_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
//            }
        } else {
            lDate = new Date();
        }
        if (bookingDetails.getNoticeDate() != null) {
            if (Utils.compareWithTwoDates(bookingDetails.getNoticeDate(), lDate) > 0) {
                return new ResponseEntity<>(Utils.CANNOT_GENERATE_FINAL_SETTLEMENT_INVALID_NOTICE_DATE, HttpStatus.BAD_REQUEST);
            }
        }

        CustomersBedHistory cbh = bedHistory.getLatestCustomerBed(customerId);
        if (Utils.compareWithTwoDates(cbh.getStartDate(), lDate) > 0) {
            return new ResponseEntity<>(Utils.CANNOT_GENERATE_FINAL_SETTLEMENT_PREVIOUS_HISTORY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        if (!billDate.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
            if (billDate.billingModel().equalsIgnoreCase(BillingModel.POSTPAID.name())) {
                //done
                return getInformationForPostpaidSettlements(customers, lDate, bookingDetails, billDate);
            }
        } else {
            if (billDate.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
                //done
                return getFinalSettlementInfoFotJoiningBasedPrepaid(customers, lDate, bookingDetails);
            }
        }


        if (Utils.compareWithTwoDates(cbh.getStartDate(), billDate.currentBillStartDate()) > 0) {
            settlementDetailsService.addSettlementForCustomer(customerId, lDate);
            //done
            FinalSettlement finalSettlement = getFinalSettlementInfoForBedChange(customers, bookingDetails, billDate, lDate);

            return new ResponseEntity<>(finalSettlement, HttpStatus.OK);
        }

        settlementDetailsService.addSettlementForCustomer(customerId, lDate);
        FinalSettlement finalSettlement = getFinalSettlementForPrepaidFixed(customers, bookingDetails, billDate, lDate);

        return new ResponseEntity<>(finalSettlement, HttpStatus.OK);
    }

    //no bed change
    private FinalSettlement getFinalSettlementForPrepaidFixed(Customers customers, BookingsV1 bookingDetails, BillingDates billDate, Date lDate) {
        Double amountToBePaid = 0.0;
        Double payableRent = 0.0;
        Double refundableAdvance = 0.0;
        Double ebAmount = 0.0;
        Double unpaidInvoiceAmount = 0.0;
        boolean isRefundable = false;
        String label = null;
        Double payableAmount = 0.0;

        boolean isAdvancePaid = false;
        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        InvoicesV1 bookingInvoice = invoiceService.getBookingInvoice(customers.getCustomerId(), customers.getHostelId());

        double bookingAmount = 0.0;
        double advanceAmount = 0.0;
        double availableAdvanceAmount = 0.0;
        double availableBookingAmount = 0.0;

        double totalAdvanceAmount = 0.0;
        double totalAmountToRedeem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;
        DeductionsInfo deductionsInfo = null;
        double deductionAmount = 0.0;


        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    advanceAmount = advanceInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount + advanceInvoice.getPaidAmount();
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmount = advanceInvoice.getBalanceAmount();
                    totalAmountToRedeem = totalAmountToRedeem + advanceInvoice.getBalanceAmount();
                }
            }
            if (advanceInvoice.getDeductions() != null && advanceInvoice.getDeductionAmount() != null && advanceInvoice.getDeductionAmount() > 0) {
                double paidDeductionAmount = 0.0;
                double totalDeductionAmount = 0.0;
                double pendingDeductionAmount = 0.0;
                List<Deductions> listDeductions = advanceInvoice.getDeductions();
                if (listDeductions != null) {

                    List<DeductionsItem> listDeductionItem = listDeductions.stream().filter(i -> i.getPaidAmount() == null || i.getPaidAmount() < i.getAmount()).map(i -> {
                        double pendingAmount = 0.0;
                        if (i.getPaidAmount() != null) {
                            pendingAmount = i.getAmount() - i.getPaidAmount();
                        }
                        return new DeductionsItem(i.getType(), i.getPaidAmount(), i.getAmount(), pendingAmount);
                    }).toList();
                    paidDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getPaidAmount).sum();
                    totalDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();
                    pendingDeductionAmount = totalDeductionAmount - paidDeductionAmount;
                    deductionAmount = totalDeductionAmount - paidDeductionAmount;

                    deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidDeductionAmount, pendingDeductionAmount, listDeductionItem);
                }
            }

        }

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    bookingAmount = bookingInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmount = bookingInvoice.getBalanceAmount();
                    totalAmountToRedeem = totalAmountToRedeem + bookingInvoice.getBalanceAmount();
                }
            }
        }

        CustomerInformations customerInformations = getCustomerInformations(customers, bookingDetails);
        StayInfo stayInfo = bookingsService.getStayInfo(customers, bookingDetails, lDate);
        EBInfo ebInfo = electricityService.getEbInfoForSettlement(customers, customers.getHostelId(), lDate);
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoices = invoiceService.getUnpaidInvoicesInfo(customers.getCustomerId(), customers.getHostelId(), lDate);
        RentInfo currentMonthRentInfo = getRentInfo(customers.getHostelId(), customers, lDate, bookingDetails.getRentAmount());
        AdvanceItems advanceItems = invoiceService.getRedeemedListFromAdvance(customers.getHostelId(), customers.getCustomerId());
        AdvanceItems bookingItems = invoiceService.getRedeemedListFromBookings(customers.getHostelId(), customers.getCustomerId());
        List<com.smartstay.smartstay.dto.wallet.WalletTransactions> listWallets = customerWalletHistoryService.getInvoicePendingByCustomerId(customers.getCustomerId());

        double walletAmount = 0.0;
        if (customers.getWallet() != null) {
            if (customers.getWallet().getAmount() != null) {
                walletAmount = customers.getWallet().getAmount();
            }
        }

        if (currentMonthRentInfo != null) {
            if (currentMonthRentInfo.currentRentPaid() > currentMonthRentInfo.currentPayableRent()) {
                label = "Refundable rent";
                payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
            } else {
                label = "Payable rent";
                payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
            }
        }


        if (ebInfo != null) {
            ebAmount = ebInfo.pendingEbAmount();
        }

        com.smartstay.smartstay.dto.wallet.WalletInfo walletInfo = new com.smartstay.smartstay.dto.wallet.WalletInfo(Utils.roundOffWithTwoDigit(walletAmount), listWallets);

        unpaidInvoiceAmount = unpaidInvoices.unpaidAmount();
        amountToBePaid = unpaidInvoices.invoiceTotalAmount() + ebAmount + walletAmount;
        double paidAmount = unpaidInvoices.paidAmount();
        payableRent = unpaidInvoices.unpaidAmount();
        if (currentMonthRentInfo != null) {
            amountToBePaid = amountToBePaid + currentMonthRentInfo.currentMonthTotalAmount();
            paidAmount = paidAmount + currentMonthRentInfo.currentRentPaid();
            payableRent = payableRent + currentMonthRentInfo.currentMonthPayableAmount();
        }

        amountToBePaid = amountToBePaid - paidAmount;
        amountToBePaid = amountToBePaid + deductionAmount;
        if (isAdvancePaid) {
            amountToBePaid = amountToBePaid - totalAmountToRedeem;
        }
        if (amountToBePaid < 0) {
            isRefundable = true;
        }
        double totalRefundableAdvance = 0.0;
        if (isAdvancePaid) {
            totalRefundableAdvance = totalAmountToRedeem;
        }
        SettlementInfo settlementInfo = new SettlementInfo(Utils.roundOffWithTwoDigit(amountToBePaid), deductionAmount, Utils.roundOffWithTwoDigit(payableRent), Utils.roundOffWithTwoDigit(payableRent), Utils.roundOffWithTwoDigit(totalRefundableAdvance), Utils.roundOffWithTwoDigit(ebAmount), Utils.roundOffWithTwoDigit(unpaidInvoiceAmount), isRefundable, label, Utils.roundOffWithTwoDigit(payableAmount));

        return new FinalSettlement(customerInformations, stayInfo, ebInfo, null, unpaidInvoices, currentMonthRentInfo, walletInfo, advanceItems, bookingItems, deductionsInfo, settlementInfo);

    }


    private ResponseEntity<?> getInformationForPostpaidSettlements(Customers customers, Date lDate, BookingsV1 bookingDetails, BillingDates currentMonthBillingDates) {
        settlementDetailsService.addSettlementForCustomer(customers.getCustomerId(), lDate);
        FinalSettlement settlement = getFinalSettlementInfo(customers, lDate, bookingDetails, currentMonthBillingDates);
        return new ResponseEntity<>(settlement, HttpStatus.OK);
    }

    private FinalSettlement getFinalSettlementInfo(Customers customers, Date leavingDate, BookingsV1 bookingsV1, BillingDates currentMonthBillingDates) {
        boolean isAdvancePaid = false;
        double advancePaidAmount = 0.0;
        double bookingAmount = 0.0;
        double totalAdvanceAmount = 0.0;
        double totalAdvancePaid = 0.0;
        double availableAdvanceAmountToReddem = 0.0;
        double availableBookingAmountToRedeem = 0.0;
        double availableTotalAmountToReddem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;

        DeductionsInfo deductionsInfo = null;
        double deductionAmount = 0.0;

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        InvoicesV1 bookingInvoice = invoiceService.getBookingInvoice(customers.getCustomerId(), customers.getHostelId());

        if (advanceInvoice != null) {
            advanceAmountRedeemedFromBookingInvoice = invoiceService.getAdvanceAmountFromBookingInvoice(advanceInvoice.getHostelId(), advanceInvoice.getInvoiceId());
            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    advancePaidAmount = advanceInvoice.getPaidAmount();
                    isAdvancePaid = true;
                    totalAdvancePaid = totalAdvancePaid + advanceInvoice.getPaidAmount();
                    totalAdvancePaid = totalAdvancePaid - advanceAmountRedeemedFromBookingInvoice;
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmountToReddem = advanceInvoice.getBalanceAmount();
                    availableTotalAmountToReddem = availableTotalAmountToReddem + advanceInvoice.getBalanceAmount();
                }
            }

            if (advanceInvoice.getDeductions() != null && advanceInvoice.getDeductionAmount() != null && advanceInvoice.getDeductionAmount() > 0) {
                double paidDeductionAmount = 0.0;
                double totalDeductionAmount = 0.0;
                double pendingDeductionAmount = 0.0;
                List<Deductions> listDeductions = advanceInvoice.getDeductions();
                if (listDeductions != null) {

                    List<DeductionsItem> listDeductionItem = listDeductions.stream().filter(i -> i.getPaidAmount() == null || i.getPaidAmount() < i.getAmount()).map(i -> {
                        double pendingAmount = 0.0;
                        if (i.getPaidAmount() != null) {
                            pendingAmount = i.getAmount() - i.getPaidAmount();
                        }
                        return new DeductionsItem(i.getType(), i.getPaidAmount(), i.getAmount(), pendingAmount);
                    }).toList();
                    paidDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getPaidAmount).sum();
                    totalDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();
                    pendingDeductionAmount = totalDeductionAmount - paidDeductionAmount;
                    deductionAmount = totalDeductionAmount - paidDeductionAmount;

                    deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidDeductionAmount, pendingDeductionAmount, listDeductionItem);
                }
            }
        }

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    bookingAmount = bookingInvoice.getPaidAmount();
                    isAdvancePaid = true;
                    totalAdvancePaid = totalAdvancePaid + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmountToRedeem = bookingInvoice.getBalanceAmount();
                    availableTotalAmountToReddem = availableTotalAmountToReddem + bookingInvoice.getBalanceAmount();
                }
            }
        }

        AvailableAmountToRedeem availableAmountToRedeem = new AvailableAmountToRedeem(availableAdvanceAmountToReddem, availableBookingAmountToRedeem, availableTotalAmountToReddem);

        CustomerInformations customerInformations = new CustomerInformations(customers.getCustomerId(), customers.getFirstName(), customers.getLastName(), NameUtils.getFullName(customers.getFirstName(), customers.getLastName()), customers.getProfilePic(), NameUtils.getInitials(customers.getFirstName(), customers.getLastName()), "91", customers.getMobile(), Utils.dateToString(bookingsV1.getJoiningDate()), customers.getAdvance().getAdvanceAmount(), bookingsV1.getRentAmount(), isAdvancePaid, totalAdvancePaid, bookingAmount, availableAmountToRedeem);

        StayInfo stayInfo = bookingsService.getStayInfo(customers, bookingsV1, leavingDate);
        EBInfo ebInfo = electricityService.getEbInfoForSettlement(customers, customers.getHostelId(), leavingDate);
        List<UnpaidInvoices> listUnpaidInvoices = invoiceService.getUnpaidInvoices(customers.getCustomerId(), customers.getHostelId(), leavingDate);
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoicesInfo = invoiceService.getUnpaidInvoicesInfo(customers.getCustomerId(), customers.getHostelId(), leavingDate);
//        RentInfo currentMonthRentInfo = getRentInfo(customers.getHostelId(), customers, leavingDate, bookingsV1.getRentAmount());
        RentInfo currentMonthRentInfo = getRentInfoForPostpaidHostels(customers, bookingsV1, leavingDate, currentMonthBillingDates);
        AdvanceItems advanceItems = invoiceService.getRedeemedListFromAdvance(customers.getHostelId(), customers.getCustomerId());
        AdvanceItems bookingItems = invoiceService.getRedeemedListFromBookings(customers.getHostelId(), customers.getCustomerId());
        String label = null;
        double payableAmount = 0.0;
        if (currentMonthRentInfo.currentRentPaid() > currentMonthRentInfo.currentPayableRent()) {
            label = "Refundable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        } else {
            label = "Payable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        }
        double walletAmount = 0.0;
        if (customers.getWallet() != null) {
            CustomerWallet wallet = customers.getWallet();
            if (wallet.getAmount() != null) {
                walletAmount = wallet.getAmount();
            }
        }

        double ebAmount = 0.0;
        if (ebInfo != null) {
            ebAmount = ebInfo.pendingEbAmount();
        }
        List<com.smartstay.smartstay.dto.wallet.WalletTransactions> listWallets = customerWalletHistoryService.getInvoicePendingByCustomerId(customers.getCustomerId());

        com.smartstay.smartstay.dto.wallet.WalletInfo walletInfo = new com.smartstay.smartstay.dto.wallet.WalletInfo(Utils.roundOffWithTwoDigit(walletAmount), listWallets);

        double totalAmountToBePaid = unpaidInvoicesInfo.invoiceTotalAmount() + ebAmount + walletInfo.walletAmount() + currentMonthRentInfo.currentMonthPayableAmount();
        double paidAmount = unpaidInvoicesInfo.paidAmount() + currentMonthRentInfo.currentRentPaid();

        double payableRent = unpaidInvoicesInfo.unpaidAmount() + currentMonthRentInfo.currentMonthPayableAmount();
//        if (customers.getAdvance() != null) {
//            Advance advance = customers.getAdvance();
//            if (advance.getDeductions() != null) {
//                totalDeductions = advance.getDeductions().stream().mapToDouble(i -> i.getAmount()).sum();
//            }
//        }

        totalAmountToBePaid = totalAmountToBePaid - availableTotalAmountToReddem - paidAmount;
        totalAmountToBePaid = totalAmountToBePaid + deductionAmount;
        if (isAdvancePaid) {
            totalAmountToBePaid = totalAmountToBePaid - totalAdvanceAmount;
        }
        boolean isRefundable = totalAmountToBePaid < 0;

        double totalRefundableAdvance = 0.0;
        if (isAdvancePaid) {
            totalRefundableAdvance = availableTotalAmountToReddem;
        }


        SettlementInfo settlementInfo = new SettlementInfo((double) Math.round(totalAmountToBePaid), deductionAmount, payableRent, Utils.roundOffWithTwoDigit(deductionAmount), Utils.roundOffWithTwoDigit(totalRefundableAdvance), Utils.roundOffWithTwoDigit(ebAmount), Utils.roundOfDouble(unpaidInvoicesInfo.unpaidAmount()), isRefundable, label, Utils.roundOffWithTwoDigit(payableAmount));

        return new FinalSettlement(customerInformations, stayInfo, ebInfo, listUnpaidInvoices, unpaidInvoicesInfo, currentMonthRentInfo, walletInfo, advanceItems, bookingItems, deductionsInfo, settlementInfo);
    }

    private ResponseEntity<?> getFinalSettlementInfoFotJoiningBasedPrepaid(Customers customers, Date lDate, BookingsV1 bookingDetails) {
        settlementDetailsService.addSettlementForCustomer(customers.getCustomerId(), lDate);
        FinalSettlement settlement = getSettlementInfoForJoiningBased(customers, lDate, bookingDetails);
        return new ResponseEntity<>(settlement, HttpStatus.OK);
    }

    private FinalSettlement getSettlementInfoForJoiningBased(Customers customers, Date leavingDate, BookingsV1 bookingsV1) {
        BillingDates currentMonthBillingDates = hostelService.getJoiningBasedCurrentMonthBillingDate(customers.getJoiningDate(), customers.getHostelId(), leavingDate);
        boolean isAdvancePaid = false;
        double advancePaidAmount = invoiceService.invoicesPaidAmountByType(customers.getCustomerId(), InvoiceType.ADVANCE.name());
        double bookingAmount = invoiceService.invoicesPaidAmountByType(customers.getCustomerId(), InvoiceType.BOOKING.name());
        double totalAdvanceAmount = 0.0;
        double totalAdvancePaid = 0.0;

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        InvoicesV1 bookingInvoice = invoiceService.getBookingInvoice(customers.getCustomerId(), customers.getHostelId());

        double advanceAmount = 0.0;
        double availableAdvanceAmount = 0.0;
        double availableBookingAmount = 0.0;
        DeductionsInfo deductionsInfo = null;
        double deductionAmount = 0.0;

        double totalAmountToRedeem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;


        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    advanceAmount = advanceInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount + advanceInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount - advanceAmountRedeemedFromBookingInvoice;
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmount = advanceInvoice.getBalanceAmount();
                    totalAmountToRedeem = totalAmountToRedeem + advanceInvoice.getBalanceAmount();
                }
            }
            if (advanceInvoice.getDeductions() != null && advanceInvoice.getDeductionAmount() != null && advanceInvoice.getDeductionAmount() > 0) {
                double paidDeductionAmount = 0.0;
                double totalDeductionAmount = 0.0;
                double pendingDeductionAmount = 0.0;
                List<Deductions> listDeductions = advanceInvoice.getDeductions();
                if (listDeductions != null) {

                    List<DeductionsItem> listDeductionItem = listDeductions.stream().filter(i -> i.getPaidAmount() == null || i.getPaidAmount() < i.getAmount()).map(i -> {
                        double pendingAmount = 0.0;
                        if (i.getPaidAmount() != null) {
                            pendingAmount = i.getAmount() - i.getPaidAmount();
                        }
                        return new DeductionsItem(i.getType(), i.getPaidAmount(), i.getAmount(), pendingAmount);
                    }).toList();
                    paidDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getPaidAmount).sum();
                    totalDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();
                    pendingDeductionAmount = totalDeductionAmount - paidDeductionAmount;
                    deductionAmount = totalDeductionAmount - paidDeductionAmount;

                    deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidDeductionAmount, pendingDeductionAmount, listDeductionItem);
                }
            }


        }

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    bookingAmount = bookingInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmount = bookingInvoice.getBalanceAmount();
                    totalAmountToRedeem = totalAmountToRedeem + bookingInvoice.getBalanceAmount();
                }
            }
        }


        CustomerInformations customerInformations = getCustomerInformations(customers, bookingsV1);

        StayInfo stayInfo = bookingsService.getStayInfo(customers, bookingsV1, leavingDate);
        EBInfo ebInfo = electricityService.getEbInfoForSettlement(customers, customers.getHostelId(), leavingDate);
        List<UnpaidInvoices> listUnpaidInvoices = invoiceService.getUnpaidInvoices(customers.getCustomerId(), customers.getHostelId(), leavingDate);
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoicesInfo = invoiceService.getUnpaidInvoicesInfoForJoiningBased(customers.getCustomerId(), customers.getHostelId(), customers.getJoiningDate(), leavingDate);
        RentInfo currentMonthRentInfo = getRentInfo(customers.getHostelId(), customers, leavingDate, bookingsV1.getRentAmount());
//        RentInfo currentMonthRentInfo = getRentInfoForJoiningBased(customers, bookingsV1, leavingDate, currentMonthBillingDates);
        AdvanceItems advanceItems = invoiceService.getRedeemedListFromAdvance(customers.getHostelId(), customers.getCustomerId());
        AdvanceItems bookingItems = invoiceService.getRedeemedListFromBookings(customers.getHostelId(), customers.getCustomerId());
        String label = null;
        double payableAmount = 0.0;
        if (currentMonthRentInfo.currentRentPaid() > currentMonthRentInfo.currentPayableRent()) {
            label = "Refundable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        } else {
            label = "Payable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        }
        double walletAmount = 0.0;
        if (customers.getWallet() != null) {
            CustomerWallet wallet = customers.getWallet();
            if (wallet.getAmount() != null) {
                walletAmount = wallet.getAmount();
            }
        }

        double ebAmount = 0.0;
        if (ebInfo != null) {
            ebAmount = ebInfo.pendingEbAmount();
        }
        List<com.smartstay.smartstay.dto.wallet.WalletTransactions> listWallets = customerWalletHistoryService.getInvoicePendingByCustomerId(customers.getCustomerId());

        com.smartstay.smartstay.dto.wallet.WalletInfo walletInfo = new com.smartstay.smartstay.dto.wallet.WalletInfo(Utils.roundOffWithTwoDigit(walletAmount), listWallets);

        double totalAmountToBePaid = unpaidInvoicesInfo.invoiceTotalAmount() + ebAmount + walletInfo.walletAmount() + currentMonthRentInfo.currentMonthTotalAmount();
        double paidAmount = unpaidInvoicesInfo.paidAmount() + currentMonthRentInfo.currentRentPaid();
//        double totalDeductions = 0.0;
        double payableRent = unpaidInvoicesInfo.unpaidAmount() + currentMonthRentInfo.currentMonthPayableAmount();

        totalAmountToBePaid = totalAmountToBePaid - paidAmount;
        totalAmountToBePaid = totalAmountToBePaid + deductionAmount;
        if (isAdvancePaid) {
            totalAmountToBePaid = totalAmountToBePaid - totalAmountToRedeem;
        }
        boolean isRefundable = totalAmountToBePaid < 0;

        double totalRefundableAdvance = 0.0;
        if (isAdvancePaid) {
            totalRefundableAdvance = totalAmountToRedeem;
        }


        SettlementInfo settlementInfo = new SettlementInfo((double) Math.round(totalAmountToBePaid), deductionAmount, payableRent, Utils.roundOffWithTwoDigit(0.0), Utils.roundOffWithTwoDigit(totalRefundableAdvance), Utils.roundOffWithTwoDigit(ebAmount), Utils.roundOfDouble(unpaidInvoicesInfo.unpaidAmount()), isRefundable, label, Utils.roundOffWithTwoDigit(payableAmount));
        return new FinalSettlement(customerInformations, stayInfo, ebInfo, listUnpaidInvoices, unpaidInvoicesInfo, currentMonthRentInfo, walletInfo, advanceItems, bookingItems, deductionsInfo, settlementInfo);
    }

    private CustomerInformations getCustomerInformations(Customers customers, BookingsV1 bookings) {
        boolean isAdvancePaid = false;
        double bookingAmount = 0.0;
        double totalAdvanceAmount = 0.0;
        double availableAmountToRedeem = 0.0;
        double availableAdvanceAmountToRedeem = 0.0;
        double availableBookingAmountToReddem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        InvoicesV1 bookingInvoice = invoiceService.getBookingInvoice(customers.getCustomerId(), customers.getHostelId());

        if (advanceInvoice != null) {
            advanceAmountRedeemedFromBookingInvoice = invoiceService.getAdvanceAmountFromBookingInvoice(advanceInvoice.getHostelId(), advanceInvoice.getInvoiceId());
            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    totalAdvanceAmount = totalAdvanceAmount + advanceInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount - advanceAmountRedeemedFromBookingInvoice;
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmountToRedeem = advanceInvoice.getBalanceAmount();
                    availableAmountToRedeem = availableAmountToRedeem + advanceInvoice.getBalanceAmount();
                }
            }
        }

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    isAdvancePaid = true;
                    bookingAmount = bookingInvoice.getPaidAmount();
                    totalAdvanceAmount = totalAdvanceAmount + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmountToReddem = bookingInvoice.getBalanceAmount();
                    availableAmountToRedeem = availableAmountToRedeem + bookingInvoice.getBalanceAmount();
                }
            }
        }
//        if (advancePaidAmount > 0) {
//            isAdvancePaid = true;
//            totalAdvanceAmount = totalAdvanceAmount + advancePaidAmount;
//        }
//        if (bookingAmount > 0) {
//            isAdvancePaid = true;
//            totalAdvanceAmount = totalAdvanceAmount + bookingAmount;
//        }

//        List<Deductions> listDeductions = new ArrayList<>();
//        if (customers != null) {
//            Advance advance = customers.getAdvance();
//            if (advance != null) {
//                listDeductions = advance.getDeductions();
//            }
//        }

        AvailableAmountToRedeem redeemableAmount = new AvailableAmountToRedeem(availableAdvanceAmountToRedeem, availableBookingAmountToReddem, availableAmountToRedeem);


        CustomerInformations customerInformations = new CustomerInformations(customers.getCustomerId(), customers.getFirstName(), customers.getLastName(), NameUtils.getFullName(customers.getFirstName(), customers.getLastName()), customers.getProfilePic(), NameUtils.getInitials(customers.getFirstName(), customers.getLastName()), "91", customers.getMobile(), Utils.dateToString(bookings.getJoiningDate()), customers.getAdvance().getAdvanceAmount(), bookings.getRentAmount(), isAdvancePaid, totalAdvanceAmount, bookingAmount, redeemableAmount);

        return customerInformations;
    }

    private RentInfo getRentInfoForPostpaidHostels(Customers customers, BookingsV1 bookingsV1, Date leavingDate, BillingDates currentMonthBillingDates) {
        List<RentBreakUp> listRentBreakup = bookingsService.getRentBreakup(customers, bookingsV1, leavingDate, currentMonthBillingDates);
        List<CurrentMonthOtherItems> currentMonthOtherItems = new ArrayList<>();

        Date startDate = null;
        if (Utils.compareWithTwoDates(bookingsV1.getJoiningDate(), currentMonthBillingDates.currentBillStartDate()) < 0) {
            startDate = currentMonthBillingDates.currentBillStartDate();
        } else {
            startDate = bookingsV1.getJoiningDate();
        }

        List<Amenities> amenities = amenitiesService.getAmenitiesByCustomerId(customers.getCustomerId(), startDate, leavingDate);
        final double[] otherItemAMount = {0.0};
        long totalDaysStayed = Utils.findNumberOfDays(startDate, leavingDate);
        long totalDaysInAMonth = Utils.findNumberOfDays(currentMonthBillingDates.currentBillStartDate(), currentMonthBillingDates.currentBillEndDate());
        if (amenities != null) {
            List<Amenities> proRateAmenities = amenities.stream().filter(Amenities::isProRate).toList();
            proRateAmenities.forEach(item -> {
                double perDayAmount = item.amenityAmount() / totalDaysInAMonth;
                double amenityAmount = perDayAmount * totalDaysStayed;
                otherItemAMount[0] = otherItemAMount[0] + amenityAmount;
                currentMonthOtherItems.add(new CurrentMonthOtherItems(item.amenityName(), Utils.roundOffWithTwoDigit(amenityAmount)));
            });

            List<Amenities> nonProRateAmenities = amenities.stream().filter(i -> !i.isProRate()).toList();
            nonProRateAmenities.forEach(item -> {
                otherItemAMount[0] = otherItemAMount[0] + item.amenityAmount();
                currentMonthOtherItems.add(new CurrentMonthOtherItems(item.amenityName(), Utils.roundOffWithTwoDigit(item.amenityAmount())));
            });
        }

        double currentPayableRent = 0.0;
        double currentRentPaid = 0.0;
        int stayDays = 0;
        double fullRent = 0.0;
        double priceDifference = 0.0;
        double currentMonthRent = bookingsV1.getRentAmount();
        double currentMonthPayableAmount = 0.0;
        String currentInvoiceStartDate = Utils.dateToString(currentMonthBillingDates.currentBillStartDate());
        String currentInvoiceEndDate = Utils.dateToString(currentMonthBillingDates.currentBillEndDate());

        if (listRentBreakup != null) {
            currentPayableRent = listRentBreakup.stream().mapToDouble(RentBreakUp::totalRent).sum();
            stayDays = (int) listRentBreakup.stream().mapToLong(RentBreakUp::noOfDays).sum();
            currentMonthPayableAmount = currentPayableRent + otherItemAMount[0];
            if (listRentBreakup.size() > 1) {
                RentBreakUp rbu = listRentBreakup
                        .stream()
                        .max(Comparator.comparing(RentBreakUp::rent))
                        .orElse(null);
                if (rbu != null) {
                    fullRent = rbu.rent();
                }
            }
            else if (!listRentBreakup.isEmpty()) {
                fullRent = listRentBreakup
                        .getFirst().rent();

            }
        }

//        priceDifference = fullRent - currentMonthPayableAmount - currentRentPaid;
//        if (priceDifference < 0) {
//            priceDifference = priceDifference * (-1);
//        }

//        double currentMonthRentOnly = currentMonthPayableAmount - otherItemAMount[0];
//        priceDifference = fullRent - currentMonthRentOnly;
//        if (currentRentPaid > 0) {
//            if (currentRentPaid >= (fullRent + otherItemAMount[0])) {
//
//                priceDifference = currentMonthPayableAmount * -1;
//            }
//            else if (currentRentPaid <= currentMonthPayableAmount) {
//                //do nothing
////                    priceDifference = priceDifference + otherItemAmount.get();
//            }
//            else {
//                double diff = (currentRentPaid - otherItemAMount[0]) - currentMonthRentOnly;
//                priceDifference = priceDifference + diff ;
//            }
//        }

        double currentMonthRentOnly = currentMonthPayableAmount - otherItemAMount[0];
        priceDifference = fullRent - currentMonthRentOnly;


        return new RentInfo(Utils.roundOffWithTwoDigit(currentPayableRent), currentRentPaid, stayDays, currentMonthRent, currentMonthPayableAmount, Utils.roundOffWithTwoDigit(currentMonthPayableAmount), currentInvoiceStartDate, currentInvoiceEndDate, null, Utils.roundOffWithTwoDigit(otherItemAMount[0]), false, 0.0, fullRent, Utils.roundOffWithTwoDigit(priceDifference), currentMonthOtherItems, listRentBreakup);
    }

    /**
     * this will work based on current month
     *
     * @param
     * @param leavingDate
     * @return
     */
    private RentInfo getRentInfo(String hostelId, Customers customers, Date leavingDate, double monthlyRent) {
        return invoiceService.getRentInfoForSettlement(hostelId, customers, leavingDate, monthlyRent);
    }

    public FinalSettlement getFinalSettlementInfoForBedChange(Customers customers, BookingsV1 bookingsV1, BillingDates billingDates, Date leavingDate) {
        CustomerInformations customerInformations = getCustomerInformations(customers, bookingsV1);
        StayInfo stayInfo = bookingsService.getStayInfo(customers, bookingsV1, leavingDate);
        EBInfo ebInfo = electricityService.getEbInfoForSettlement(customers, customers.getHostelId(), leavingDate);
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoicesInfo = invoiceService.getUnpaidInvoicesInfo(customers.getCustomerId(), customers.getHostelId(), leavingDate);
        RentInfo currentMonthRentInfo = invoiceService.getRentInfoForBedChange(customers.getCustomerId(), customers, leavingDate, billingDates, bookingsV1);

        List<com.smartstay.smartstay.dto.wallet.WalletTransactions> listWallets = customerWalletHistoryService.getInvoicePendingByCustomerId(customers.getCustomerId());
        AdvanceItems advanceItems = invoiceService.getRedeemedListFromAdvance(customers.getHostelId(), customers.getCustomerId());
        AdvanceItems bookingItems = invoiceService.getRedeemedListFromBookings(customers.getHostelId(), customers.getCustomerId());

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        InvoicesV1 bookingInvoice = invoiceService.getBookingInvoice(customers.getCustomerId(), customers.getHostelId());

        boolean isAdvancePaid = false;
        double advancePaidAmount = 0.0;
        double bookingAmount = 0.0;
        double totalAdvanceAmount = 0.0;
        double totalAdvancePaid = 0.0;
        double availableAdvanceAmountToReddem = 0.0;
        double availableBookingAmountToRedeem = 0.0;
        double availableTotalAmountToReddem = 0.0;

        DeductionsInfo deductionsInfo = null;
        double deductionAmount = 0.0;

        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    advancePaidAmount = advanceInvoice.getPaidAmount();
                    isAdvancePaid = true;
                    totalAdvancePaid = totalAdvancePaid + advanceInvoice.getPaidAmount();
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmountToReddem = advanceInvoice.getBalanceAmount();
                    availableTotalAmountToReddem = availableTotalAmountToReddem + advanceInvoice.getBalanceAmount();
                }
            }

            if (advanceInvoice.getDeductions() != null && advanceInvoice.getDeductionAmount() != null && advanceInvoice.getDeductionAmount() > 0) {
                double paidDeductionAmount = 0.0;
                double totalDeductionAmount = 0.0;
                double pendingDeductionAmount = 0.0;
                List<Deductions> listDeductions = advanceInvoice.getDeductions();
                if (listDeductions != null) {

                    List<DeductionsItem> listDeductionItem = listDeductions.stream().filter(i -> i.getPaidAmount() == null || i.getPaidAmount() < i.getAmount()).map(i -> {
                        double pendingAmount = 0.0;
                        if (i.getPaidAmount() != null) {
                            pendingAmount = i.getAmount() - i.getPaidAmount();
                        }
                        return new DeductionsItem(i.getType(), i.getPaidAmount(), i.getAmount(), pendingAmount);
                    }).toList();
                    paidDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getPaidAmount).sum();
                    totalDeductionAmount = listDeductions.stream().mapToDouble(Deductions::getAmount).sum();
                    pendingDeductionAmount = totalDeductionAmount - paidDeductionAmount;
                    deductionAmount = totalDeductionAmount - paidDeductionAmount;

                    deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidDeductionAmount, pendingDeductionAmount, listDeductionItem);
                }
            }
        }

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    bookingAmount = bookingInvoice.getPaidAmount();
                    isAdvancePaid = true;
                    totalAdvancePaid = totalAdvancePaid + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmountToRedeem = bookingInvoice.getBalanceAmount();
                    availableTotalAmountToReddem = availableTotalAmountToReddem + bookingInvoice.getBalanceAmount();
                }
            }
        }


        String label = null;
        double payableAmount = 0.0;
        if (currentMonthRentInfo.currentRentPaid() > currentMonthRentInfo.currentPayableRent()) {
            label = "Refundable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        } else {
            label = "Payable rent";
            payableAmount = currentMonthRentInfo.currentMonthPayableAmount();
        }
        double walletAmount = 0.0;
        if (customers.getWallet() != null) {
            CustomerWallet wallet = customers.getWallet();
            if (wallet.getAmount() != null) {
                walletAmount = wallet.getAmount();
            }
        }

        double ebAmount = 0.0;
        if (ebInfo != null) {
            ebAmount = ebInfo.pendingEbAmount();
        }

        com.smartstay.smartstay.dto.wallet.WalletInfo walletInfo = new com.smartstay.smartstay.dto.wallet.WalletInfo(Utils.roundOffWithTwoDigit(walletAmount), listWallets);

        double totalAmountToBePaid = unpaidInvoicesInfo.invoiceTotalAmount() + ebAmount + walletInfo.walletAmount() + currentMonthRentInfo.currentMonthTotalAmount();
        double paidAmount = unpaidInvoicesInfo.paidAmount() + currentMonthRentInfo.currentRentPaid();
        double totalDeductions = 0.0;
        double payableRent = unpaidInvoicesInfo.unpaidAmount() + currentMonthRentInfo.currentMonthPayableAmount();

        totalAmountToBePaid = totalAmountToBePaid - paidAmount;
//        totalAmountToBePaid = totalAmountToBePaid  - paidAmount;
        totalAmountToBePaid = totalAmountToBePaid + deductionAmount;
        if (isAdvancePaid) {
            totalAmountToBePaid = totalAmountToBePaid - availableTotalAmountToReddem;
        }
        boolean isRefundable = totalAmountToBePaid < 0;

        double totalRefundableAdvance = 0.0;
        if (isAdvancePaid) {
            totalRefundableAdvance = availableTotalAmountToReddem;
        }


        SettlementInfo settlementInfo = new SettlementInfo((double) Math.round(totalAmountToBePaid), deductionAmount, payableRent, Utils.roundOffWithTwoDigit(totalDeductions), Utils.roundOffWithTwoDigit(totalRefundableAdvance), Utils.roundOffWithTwoDigit(ebAmount), Utils.roundOfDouble(unpaidInvoicesInfo.unpaidAmount()), isRefundable, label, Utils.roundOffWithTwoDigit(payableAmount));

        return new FinalSettlement(customerInformations, stayInfo, ebInfo, null, unpaidInvoicesInfo, currentMonthRentInfo, walletInfo, advanceItems, bookingItems, deductionsInfo, settlementInfo);
    }

    public ResponseEntity<?> generateFinalSettlement(String customerId, com.smartstay.smartstay.payloads.settlement.Settlement settlement) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        BookingsV1 bookingDetails = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingDetails == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());
        CustomersBedHistory cbh = bedHistory.getLatestCustomerBed(customerId);
        SettlementDetails settlementDetails = settlementDetailsService.getSettlementInfoForCustomer(customerId);

        if (settlementDetails == null) {
            return new ResponseEntity<>(Utils.SETTLEMENT_INFORMATION_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
        }

        boolean isFullRentCollected = false;
        double customRent = 0.0;
        if (settlement != null) {
            if (settlement.shouldCollectFullRent() != null) {
                isFullRentCollected = settlement.shouldCollectFullRent();
            }
            if (settlement.customRent() != null) {
                customRent = settlement.customRent();
            }
        }

        if (!billDate.typeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
            if (billDate.billingModel().equalsIgnoreCase(BillingModel.POSTPAID.name())) {
                //done updating cbh
                return generateFinalSettlementForFixedPostpaid(customers, settlementDetails.getLeavingDate(), bookingDetails, billDate, settlement, users, isFullRentCollected, customRent);
            } else {
                if (Utils.compareWithTwoDates(cbh.getStartDate(), billDate.currentBillStartDate()) > 0) {
                    //done updating cbh
                    return generateFinalSettlementForBedChange(customers, bookingDetails, billDate, cbh, settlement, settlementDetails, users, isFullRentCollected, customRent);
                }
                //done updating cbh
                return generateFinalSettlementInvoiceForFixedPrepaid(customers, settlementDetails.getLeavingDate(), bookingDetails, billDate, settlement, users, isFullRentCollected, customRent);
            }
        } else {
            if (billDate.billingModel().equalsIgnoreCase(BillingModel.PREPAID.name())) {
                BillingDates customerBillingDates = hostelService.getJoiningBasedCurrentMonthBillingDate(customers.getJoiningDate(), customers.getHostelId(), settlementDetails.getLeavingDate());
                //done updating cbh
                return generateFinalSettlementForJoininBasedPrepaid(customers, settlementDetails.getLeavingDate(), bookingDetails, customerBillingDates, settlement, users, isFullRentCollected, customRent);
            }
        }

        boolean isCurrentRentPaid = false;
        boolean isAdvancePaid = false;

        double bookingAmount = 0.0;
        double totalDeductions = 0.0;
        long noOfDaySatayed = 1;
        double currentMonthRent = 0.0;
        double currentRentPaid = 0.0;
        double currentMonthPayableRent = 0.0;
        double advancePaidAmount = 0.0;
        double unpaidInvoiceAmount = 0.0;
        double partialPaidAmount = 0.0;
        double totalAmountToBePaid = 0.0;
        double totalAmountWithoutDeductions = 0.0;
        double walletAmount = 0.0;
        List<Deductions> listDeductions = new ArrayList<>();

        if (bookingDetails.getBookingAmount() != null) {
            bookingAmount = bookingDetails.getBookingAmount();
        }

//        if (customers.getAdvance() != null) {
//            totalDeductions = customers.getAdvance().getDeductions().stream().mapToDouble(Deductions::getAmount).sum();
//
//            listDeductions = customers.getAdvance().getDeductions();
//        }

        CustomerWallet cw = customers.getWallet();
        if (cw != null) {
            if (cw.getAmount() != null) {
                walletAmount = cw.getAmount();
            }
        }

        List<InvoicesV1> listUnpaidInvoices = invoiceService.listAllUnpaidInvoices(customerId, customers.getHostelId());

        List<InvoicesV1> listUnpaidRentalInvoices = listUnpaidInvoices.stream().filter(item -> (item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || item.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0).toList();

        List<InvoicesV1> currentMonthInvoice = listUnpaidInvoices.stream().filter(item -> (item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || item.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) >= 0).toList();

//        Calendar calStartDate = Calendar.getInstance();
//        calStartDate.setTime();

//        Calendar calEndDate = Calendar.getInstance();
//        calEndDate.setTime(billDate.currentBillEndDate());

        Date dateStartDate = null;

        long noOfd = Utils.compareWithTwoDates(billDate.currentBillStartDate(), bookingDetails.getJoiningDate());
        if (Utils.compareWithTwoDates(billDate.currentBillStartDate(), bookingDetails.getJoiningDate()) <= 0) {
            dateStartDate = bookingDetails.getJoiningDate();
        } else {
            dateStartDate = billDate.currentBillStartDate();
        }

        long findNoOfDaysInCurrentMonth = Utils.findNumberOfDays(billDate.currentBillStartDate(), billDate.currentBillEndDate());

        noOfDaySatayed = Utils.findNumberOfDays(dateStartDate, settlementDetails.getLeavingDate());

        double currentMonthOtherPayableAmount = 0.0;
        double currentMonthTotalPayableAmount = 0.0;
        //taken from unpaid invoices. So current month invoice is empty for paid
        if (!currentMonthInvoice.isEmpty()) {
            InvoicesV1 currentInvoice = currentMonthInvoice.get(0);
            currentMonthRent = currentInvoice.getTotalAmount();

            List<String> currentMonthInfo = new ArrayList<>();
            currentMonthInfo.add(currentInvoice.getInvoiceId());

            currentRentPaid = transactionService.getTransactionInfo(currentMonthInfo).stream().mapToDouble(PartialPaidInvoiceInfo::paidAmount).sum();
            if (currentRentPaid > 0) {
                isCurrentRentPaid = true;
            }
            currentMonthOtherPayableAmount = currentInvoice.getInvoiceItems().stream().filter(i -> !i.getInvoiceItem().equalsIgnoreCase(InvoiceItems.RENT.name())).mapToDouble(i -> {
                if (i.getAmount() == null) {
                    return 0.0;
                }
                return i.getAmount();
            }).sum();
        } else {
            //current month invoice is paid rent
            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthRentInvoice(customerId);
            if (invoicesV1 != null) {
                currentMonthRent = invoicesV1.getTotalAmount();
                if (invoicesV1.getPaidAmount() == null) {
                    currentRentPaid = 0.0;
                } else {
                    currentRentPaid = invoicesV1.getPaidAmount();
                }

                isCurrentRentPaid = true;

                currentMonthOtherPayableAmount = invoicesV1.getInvoiceItems().stream().filter(i -> !i.getInvoiceItem().equalsIgnoreCase(InvoiceItems.RENT.name())).mapToDouble(i -> {
                    if (i.getAmount() == null) {
                        return 0.0;
                    }
                    return i.getAmount();
                }).sum();
            }

        }

        double rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) >= 0) {
            rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
        }
        currentMonthPayableRent = Math.round(noOfDaySatayed * rentPerDay);
        currentMonthTotalPayableAmount = currentMonthPayableRent + currentMonthOtherPayableAmount;

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices.stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())).toList();
        InvoicesV1 invAdvanceInvoice = null;


        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            invAdvanceInvoice = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }

            InvoicesV1 advanceInvoiceTemp = advInv;
            if (advanceInvoiceTemp.getDeductions() != null ) {
                if (!advanceInvoiceTemp.getDeductions().isEmpty()) {
                    if (advanceInvoiceTemp.getPaidAmount() != null) {
                        if (advanceInvoiceTemp.getPaidAmount() < advanceInvoiceTemp.getDeductionAmount()) {
                            listDeductions = advanceInvoiceTemp
                                    .getDeductions()
                                    .stream()
                                    .filter(i -> {
                                        if (i.getPaidAmount() == null) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() == 0) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() < i.getAmount()) {
                                            return true;
                                        }
                                        return false;
                                    })
                                    .toList();
                        }
                    }
                }
            }

        } else {
            invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
//            advanceInvoice = new ArrayList<>();
//            advanceInvoice.add(invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId()));
            if (invAdvanceInvoice != null) {
                Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
                if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                    isAdvancePaid = true;
                    advancePaidAmount = paidAmount;
                } else {
                    isAdvancePaid = false;
                    advancePaidAmount = paidAmount;
                }
                InvoicesV1 advanceInvoiceTemp = invAdvanceInvoice;
                if (advanceInvoiceTemp.getDeductions() != null ) {
                    if (!advanceInvoiceTemp.getDeductions().isEmpty()) {
                        if (advanceInvoiceTemp.getPaidAmount() != null) {
                            if (advanceInvoiceTemp.getPaidAmount() < advanceInvoiceTemp.getDeductionAmount()) {
                                listDeductions = advanceInvoiceTemp
                                        .getDeductions()
                                        .stream()
                                        .filter(i -> {
                                            if (i.getPaidAmount() == null) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() == 0) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() < i.getAmount()) {
                                                return true;
                                            }
                                            return false;
                                        })
                                        .toList();
                            }
                        }
                    }
                }
            }
        }

        advancePaidAmount = advancePaidAmount + bookingAmount;


        List<String> partialPaymentInvoices = listUnpaidRentalInvoices.stream().filter(invoicesV1 -> invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())).map(InvoicesV1::getInvoiceId).toList();

//        List<PartialPaidInvoiceInfo> lisPartialPayments = transactionService.getTransactionInfo(partialPaymentInvoices);

        partialPaidAmount = listUnpaidRentalInvoices.stream().filter(i -> !i.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())).mapToDouble(InvoicesV1::getPaidAmount).sum();
        unpaidInvoiceAmount = listUnpaidInvoices.stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0).mapToDouble(InvoicesV1::getTotalAmount).sum();

        double invoiceBalance = unpaidInvoiceAmount - partialPaidAmount;

        totalAmountToBePaid = invoiceBalance - advancePaidAmount;
        totalAmountToBePaid = totalAmountToBePaid + walletAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthTotalPayableAmount - currentRentPaid);
        } else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthTotalPayableAmount;
        }

        totalAmountWithoutDeductions = totalAmountToBePaid;
        totalAmountToBePaid = totalAmountToBePaid + totalDeductions;
        double totalAmountForFinalSettlement = totalAmountToBePaid;
        double totalDeductionForFinalSettlement = 0.0;

        List<Settlement> deductions = new ArrayList<>();
        if (settlement.deductions() != null) {
            deductions = settlement.deductions();
        }

        if (deductions != null && !deductions.isEmpty()) {
            double finalDeductions = deductions.stream().mapToDouble(Settlement::amount).sum();
            List<Deductions> newDeductions = deductions.stream().map(i -> {
                Deductions d = new Deductions();
                d.setType(i.item());
                d.setAmount(i.amount());
                return d;
            }).toList();
            listDeductions.addAll(newDeductions);

            totalAmountToBePaid = totalAmountToBePaid + finalDeductions;
            totalDeductionForFinalSettlement = finalDeductions;
        }


        List<InvoicesV1> unpaidUpdated = listUnpaidInvoices.stream().peek(item -> item.setCancelled(true)).toList();


        totalAmountToBePaid = Math.round(totalAmountToBePaid);
        //fetch the EB Amount
        double ebAmount = electricityService.getEbAmountForSettlement(customers.getCustomerId(), customers.getHostelId(), settlementDetails.getLeavingDate());

        totalAmountToBePaid = totalAmountToBePaid + ebAmount;
        invoiceService.cancelActiveInvoice(unpaidUpdated);
//        if (invAdvanceInvoice != null) {
        InvoicesV1 invoicesV1 = invoiceService.createSettlementInvoice(customers, customers.getHostelId(), totalAmountToBePaid, unpaidUpdated, listDeductions, totalAmountWithoutDeductions, settlementDetails.getLeavingDate(), users, listDeductions);

        SettlementItems settlementItems = settlementItemService.generateSettlementItems(customers.getCustomerId(), customers.getHostelId(), invoicesV1.getInvoiceId(), null, isFullRentCollected, customRent);
        if (cw != null) {
            cw.setAmount(0.0);
            cw.setCustomers(customers);
            customers.setWallet(cw);
            customerWalletHistoryService.makePendingToInvoiceGenerated(customers.getCustomerId(), invoicesV1.getInvoiceId());
        }
        bedsService.makeABedVacant(bookingDetails.getBedId(), settlementDetails);
        bookingDetails.setCurrentStatus(BookingStatus.VACATED.name());
        if (settlementDetails != null && settlementDetails.getLeavingDate() != null) {
            bookingDetails.setLeavingDate(settlementDetails.getLeavingDate());
            bedHistory.generateSettlement(customers.getHostelId(), customers.getCustomerId(), settlementDetails.getLeavingDate());
        }
        bookingsService.saveBooking(bookingDetails);
        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());

        customersRepository.save(customers);

        userService.addUserLog(bookingDetails.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT, users);
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(customers.getHostelId());
        if (electricityConfig != null) {
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
                eventPublisher.publishEvent(new AddRoomSettlementEbEvents(this, customers.getHostelId(), customerId, settlementDetails.getLeavingDate(), authentication.getName(), settlementItems.getInvoiceId()));
            }
        }
//        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    private ResponseEntity<?> generateFinalSettlementInvoiceForFixedPrepaid(Customers customers, Date leavingDate, BookingsV1 bookingDetails, BillingDates billDate, com.smartstay.smartstay.payloads.settlement.Settlement settlement, Users users, boolean isFullRentCollected, Double customRent) {
        FinalSettlement settlementInfo = getFinalSettlementForPrepaidFixed(customers, bookingDetails, billDate, leavingDate);
        double totalAmountToBePaid = settlementInfo.settlementInfo().amountTobePaid();
        double advanceDeductionAmount = 0.0;
        double settlementDeductionAmount = 0.0;
        double discountAmount = 0.0;
        double discountAmountFromInvoice = 0.0;
        double deductionAmount = 0.0;
        double finalDiscountAmount = 0.0;
        boolean isAdvancePaid = false;
        List<Settlement> deductions = settlement.deductions();
        List<String> listUnpaidInvoices = new ArrayList<>();

        Advance advance = customers.getAdvance();
        if (advance != null) {
            List<Deductions> advanceDeductionList = advance.getDeductions();
            if (advanceDeductionList != null) {
                advanceDeductionAmount = advance.getDeductions().stream().mapToDouble(Deductions::getAmount).sum();
            }
        }

        List<Deductions> checkInDeductions = null;
        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus() == null) {
                isAdvancePaid = false;
            } else if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
            }
            if (advanceInvoice.getDeductions() != null ) {
                if (!advanceInvoice.getDeductions().isEmpty()) {
                    if (advanceInvoice.getPaidAmount() != null) {
                        if (advanceInvoice.getPaidAmount() < advanceInvoice.getDeductionAmount()) {
                            checkInDeductions = advanceInvoice
                                    .getDeductions()
                                    .stream()
                                    .filter(i -> {
                                        if (i.getPaidAmount() == null) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() == 0) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() < i.getAmount()) {
                                            return true;
                                        }
                                        return false;
                                    })
                                    .map(i -> {
                                        if (i.getPaidAmount() != null) {
                                            i.setAmount(i.getAmount() - i.getPaidAmount());
                                        }
                                        i.setPaidAmount(0.0);
                                        return i;
                                    })
                                    .toList();
                        }
                    }
                }
            }
        }

        if (settlement != null) {
            List<Settlement> listSettlementDeducitons = settlement.deductions();
            if (listSettlementDeducitons != null) {
                settlementDeductionAmount = listSettlementDeducitons.stream().mapToDouble(Settlement::amount).sum();
            }

            if (settlement.discountAmount() != null) {
                discountAmount = settlement.discountAmount();
            }

            com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoices = settlementInfo.unpaidInvoiceInfo();
            if (unpaidInvoices != null) {
                List<UnpaidInvoices> unpaidInvoicesList = unpaidInvoices.listUnpaidInvoices();
                if (unpaidInvoicesList != null) {
                    listUnpaidInvoices.addAll(unpaidInvoicesList.stream().map(UnpaidInvoices::invoiceId).toList());
                }

                List<String> listCurrentMonthInvoicesIds = invoiceService.findUnpaidCurrentMonthInvoicesIds(customers.getHostelId(), customers.getCustomerId(), billDate);
                listUnpaidInvoices.addAll(listCurrentMonthInvoicesIds);
            }
        }

        RentInfo currentMonthRentInfo = settlementInfo.currentMonthRentInfo();
        if (currentMonthRentInfo != null) {
            if (currentMonthRentInfo.isDiscountApplied()) {
                discountAmountFromInvoice = currentMonthRentInfo.discountAmount();
            }

            if (currentMonthRentInfo.currentRentPaid() == 0) {
                listUnpaidInvoices.add(currentMonthRentInfo.currentInvoiceId());
            }
        }


        if (discountAmountFromInvoice != discountAmount) {
            finalDiscountAmount = discountAmount;
            invoiceService.modifyCurrentMonthDiscount(customers.getCustomerId(), customers.getHostelId(), discountAmount, billDate);
        } else {
            finalDiscountAmount = discountAmountFromInvoice;
        }

        totalAmountToBePaid = totalAmountToBePaid - finalDiscountAmount;

        List<Deductions> lisDeductions = new ArrayList<>();
        if (deductions != null && !deductions.isEmpty()) {
            lisDeductions.addAll(deductions.stream().map(i -> new Deductions(i.item(), i.amount(), 0.0)).toList());
        }

//        if (checkInDeductions != null) {
//            if (!checkInDeductions.isEmpty()) {
//                lisDeductions.addAll(checkInDeductions);
//            }
//        }
        //no need to add advance deduction amount. Already added in advance deductions.
        totalAmountToBePaid = totalAmountToBePaid + settlementDeductionAmount;
        deductionAmount = advanceDeductionAmount + settlementDeductionAmount;

        double differenceAmount = 0.0;

        if (isFullRentCollected) {
            if (customRent != null) {
                if (customRent == 0) {
                    RentInfo rentInfo = settlementInfo.currentMonthRentInfo();
                    if (rentInfo != null) {
                        if (rentInfo.fullRent() != null) {
                            customRent = rentInfo.fullRent();
                        }
                        differenceAmount = rentInfo.rentDifference();
                    }
                }
                else {
                    RentInfo rentInfo = settlementInfo.currentMonthRentInfo();
                    if (rentInfo != null) {
                        if (!customRent.equals(rentInfo.currentMonthRent())) {
                            differenceAmount = customRent - rentInfo.fullRent() + rentInfo.rentDifference();
//                            if (differenceAmount < 0) {
//                                differenceAmount = differenceAmount * -1;
//                            }
                        }
                        else {
                            differenceAmount =  rentInfo.rentDifference();
                        }

                    }
                }
            }
            else {
                RentInfo rentInfo = settlementInfo.currentMonthRentInfo();
                if (rentInfo != null) {
                    differenceAmount = rentInfo.rentDifference();
                }
            }

            totalAmountToBePaid = totalAmountToBePaid + differenceAmount;
        }

        double amountToBePaidWithoutDeductions = totalAmountToBePaid - deductionAmount;
//        leavingDate, users, isAdvancePaid

        List<InvoicesV1> unpaidInvoices = invoiceService.findUnpaidInvoices(customers.getCustomerId());
        List<String> unpaidInvoiceIds = unpaidInvoices
                .stream()
                .map(InvoicesV1::getInvoiceId)
                .toList();


        InvoicesV1 settlementInvoice = invoiceService.createSettlementInvoiceForFixedPrepaid(customers, customers.getHostelId(), totalAmountToBePaid, unpaidInvoiceIds, lisDeductions, amountToBePaidWithoutDeductions, leavingDate, users, isAdvancePaid, checkInDeductions);

        CustomerWallet cw = customers.getWallet();
        if (cw != null) {
            cw.setAmount(0.0);
            cw.setCustomers(customers);
            customers.setWallet(cw);
            customerWalletHistoryService.makePendingToInvoiceGenerated(customers.getCustomerId(), settlementInvoice.getInvoiceId());
        }
        bedHistory.generateSettlement(customers.getHostelId(), customers.getCustomerId(), leavingDate);
        bedsService.makeABedVacant(bookingDetails.getBedId(), leavingDate);
        bookingDetails.setCurrentStatus(BookingStatus.VACATED.name());
        bookingDetails.setLeavingDate(leavingDate);
        bookingDetails.setSettlementGeneratedDate(leavingDate);
        bookingsService.saveBooking(bookingDetails);
        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
        customersRepository.save(customers);

        SettlementItems settlementItems = settlementItemService.generateSettlementItems(customers.getCustomerId(), customers.getHostelId(), settlementInvoice.getInvoiceId(), settlementInfo, isFullRentCollected, customRent);

        userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT, users);

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(customers.getHostelId());
        if (electricityConfig != null) {
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
                eventPublisher.publishEvent(new AddRoomSettlementEbEvents(this, customers.getHostelId(), customers.getCustomerId(), leavingDate, authentication.getName(), settlementItems.getInvoiceId()));
            }
        }
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    private ResponseEntity<?> generateFinalSettlementForJoininBasedPrepaid(Customers customers, Date leavingDate, BookingsV1 bookingDetails, BillingDates currentMonthBillingDates, com.smartstay.smartstay.payloads.settlement.Settlement stml, Users users, boolean isFullRentCollected, Double customRent) {
        FinalSettlement settlement = getSettlementInfoForJoiningBased(customers, leavingDate, bookingDetails);

        List<InvoicesV1> currentMonthUnpaidInvoices = invoiceService.findAllCurrentMonthRentalInvoice(customers.getCustomerId(), customers.getHostelId(), currentMonthBillingDates.currentBillStartDate());
        List<InvoicesV1> currentMonthUnPaid = currentMonthUnpaidInvoices.stream().filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())).toList();
        List<String> listUnpaidInvoices = new ArrayList<>();
        List<Settlement> deductions = stml.deductions();
        if (deductions == null) {
            deductions = new ArrayList<>();
        }
        List<Deductions> checkInDeductions = null;
        double fullRent = 0.0;
        double rentDifference = 0.0;
        boolean isAdvancePaid = false;
        double amountToBePaid = 0.0;
        double amoutToBePaidWithoutDeductions = 0.0;
        double discountAmountFromInvoice = 0.0;
        double discountAmountFromSettlement = 0.0;
        double finalDiscount = 0.0;
        double deductionAmount = deductions.stream().mapToDouble(i -> {
            if (i.amount() != null) {
                return i.amount();
            }
            return 0.0;
        }).sum();
        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus() == null) {
                isAdvancePaid = false;
            } else if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
            }

            if (advanceInvoice.getDeductions() != null ) {
                if (!advanceInvoice.getDeductions().isEmpty()) {
                    if (advanceInvoice.getPaidAmount() != null) {
                        if (advanceInvoice.getPaidAmount() < advanceInvoice.getDeductionAmount()) {
                            checkInDeductions = advanceInvoice
                                    .getDeductions()
                                    .stream()
                                    .filter(i -> {
                                        if (i.getPaidAmount() == null) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() == 0) {
                                            return true;
                                        }
                                        if (i.getPaidAmount() < i.getAmount()) {
                                            return true;
                                        }
                                        return false;
                                    })
                                    .map(i -> {
                                        if (i.getPaidAmount() != null) {
                                            i.setAmount(i.getAmount() - i.getPaidAmount());
                                        }
                                        i.setPaidAmount(0.0);
                                        return i;
                                    })
                                    .toList();
                        }
                    }
                }
            }
        }
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoices = settlement.unpaidInvoiceInfo();
        if (unpaidInvoices != null) {
            List<UnpaidInvoices> unpaid = unpaidInvoices.listUnpaidInvoices();
            if (unpaid != null) {
                listUnpaidInvoices.addAll(unpaid.stream().map(UnpaidInvoices::invoiceId).toList());
            }
        }

        if (currentMonthUnPaid != null) {
            if (listUnpaidInvoices == null) {
                listUnpaidInvoices = new ArrayList<>();
            }
            listUnpaidInvoices.addAll(currentMonthUnPaid.stream().map(InvoicesV1::getInvoiceId).toList());
        }

        if (stml != null) {
            if (stml.discountAmount() != null) {
                discountAmountFromSettlement = stml.discountAmount();
            }
        }

        SettlementInfo info = settlement.settlementInfo();
        if (info != null) {
            amountToBePaid = info.amountTobePaid();
            amoutToBePaidWithoutDeductions = info.amountTobePaid();
            amountToBePaid = amountToBePaid + deductionAmount;

            RentInfo currentMonthRentInfo = settlement.currentMonthRentInfo();
            if (currentMonthRentInfo != null) {
                if (currentMonthRentInfo.isDiscountApplied()) {
                    discountAmountFromInvoice = currentMonthRentInfo.discountAmount();
                }
                if (isFullRentCollected) {
                    if (customRent != null) {
                        if (customRent == 0) {
                            fullRent = currentMonthRentInfo.fullRent();
                            rentDifference = currentMonthRentInfo.rentDifference();
                        }
                        else {
                            fullRent = customRent;
                            rentDifference = customRent - currentMonthRentInfo.fullRent() + currentMonthRentInfo.rentDifference();
//                            if (rentDifference < 0) {
//                                rentDifference = rentDifference * -1;
//                            }
                        }
                    }
                    else {
                        fullRent = customRent;
                        rentDifference = customRent - currentMonthRentInfo.currentPayableRent();
                        if (rentDifference < 0) {
                            rentDifference = rentDifference * -1;
                        }
                    }

                    amountToBePaid = amountToBePaid + rentDifference;
                }
            }
        }

        if (discountAmountFromInvoice != discountAmountFromSettlement) {
            finalDiscount = discountAmountFromSettlement;
            invoiceService.modifyCurrentMonthDiscount(customers.getCustomerId(), customers.getHostelId(), discountAmountFromSettlement, currentMonthBillingDates);
        } else {
            finalDiscount = discountAmountFromInvoice;
        }

        List<Deductions> lisDeductions = new ArrayList<>();

//        List<Deductions> lisDeductions = customers.getAdvance().getDeductions();
        if (deductions != null && !deductions.isEmpty()) {
            lisDeductions.addAll(deductions.stream().map(i -> new Deductions(i.item(), i.amount(), 0.0)).toList());
        }

        amountToBePaid = amountToBePaid - finalDiscount;

        InvoicesV1 invoicesV1 = invoiceService.createSettlementInvoiceForPostpaid(customers, customers.getHostelId(), Math.round(amountToBePaid), listUnpaidInvoices, lisDeductions, amoutToBePaidWithoutDeductions, leavingDate, users, isAdvancePaid, checkInDeductions);

        SettlementItems settlementItems = settlementItemService.generateSettlementItems(customers.getCustomerId(), customers.getHostelId(), invoicesV1.getInvoiceId(), settlement, isFullRentCollected, customRent);
        CustomerWallet cw = customers.getWallet();
        if (cw != null) {
            cw.setAmount(0.0);
            cw.setCustomers(customers);
            customers.setWallet(cw);
            customerWalletHistoryService.makePendingToInvoiceGenerated(customers.getCustomerId(), invoicesV1.getInvoiceId());
        }
        bedsService.makeABedVacant(bookingDetails.getBedId(), leavingDate);
        bedHistory.generateSettlement(customers.getHostelId(), customers.getCustomerId(), leavingDate);
        bookingDetails.setCurrentStatus(BookingStatus.VACATED.name());
        bookingDetails.setLeavingDate(leavingDate);
        bookingDetails.setSettlementGeneratedDate(leavingDate);
        bookingsService.saveBooking(bookingDetails);
        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
        customersRepository.save(customers);

        userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT, users);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    private ResponseEntity<?> generateFinalSettlementForFixedPostpaid(Customers customers, Date leavingDate, BookingsV1 bookingDetails, BillingDates currentMonthBillingDates, com.smartstay.smartstay.payloads.settlement.Settlement stlm, Users users, boolean isFullRentCollected, double customRent) {
        FinalSettlement settlement = getFinalSettlementInfo(customers, leavingDate, bookingDetails, currentMonthBillingDates);
        List<String> listUnpaidInvoices = new ArrayList<>();

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        List<Settlement> deductions = stlm.deductions();
        if (deductions == null) {
            deductions = new ArrayList<>();
        }
        double fullRent = 0.0;
        if (customRent != 0) {
            fullRent = customRent;
        }
        boolean isAdvancePaid = false;
        double amountToBePaid = 0.0;
        double amoutToBePaidWithoutDeductions = 0.0;
        double deductionAmount = deductions.stream().mapToDouble(i -> {
            if (i.amount() != null) {
                return i.amount();
            }
            return 0.0;
        }).sum();
        List<Deductions> checkInDeductions = null;
        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus() == null) {
                isAdvancePaid = false;
            } else if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
            }

            if (advanceInvoice.getPaymentStatus() != null && !advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                if (advanceInvoice.getDeductions() != null ) {
                    if (!advanceInvoice.getDeductions().isEmpty()) {
                        if (advanceInvoice.getPaidAmount() != null) {
                            if (advanceInvoice.getPaidAmount() < advanceInvoice.getDeductionAmount()) {
                                checkInDeductions = advanceInvoice
                                        .getDeductions()
                                        .stream()
                                        .filter(i -> {
                                            if (i.getPaidAmount() == null) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() == 0) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() < i.getAmount()) {
                                                return true;
                                            }
                                            return false;
                                        })
                                        .map(i -> {
                                            if (i.getPaidAmount() != null) {
                                                i.setAmount(i.getAmount() - i.getPaidAmount());
                                            }
                                            i.setPaidAmount(0.0);
                                            return i;
                                        })
                                        .toList();
                            }
                        }
                    }
                }
            }

        }
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoices = settlement.unpaidInvoiceInfo();
        if (unpaidInvoices != null) {
            List<UnpaidInvoices> unpaid = unpaidInvoices.listUnpaidInvoices();
            if (unpaid != null) {
                listUnpaidInvoices = unpaid.stream().map(UnpaidInvoices::invoiceId).toList();
            }
        }

        SettlementInfo info = settlement.settlementInfo();
        if (info != null) {
            amountToBePaid = info.amountTobePaid();
            amoutToBePaidWithoutDeductions = info.amountTobePaid();
            amountToBePaid = amountToBePaid + deductionAmount;
            if (stlm.discountAmount() != null) {
                amountToBePaid = amountToBePaid - stlm.discountAmount();
            }
        }

        if (isFullRentCollected) {
            double difference = 0.0;
                if (customRent == 0) {
                    RentInfo rentInfo = settlement.currentMonthRentInfo();
                    if (rentInfo != null) {
                        if (rentInfo.fullRent() != null) {
                            customRent = rentInfo.fullRent();
                        }
                        difference = rentInfo.rentDifference();
                    }
                }
                else {
                    RentInfo rentInfo = settlement.currentMonthRentInfo();
                    if (rentInfo != null) {
                        if (!rentInfo.currentMonthRent().equals(fullRent)) {
                            difference = customRent - rentInfo.fullRent() + rentInfo.rentDifference();
//                            if (difference < 0) {
//                                difference = difference * -1;
//                            }
                        }
                        else {
                            difference =  rentInfo.rentDifference();
                        }

                    }
                }
//            }
//            else {
//                RentInfo rentInfo = settlementInfo.currentMonthRentInfo();
//                if (rentInfo != null) {
//                    differenceAmount = rentInfo.rentDifference();
//                }
//            }

            amountToBePaid = amountToBePaid + difference;
        }

//        double amountToBePaidWithoutDeductions = totalAmountToBePaid - deductionAmount;

        List<Deductions> lisDeductions = new ArrayList<>();
        if (deductions != null && !deductions.isEmpty()) {
            lisDeductions.addAll(deductions.stream().map(i -> new Deductions(i.item(), i.amount(), 0.0)).toList());
        }


        InvoicesV1 invoicesV1 = invoiceService.createSettlementInvoiceForPostpaid(customers, customers.getHostelId(), Math.round(amountToBePaid), listUnpaidInvoices, lisDeductions, amoutToBePaidWithoutDeductions, leavingDate, users, isAdvancePaid, checkInDeductions);

        SettlementItems settlementItems = settlementItemService.generateSettlementItems(customers.getCustomerId(), customers.getHostelId(), invoicesV1.getInvoiceId(), settlement, isFullRentCollected, customRent);
        CustomerWallet cw = customers.getWallet();
        if (cw != null) {
            cw.setAmount(0.0);
            cw.setCustomers(customers);
            customers.setWallet(cw);
            customerWalletHistoryService.makePendingToInvoiceGenerated(customers.getCustomerId(), invoicesV1.getInvoiceId());
        }
        bedsService.makeABedVacant(bookingDetails.getBedId(), leavingDate);
        bedHistory.generateSettlement(customers.getHostelId(), customers.getCustomerId(), leavingDate);
        bookingDetails.setCurrentStatus(BookingStatus.VACATED.name());
        bookingDetails.setLeavingDate(leavingDate);
        bookingDetails.setSettlementGeneratedDate(leavingDate);
        bookingsService.saveBooking(bookingDetails);
        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
        customersRepository.save(customers);

        userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT, users);

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(customers.getHostelId());
        if (electricityConfig != null) {
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
                eventPublisher.publishEvent(new AddRoomSettlementEbEvents(this, customers.getHostelId(), customers.getCustomerId(), leavingDate, authentication.getName(), settlementItems.getInvoiceId()));
            }
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public ResponseEntity<?> generateFinalSettlementForBedChange(Customers customers, BookingsV1 bookingsV1, BillingDates billingDates, CustomersBedHistory latestBed, com.smartstay.smartstay.payloads.settlement.Settlement stml, SettlementDetails settlementDetails, Users users, boolean isFullRentCollected, Double customRent) {
        FinalSettlement settlement = getFinalSettlementInfoForBedChange(customers, bookingsV1, billingDates, settlementDetails.getLeavingDate());

        List<String> listUnPaidIds = new ArrayList<>();
        List<InvoicesV1> listUnpaidInvoices = new ArrayList<>();

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        List<Settlement> deductions = stml.deductions();
        if (deductions == null) {
            deductions = new ArrayList<>();
        }
        List<Deductions> checkInDeductions = null;
        boolean isAdvancePaid = false;
        double amountToBePaid = 0.0;
        double differenceAmount = 0.0;
        double amoutToBePaidWithoutDeductions = 0.0;
        double deductionAmount = deductions.stream().mapToDouble(i -> {
            if (i.amount() != null) {
                return i.amount();
            }
            return 0.0;
        }).sum();
        if (advanceInvoice != null) {
            if (advanceInvoice.getPaymentStatus() == null) {
                isAdvancePaid = false;
            } else if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
            }

            if (advanceInvoice.getPaymentStatus() != null && !advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                if (advanceInvoice.getDeductions() != null ) {
                    if (!advanceInvoice.getDeductions().isEmpty()) {
                        if (advanceInvoice.getPaidAmount() != null) {
                            if (advanceInvoice.getPaidAmount() < advanceInvoice.getDeductionAmount()) {
                                checkInDeductions = advanceInvoice
                                        .getDeductions()
                                        .stream()
                                        .filter(i -> {
                                            if (i.getPaidAmount() == null) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() == 0) {
                                                return true;
                                            }
                                            if (i.getPaidAmount() < i.getAmount()) {
                                                return true;
                                            }
                                            return false;
                                        })
                                        .map(i -> {
                                            if (i.getPaidAmount() != null) {
                                                i.setAmount(i.getAmount() - i.getPaidAmount());
                                            }
                                            i.setPaidAmount(0.0);
                                            return i;
                                        })
                                        .toList();
                            }
                        }
                    }
                }
            }

        }
        com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoices = settlement.unpaidInvoiceInfo();
        if (unpaidInvoices != null) {
            List<UnpaidInvoices> unpaid = unpaidInvoices.listUnpaidInvoices();
            if (unpaid != null) {
                listUnPaidIds = unpaid.stream().map(UnpaidInvoices::invoiceId).toList();
                listUnpaidInvoices = invoiceService.findInvoices(listUnPaidIds);

                List<InvoicesV1> cancellInvoices = listUnpaidInvoices.stream().peek(item -> {
                    item.setCancelled(true);
                    item.setCancelledDate(settlementDetails.getLeavingDate());
                }).toList();

                if (cancellInvoices != null && !cancellInvoices.isEmpty()) {
                    invoiceService.cancelActiveInvoice(cancellInvoices);
                }
            }
        }


        SettlementInfo info = settlement.settlementInfo();
        if (info != null) {
            amountToBePaid = info.amountTobePaid();
            amoutToBePaidWithoutDeductions = info.amountTobePaid();
            amountToBePaid = amountToBePaid + deductionAmount;
            if (stml.discountAmount() != null) {
                amountToBePaid = amountToBePaid - stml.discountAmount();
            }

        }

        if (isFullRentCollected) {
            double difference = 0.0;
            if (customRent == 0) {
                RentInfo rentInfo = settlement.currentMonthRentInfo();
                if (rentInfo != null) {
                    if (rentInfo.fullRent() != null) {
                        customRent = rentInfo.fullRent();
                    }
                    customRent = rentInfo.fullRent();
                    difference = rentInfo.rentDifference();
                }
            }
            else {
                RentInfo rentInfo = settlement.currentMonthRentInfo();
                if (rentInfo != null) {
                    difference = customRent - rentInfo.currentMonthTotalAmount();
//                    if (difference < 0) {
//                        difference = difference * -1;
//                    }
                }
            }

            amountToBePaid = amountToBePaid + difference;
        }




        List<Deductions> lisDeductions = new ArrayList<>();
        if (deductions != null && !deductions.isEmpty()) {
            lisDeductions.addAll(deductions.stream().map(i -> new Deductions(i.item(), i.amount(), 0.0)).toList());
        }

        List<InvoicesV1> unpaidInvoiceIds = invoiceService.findUnpaidInvoices(customers.getCustomerId());

        InvoicesV1 invoicesV1 = invoiceService.createSettlementInvoice(customers, customers.getHostelId(), Math.round(amountToBePaid), unpaidInvoiceIds, lisDeductions, amoutToBePaidWithoutDeductions, settlementDetails.getLeavingDate(), users, checkInDeductions);
//        InvoicesV1 invoicesV1 = invoiceService.createSettlementInvoice(customers, customers.getHostelId(), Math.round(amountToBePaid), unpaidUpdated, listDeductions, totalAmountWithoutDeductions, settlementDetails.getLeavingDate(), users);
        SettlementItems settlementItems = settlementItemService.generateSettlementItems(customers.getCustomerId(), customers.getHostelId(), invoicesV1.getInvoiceId(), settlement, isFullRentCollected, customRent);
        CustomerWallet cw = customers.getWallet();
        if (cw != null) {
            cw.setAmount(0.0);
            cw.setCustomers(customers);
            customers.setWallet(cw);
            customerWalletHistoryService.makePendingToInvoiceGenerated(customers.getCustomerId(), invoicesV1.getInvoiceId());
        }
        bedsService.makeABedVacant(bookingsV1.getBedId(), settlementDetails);

        bookingsV1.setCurrentStatus(BookingStatus.VACATED.name());
        if (settlementDetails != null && settlementDetails.getLeavingDate() != null) {
            bookingsV1.setCheckoutDate(settlementDetails.getLeavingDate());
            bookingsV1.setSettlementGeneratedDate(settlementDetails.getLeavingDate());
            bedHistory.generateSettlement(customers.getHostelId(), customers.getCustomerId(), settlementDetails.getLeavingDate());
        }
        bookingsService.saveBooking(bookingsV1);
        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
        customersRepository.save(customers);

        userService.addUserLog(customers.getHostelId(), customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT, users);

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(customers.getHostelId());
        if (electricityConfig != null) {
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
                eventPublisher.publishEvent(new AddRoomSettlementEbEvents(this, customers.getHostelId(), customers.getCustomerId(), settlementDetails.getLeavingDate(), authentication.getName(), settlementItems.getInvoiceId()));
            }
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public boolean customerExist(String hostelId) {
        return customersRepository.existsByHostelIdAndCurrentStatusIn(hostelId, List.of(CustomerStatus.NOTICE.name(), CustomerStatus.CHECK_IN.name(), CustomerStatus.BOOKED.name()));
    }

    public List<CustomerBedsList> getCustomersFromBedHistory(String hostelId, Date billStartDate, Date billEndDate) {
        return bedHistory.getAllCustomerFromBedsHistory(hostelId, billStartDate, billEndDate);
    }

    public ResponseEntity<?> changeBed(String hostelId, String customerId, ChangeBed request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!bedsService.checkIsBedExsits(request.bedId(), user.getParentId(), hostelId)) {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingsV1 = bookingsService.findBookingsByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingsV1.getBedId() == request.bedId()) {
            return new ResponseEntity<>(Utils.CHANGE_BED_SAME_BED_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (!bedsService.isBedAvailableForReassign(request.bedId(), request.joiningDate(), customerId)) {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }

        //check is it after or equal current billing cycle
        Date joiningDate = Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (billingDates != null && billingDates.currentBillStartDate() != null) {
            if (Utils.compareWithTwoDates(joiningDate, billingDates.currentBillStartDate()) < 0) {
                return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_BILLING_START_DATE + Utils.dateToString(billingDates.currentBillStartDate()), HttpStatus.BAD_REQUEST);
            }
        }

        ReassignRent reassignRent = invoiceService.calculateAndCreateInvoiceForReassign(customers, request.joiningDate(), request.rentAmount());
        double balanceAmount = 0.0;
        if (reassignRent != null) {
            balanceAmount = reassignRent.balanceAmount() * -1;
        }
        bedsService.unassignBed(customerId, bookingsV1.getBedId());
        bedsService.reassignBed(customerId, request.bedId());

        BedRoomFloor bedRoomFloor = bedsService.findRoomAndFloorByBedIdAndHostelId(request.bedId(), hostelId);

        bookingsService.reassignBed(bedRoomFloor, bookingsV1, request);

        if (balanceAmount < 0) {
            CustomerWallet wallet = customers.getWallet();
            if (wallet != null && wallet.getAmount() != null) {
                wallet.setAmount(wallet.getAmount() + balanceAmount);
                wallet.setTransactionDate(joiningDate);
            } else {
                if (wallet == null) {
                    wallet = new CustomerWallet();
                }
                wallet.setAmount(balanceAmount);
                wallet.setTransactionDate(joiningDate);
            }
            wallet.setCustomers(customers);

            customers.setWallet(wallet);

            customerWalletHistoryService.addReassignRentIntoWalletHistory(balanceAmount, reassignRent.newInvoiceId(), customers.getCustomerId(), reassignRent.invoiceDate());
        }


        customersRepository.save(customers);
        userService.addUserLog(hostelId, customers.getCustomerId(), ActivitySource.CUSTOMERS, ActivitySourceType.CHANGED_BED, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> cancelCheckOut(String hostelId, String customerId, CancelCheckout request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingsV1 = bookingsService.findBookingsByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
//        if (bookingsV1.getBedId() == request.bedId()) {
//            return new ResponseEntity<>(Utils.CHANGE_BED_SAME_BED_ERROR, HttpStatus.BAD_REQUEST);
//        }

        Date reCheckInDate = Utils.stringToDate(request.reCheckInDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (bookingsService.isBedBookedNextDay(bookingsV1.getBedId(), customerId, reCheckInDate)) {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(reCheckInDate, bookingsV1.getJoiningDate()) < 0) {
            return new ResponseEntity<>(Utils.RECHECK_DATE_SHOULD_BE_GREATER_THAN_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        CustomersBedHistory latestBed = bedHistory.getLatestCustomerBed(customerId);
        if (latestBed == null) {
            return new ResponseEntity<>("Customer bed history not found", HttpStatus.BAD_REQUEST);
        }

        BookingsV1 otherCheckIn = bookingsService.checkBedIsBookedByOthers(latestBed.getBedId(), customerId);
        if (otherCheckIn != null) {
            return new ResponseEntity<>(Utils.BED_ALREADY_OCCUPIED, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoicesV1 = invoiceService.getFinalSettlementStatus(customers.getCustomerId());
        if (invoicesV1 != null) {
            invoicesV1.setPaymentStatus(PaymentStatus.CANCELLED.name());
            invoicesV1.setUpdatedAt(new Date());
            invoicesV1.setUpdatedBy(user.getUserId());
            invoiceService.saveInvoice(invoicesV1);
        }


        Reasons reasons = new Reasons();
        reasons.setReasonText(request.reason());
        reasons.setCreatedAt(new Date());
        reasons.setCreatedBy(user.getUserId());
        reasonService.SaveReason(reasons);


        bookingsV1.setLeavingDate(null);
        bookingsV1.setCurrentStatus(BookingStatus.CHECKIN.name());
        bookingsV1.setUpdatedAt(new Date());
        bookingsService.saveBooking(bookingsV1);

        customers.setReasons(reasons);
        customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customersRepository.save(customers);

        userService.addUserLog(hostelId, bookingsV1.getBookingId(), ActivitySource.CUSTOMERS, ActivitySourceType.CANCEL, user);


        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public void markCustomerCheckedOut(Customers customers) {
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.VACATED.name());
        customersRepository.save(customers);
    }

    public ResponseEntity<?> getCheckoutCustomers(String hostelId, String name) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(authentication.getName(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        List<CheckoutCustomers> listCustomers = customersRepository.getCheckedOutCustomerData(hostelId, name).stream().map(item -> {
            String initials = Utils.getInitials(item.getFirstName(), item.getLastName());
            String fullName = Utils.fullName(item.getFirstName(), item.getLastName());
            String currentStatus = null;
            if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                currentStatus = "Vacated";
            }
            return new CheckoutCustomers(item.getFirstName(), item.getLastName(), fullName, item.getCity(), item.getState(), item.getCountry(), item.getMobile(), currentStatus, item.getEmailId(), item.getProfilePic(), item.getBedId(), item.getFloorId(), item.getRoomId(), item.getCustomerId(), initials, Utils.dateToString(item.getExpectedJoiningDate()), Utils.dateToString(item.getActualJoiningDate()), item.getCountryCode(), Utils.dateToString(item.getCreatedAt()), item.getBedName(), item.getRoomName(), item.getFloorName(), Utils.dateToString(item.getCheckoutDate()));
        }).toList();

        CheckoutList checkoutList = new CheckoutList(hostelId, listCustomers.size(), null, listCustomers);

        return new ResponseEntity<>(checkoutList, HttpStatus.OK);
    }

    public List<Customers> getCustomerDetails(List<String> customerIds) {
        List<Customers> listCustomers = new ArrayList<>();
        if (!customerIds.isEmpty()) {
            listCustomers = customersRepository.findByCustomerIdIn(customerIds);
        }
        return listCustomers;
    }

    public Double getAdvanceAmountFromAllCustomers(String hostelId) {
        List<Customers> listCustomers = customersRepository.findCheckedInCustomerByHostelId(hostelId);
        return listCustomers.stream().map(Customers::getAdvance).mapToDouble(Advance::getAdvanceAmount).sum();
    }

    public void updateCustomersJoiningDate(Customers customers, Date joinigDate) {
        customers.setJoiningDate(joinigDate);
        customersRepository.save(customers);
    }

    public void updateAdvanceAmount(Customers customers, Advance advance) {
        customers.setAdvance(advance);
        customersRepository.save(customers);
    }

    public boolean existsByHostelIdAndCustomerIdAndStatusesIn(String s, String s1, List<String> currentStatus) {
        return customersRepository.existsByHostelIdAndCustomerIdAndStatusesIn(s, s1, currentStatus);
    }

    public ResponseEntity<?> deleteCustomer(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_ACTIVE_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }

        List<Customers> listCustomersBasedOnXuid = customersRepository.findByXuid(customers.getXuid());
        if (listCustomersBasedOnXuid.size() == 1) {
            ccs.deleteCustomer(customers.getXuid());
        }
        customersRepository.delete(customers);

        userService.addUserLog(hostelId, customerId, ActivitySource.CUSTOMERS, ActivitySourceType.DELETE, users);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public List<Customers> searchCustomerByHostelName(String hostelId, String keyword) {
        List<Customers> listCustomers = customersRepository.findByHostelIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(hostelId, keyword, keyword);
        if (listCustomers == null) {
            return new ArrayList<>();
        }
        return listCustomers;
    }

    public ResponseEntity<?> initializeCancelCheckout(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }
        boolean canCancel = true;
        boolean isBedChanged = false;
        String latestBedChange = null;
        boolean canRecheckIn = false;
        boolean isSettlementPaid = false;
        BookingsV1 bookingsV1 = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        List<CustomersBedHistory> listBedHistories = bedHistory.getCheckedInReassignedHistory(customerId);
        if (listBedHistories.size() > 1) {
            isBedChanged = true;
        }
        CustomersBedHistory latestBed = bedHistory.getLatestCustomerBed(customerId);
        if (latestBed != null) {
            latestBedChange = Utils.dateToString(latestBed.getStartDate());
        }
        assert latestBed != null;
        BookingsV1 otherCheckIn = bookingsService.checkBedIsBookedByOthers(latestBed.getBedId(), customerId);
        if (otherCheckIn != null) {
            canCancel = false;
        }
        InvoicesV1 invoicesV1 = invoiceService.findSettlementInvoiceByCustomerId(customerId, hostelId);
        if (invoicesV1 == null && canCancel) {
            canRecheckIn = true;
        } else {
            if (invoicesV1 != null && invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
                isSettlementPaid = true;
            }
        }

        com.smartstay.smartstay.responses.customer.CancelCheckout cancelCheckout = new com.smartstay.smartstay.responses.customer.CancelCheckout(customerId, hostelId, canCancel, Utils.dateToString(bookingsV1.getJoiningDate()), latestBedChange, isBedChanged, Utils.dateToString(bookingsV1.getNoticeDate()), Utils.dateToString(bookingsV1.getLeavingDate()), canRecheckIn, isSettlementPaid);

        return new ResponseEntity<>(cancelCheckout, HttpStatus.OK);
    }


    public List<Customers> findCustomerByHostelId(String hostelId, List<String> status) {
        return customersRepository.findCustomerByHostelId(hostelId, status);
    }

    public void updateCustomerWallets(List<Customers> customerWallets) {
        customersRepository.saveAll(customerWallets);
    }

    public void updateCustomersFromRecurring(Customers customers) {
        customersRepository.save(customers);
    }

    public List<String> findCustomerIdsByName(String hostelId, String name) {
        List<Customers> customers = customersRepository.findByHostelIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(hostelId, name, name);
        return customers.stream().map(Customers::getCustomerId).collect(Collectors.toList());
    }

    public ResponseEntity<?> addAdditionalContacts(String hostelId, String customerId, CustomerAdditionalContacts additionalContacts) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (additionalContacts == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (additionalContacts.fullName() == null || additionalContacts.fullName().equalsIgnoreCase("")) {
            return new ResponseEntity<>(Utils.FULL_NAME_REQUIRES, HttpStatus.BAD_REQUEST);
        }
        if (additionalContacts.mobile() == null || additionalContacts.mobile().equalsIgnoreCase("")) {
            return new ResponseEntity<>(Utils.MOBILE_NO_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        return additionalContactService.addAdditionalContacts(hostelId, customerId, additionalContacts);

    }

    public ResponseEntity<?> getCustomersOnPurpose(String hostelId, GetCustomersPurpose purpose) {
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
        if (purpose == null) {
            return new ResponseEntity<>(Utils.PARAMS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (purpose.equals(GetCustomersPurpose.WALK_IN)) {
            List<String> customerStatus = new ArrayList<>();
            customerStatus.add(CustomerStatus.INACTIVE.name());
            List<Customers> listCustomers = customersRepository.findCustomerByHostelId(hostelId, customerStatus);
            if (listCustomers != null) {
                List<WalkInCustomers> listWalkInCustomers = listCustomers.stream().map(i -> new WalkInCustomers(i.getCustomerId(), NameUtils.getFullName(i.getFirstName(), i.getLastName()), i.getProfilePic(), NameUtils.getInitials(i.getFirstName(), i.getLastName()), "+91 " + i.getMobile(), i.getEmailId())).toList();

                return new ResponseEntity<>(listWalkInCustomers, HttpStatus.OK);
            }
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        } else if (purpose.equals(GetCustomersPurpose.BILL)) {
            List<String> customerStatus = new ArrayList<>();
            customerStatus.add(CustomerStatus.ACTIVE.name());
            customerStatus.add(CustomerStatus.VACATED.name());
            customerStatus.add(CustomerStatus.NOTICE.name());
            customerStatus.add(CustomerStatus.CHECK_IN.name());

            List<Customers> listCustomers = customersRepository.findCustomerByHostelId(hostelId, customerStatus);
            List<String> customerIds = listCustomers.stream().map(Customers::getCustomerId).toList();
            List<BookingsV1> cusotmerBookings = bookingsService.findByCustomerIds(customerIds);

            List<GetCustomersForBills> customerForBills = listCustomers.stream().map(i -> new CustomerMapperForBills(cusotmerBookings).apply(i)).toList();

            return new ResponseEntity<>(customerForBills, HttpStatus.OK);
        } else if (purpose.equals(GetCustomersPurpose.COMPLAINTS)) {
            List<String> customerStatus = new ArrayList<>();
            customerStatus.add(CustomerStatus.ACTIVE.name());
            customerStatus.add(CustomerStatus.NOTICE.name());
            customerStatus.add(CustomerStatus.CHECK_IN.name());

            List<Customers> listCustomers = customersRepository.findCustomerByHostelId(hostelId, customerStatus);
            List<CustomersForComplaints> listCustomersForComplaint = listCustomers.stream().map(i -> new CustomersForComplaints(i.getCustomerId(), NameUtils.getFullName(i.getFirstName(), i.getLastName()), i.getFirstName(), i.getLastName(), i.getProfilePic(), NameUtils.getInitials(i.getFirstName(), i.getLastName()))).toList();

            return new ResponseEntity<>(listCustomersForComplaint, HttpStatus.OK);

        }
        else if (purpose.equals(GetCustomersPurpose.ADVANCE_HOLDING)) {
            List<com.smartstay.smartstay.responses.retainer.CustomersList> listCustomers = getCustomersListForRetainerInvoices(hostelId);

            CustomerListResponse response = new CustomerListResponse(hostelId,
                    listCustomers);

            return new ResponseEntity<>(response, HttpStatus.OK);

        }
        else {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private List<com.smartstay.smartstay.responses.retainer.CustomersList> getCustomersListForRetainerInvoices(String hostelId) {
        List<String> customerStatus = new ArrayList<>();
        customerStatus.add(CustomerStatus.ACTIVE.name());
        customerStatus.add(CustomerStatus.NOTICE.name());
        customerStatus.add(CustomerStatus.CHECK_IN.name());

        List<Customers> listCustomers = customersRepository.findCustomerByHostelId(hostelId, customerStatus);
        if (listCustomers == null) {
            return new ArrayList<>();
        }

        List<String> customerIds = listCustomers
                .stream()
                .map(Customers::getCustomerId)
                .toList();
        List<com.smartstay.smartstay.dao.CustomerAdditionalContacts> additionalContacts = additionalContactService.getAdditionalContactsByHostelIdAndCustomerIdIn(hostelId, customerIds);
        return listCustomers
                .stream()
                .map(i -> new CustomersListMapper(additionalContacts).apply(i))
                .toList();
    }

    public double getDeductionAmount(String customerId) {
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return 0.0;
        }

        Advance advance = customers.getAdvance();
        if (advance == null) {
            return 0.0;
        }
        List<Deductions> deductions = advance.getDeductions();
        if (deductions != null && !deductions.isEmpty()) {
            double dedctionAmount = deductions.stream().mapToDouble(Deductions::getAmount).sum();
            return dedctionAmount;
        }

        return 0.0;
    }

    public List<Customers> getCustomerDetails(List<String> listCustomerIds, String name) {
        List<Customers> listCustomers = customersRepository.findByCustomerIdsAndName(listCustomerIds, name);
        if (listCustomers == null) {
            return new ArrayList<>();
        }
        return listCustomers;
    }

    public CustomerInfo getCustomerInformationForInitializeCheckIn(String customerId) {
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return null;
        }
        return new CustomerInfo(customers.getFirstName(),
                customers.getLastName(),
                NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                customerId,
                CustomerUtils.getProfilePic(customers),
                NameUtils.getInitials(customers.getFirstName(), customers.getLastName()));
    }

    public com.smartstay.smartstay.responses.invoices.CustomerDetails getCustomerInformationForRecordPayment(String customerId) {
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers != null) {
            return new com.smartstay.smartstay.responses.invoices.CustomerDetails(NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                    customers.getFirstName(),
                    customers.getLastName(),
                    NameUtils.getInitials(customers.getFirstName(), customers.getLastName()),
                    CustomerUtils.getProfilePic(customers),
                    customers.getCustomerId());
        }
        return null;
    }

    public com.smartstay.smartstay.responses.discount.CustomerInfo getCustomerInfoForDiscount(String customerId) {
        Customers customer = customersRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return null;
        }
        return new com.smartstay.smartstay.responses.discount.CustomerInfo(customerId,
                NameUtils.getFullName(customer.getFirstName(), customer.getLastName()),
                customer.getLastName(),
                customer.getFirstName(),
                NameUtils.getInitials(customer.getFirstName(), customer.getLastName()),
                CustomerUtils.getProfilePic(customer));
    }
}
