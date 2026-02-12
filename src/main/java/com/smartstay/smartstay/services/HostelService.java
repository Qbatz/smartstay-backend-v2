package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.HostelsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.electricity.EBInfo;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.events.HostelEvents;
import com.smartstay.smartstay.payloads.AddHostelPayloads;
import com.smartstay.smartstay.payloads.RemoveUserFromHostel;
import com.smartstay.smartstay.payloads.ZohoSubscriptionRequest;
import com.smartstay.smartstay.payloads.electricity.UpdateEBConfigs;
import com.smartstay.smartstay.payloads.hostel.BillRules;
import com.smartstay.smartstay.payloads.hostel.UpdateElectricityPrice;
import com.smartstay.smartstay.payloads.hostel.UpdatePg;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import com.smartstay.smartstay.responses.hostel.EBSettings;
import com.smartstay.smartstay.responses.hostel.FloorDetails;
import com.smartstay.smartstay.responses.hostel.HostelDetails;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class HostelService {

    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    private HostelV1Repository hostelV1Repository;

    @Autowired
    @Lazy
    private CustomersService customersService;

    @Autowired
    private FloorsService floorsService;

    @Autowired
    private BedsService bedsService;

    @Autowired
    private RoomsService roomsService;

    @Autowired
    private Authentication authentication;

    @Value("${ZOHO_SUBSCRIPTION_PLAN}")
    private String zohoPlan;
    @Autowired
    private RolesService rolesService;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private UsersService usersService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private NotificationService notificationService;


    public ResponseEntity<?> addHostel(MultipartFile mainImage, List<MultipartFile> additionalImages, AddHostelPayloads payloads) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();

        String emailId = payloads.emailId();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (payloads.emailId() == null || payloads.emailId().equalsIgnoreCase("")) {
            emailId = users.getEmailId();
        } else if (!Utils.verifyEmail(payloads.emailId())) {
            emailId = usersService.findUserByUserId(userId).getEmailId();
        }

        HostelV1 hostelV1 = new HostelV1();
        hostelV1.setCreatedBy(userId);
        hostelV1.setParentId(users.getParentId());
        hostelV1.setHostelType(1);
        hostelV1.setHostelName(payloads.hostelName());
        hostelV1.setCity(payloads.city());
        hostelV1.setCountry(1);
        hostelV1.setCreatedAt(Calendar.getInstance().getTime());
        hostelV1.setUpdatedAt(Calendar.getInstance().getTime());
        hostelV1.setLandmark(payloads.landmark());
        hostelV1.setMobile(payloads.mobile());
        hostelV1.setEmailId(payloads.emailId());
        hostelV1.setHouseNo(payloads.houseNo());
        hostelV1.setPincode(payloads.pincode());
        hostelV1.setStreet(payloads.street());
        hostelV1.setState(payloads.state());
        hostelV1.setActive(true);
        hostelV1.setDeleted(false);


        if (mainImage != null) {
            String mainImageUrl = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(mainImage), "Hostel-Images");
            hostelV1.setMainImage(mainImageUrl);
        }


        List<String> listImageUrls = new ArrayList<>();
        if (additionalImages != null && !additionalImages.isEmpty()) {
            listImageUrls = additionalImages.stream().map(multipartFile -> uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(multipartFile), "Hostel-Images")).collect(Collectors.toList());
        }

        if (!listImageUrls.isEmpty()) {
            List<HostelImages> listHostelImages = new ArrayList<>(listImageUrls.stream().map(item -> {
                HostelImages hostelImg = new HostelImages();
                hostelImg.setCreatedBy(userId);
                hostelImg.setImageUrl(item);
                hostelImg.setHostel(hostelV1);
                return hostelImg;
            }).toList());

            hostelV1.setAdditionalImages(listHostelImages);
        }

        HostelV1 hostelV11 = hostelV1Repository.save(hostelV1);


        int result = userHostelService.addHostelToExistingUsers(users.getParentId(), hostelV1.getHostelId());

        if (result == 100) {
            List<Users> listUsers = usersService.findAllByParentId(users.getParentId());
            if (listUsers != null) {
                userHostelService.addHostelToExistingUsers(users.getParentId(), listUsers, hostelV1.getHostelId());
            }
        }
        eventPublisher.publishEvent(new HostelEvents(this, hostelV11.getHostelId(), authentication.getName(), users.getParentId()));
        return new ResponseEntity<>("Created successfully", HttpStatus.CREATED);
    }

    public void updateHostelFromEvents(HostelV1 hostelV1) {
        hostelV1Repository.save(hostelV1);
    }
//    public String hostelIdGenerator() {
//        UUID uuid = UUID.randomUUID();
//        String uuidString = uuid.toString();
//        HostelV1 v1 = hostelV1Repository.findById(uuidString).orElse(null);
//        if (v1 == null) {
//            return uuidString;
//        }
//        return hostelIdGenerator();
//    }

    protected ZohoSubscriptionRequest formSubscription(AddHostelPayloads payloads, String emailId) {

        Calendar cal = Calendar.getInstance();

        ZohoSubscriptionRequest.Plan plan = new ZohoSubscriptionRequest.Plan();
        plan.setPlan_code(zohoPlan);

        ZohoSubscriptionRequest.Customer customer = new ZohoSubscriptionRequest.Customer();
        customer.setDisplay_name(payloads.hostelName());
        customer.setFirst_name(payloads.hostelName());
        customer.setEmail(emailId);
        customer.setMobile(payloads.mobile());

        ZohoSubscriptionRequest request = new ZohoSubscriptionRequest();
        request.setPlan(plan);
        request.setCustomer(customer);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        request.setStarts_at(sdf.format(cal.getTime()));
        request.setNotes("New Plan");
        return request;
    }

    public List<HostelV1> getAllHostelsForRecuringInvoice() {
        return hostelV1Repository.findAll();
    }


    public ResponseEntity<?> fetchAllHostels() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.OK);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

//        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        List<Hostels> listOfHostels = userHostelService.findByUserId(userId).stream().map(item -> {
            int noOfFloors = 0;
            int noOfRooms = 0;
            final int[] noOfBeds = {0};
            AtomicInteger noOfOccupiedBeds = new AtomicInteger(0);
            final long[] noOfAvailableBeds = {0};
            List<BedsStatusCount> bedsCounts = bedsService.findBedCount(item.getHostelId());
            bedsCounts.forEach(itm -> {
                noOfBeds[0] = noOfBeds[0] + Integer.parseInt(String.valueOf(itm.getCount()));
                if (itm.getStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
                    noOfAvailableBeds[0] = itm.getCount();
                }
                if (itm.getStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name()) || itm.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
                    noOfOccupiedBeds.set(noOfOccupiedBeds.get() + Integer.valueOf(String.valueOf(itm.getCount())));
                }
            });
            noOfFloors = floorsService.getFloorCounts(item.getHostelId());
            noOfRooms = roomsService.getRoomCount(item.getHostelId());
            SubscriptionDto subscriptionDto = subscriptionService.getCurrentSubscriptionDetails(item.getHostelId());
            return new HostelsMapper(noOfFloors, noOfRooms, noOfBeds[0], noOfOccupiedBeds.get(), Integer.parseInt(String.valueOf(noOfAvailableBeds[0])), subscriptionDto).apply(Objects.requireNonNull(hostelV1Repository.findByHostelIdAndIsDeletedFalse(item.getHostelId())));
        }).toList();

        return new ResponseEntity<>(listOfHostels, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteHostelFromUser(RemoveUserFromHostel removeUserPayload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user", HttpStatus.OK);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        userHostelService.deleteUserFromHostel(removeUserPayload.userId(), removeUserPayload.hostelId());
        return new ResponseEntity<>("Deleted", HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> deleteHostel(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user", HttpStatus.OK);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelV1Repository.findById(hostelId).orElse(null);
        if (hostelV1 == null) {
            return new ResponseEntity<>("No hostels found", HttpStatus.BAD_REQUEST);
        }

        boolean customerExist = customersService.customerExist(hostelId);
        if (customerExist) {
            return new ResponseEntity<>("Cannot delete hostel. Customers are associated with this hostel.", HttpStatus.BAD_REQUEST);
        }
        hostelV1.setDeleted(true);
        hostelV1.setUpdatedAt(new Date());
        hostelV1Repository.save(hostelV1);

        userHostelService.deleteAllHostels(hostelId);
        return new ResponseEntity<>("Hostel Deleted", HttpStatus.OK);
    }
    public ResponseEntity<?> getHostelDetails(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Utils.ACCESS_RESTRICTED);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, user.getParentId());
        if (hostel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hostel found");
        }

        List<Floors> floors = floorsService.getFloorByHostelID(hostelId, user.getParentId());

        List<FloorDetails> floorDetails = floors.stream().map(floor -> new FloorDetails(floor.getFloorId(), floor.getFloorName())).toList();

        SubscriptionDto subscriptionDto = subscriptionService.getCurrentSubscriptionDetails(hostelId);
        String nextBillingDate = null;
        boolean isSubscriptionActive = false;
        int remainingDays = 0;
        if (subscriptionDto != null) {
            nextBillingDate = Utils.dateToString(subscriptionDto.nextBillingDate());
            remainingDays = subscriptionDto.endsIn();
            isSubscriptionActive = subscriptionDto.isValid();
        }
        int notificationCount = notificationService.getUnreadNotificationCount(hostelId);
        HostelDetails details = new HostelDetails(hostel.getHostelId(),
                hostel.getMainImage(),
                hostel.getCity(),
                String.valueOf(hostel.getCountry()),
                hostel.getEmailId(),
                hostel.getHostelName(),
                hostel.getHouseNo(),
                hostel.getLandmark(),
                hostel.getMobile(),
                hostel.getPincode(),
                hostel.getState(),
                hostel.getStreet(),
                Utils.dateToString(hostel.getUpdatedAt()),
                isSubscriptionActive, nextBillingDate, remainingDays, floorDetails.size(), floorDetails,notificationCount);

        return ResponseEntity.ok(details);
    }

    /**
     * this is used in event listeners side also.
     *
     * @param hostelId
     * @return
     */
    public HostelV1 getHostelInfo(String hostelId) {
        return hostelV1Repository.findByHostelId(hostelId);
    }

    public BillingDates getCurrentBillStartAndEndDates(String hostelId) {
        BillingRules billingRules = hostelConfigService.getCurrentMonthTemplate(hostelId);
        int billStartDate = 1;
        int billingRuleDueDate = 5;
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_MONTH) < billStartDate) {
            calendar.add(Calendar.MONTH, -1);
        }

//        Calendar calendarDueDate = Calendar.getInstance();
//        calendarDueDate.set(Calendar.DAY_OF_MONTH, billingRuleDate);

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(calendar.getTime(), findEndDate, dueDate, billingRuleDueDate);
    }

    public BillingDates getBillStartAndEndDateBasedOnDate(String hostelId, Date date) {
        BillingRules billingRules = hostelConfigService.getCurrentMonthTemplate(hostelId);
        int billStartDate = 1;
        int billingRuleDueDate = 5;
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(calendar.getTime(), findEndDate, dueDate, billingRuleDueDate);
    }


    public ResponseEntity<?> findFreeBeds(String hostelId) {
        return bedsService.findFreeBeds(hostelId);
    }

    public ElectricityConfig getElectricityConfig(String hostelId) {
        HostelV1 hostelV1 = hostelV1Repository.findById(hostelId).orElse(null);
        if (hostelV1 == null) {
            return null;
        }
        return hostelV1.getElectricityConfig();
    }

    public ResponseEntity<?> updateEbPrice(String hostelId, UpdateElectricityPrice electricityPrice) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, users.getParentId());
        if (hostel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hostel found");
        }
        if (electricityPrice == null) {
            return new ResponseEntity<>(Utils.ELECTICITY_PRICE_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        ElectricityConfig config = hostel.getElectricityConfig();
        config.setCharge(electricityPrice.unitPrice());
        config.setUpdated(true);
        config.setUpdatedBy(authentication.getName());
        config.setLastUpdate(new Date());

        hostel.setElectricityConfig(config);

        hostelV1Repository.save(hostel);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getEBSettings(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, users.getParentId());
        if (hostel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hostel found");
        }

        ElectricityConfig config = hostel.getElectricityConfig();
        if (config != null) {
            EBSettings settings = new EBSettings(hostelId,
                    config.getCharge(),
                    config.getTypeOfReading().equalsIgnoreCase(EBReadingType.HOSTEL_READING.name()),
                    config.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name()),
                    config.isProRate());

            return new ResponseEntity<>(settings, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Configuration found", HttpStatus.FAILED_DEPENDENCY);
    }

    public ResponseEntity<?> updateEbConfig(String hostelId, UpdateEBConfigs payloads) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, users.getParentId());
        if (hostel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hostel found");
        }

        if (payloads != null && payloads.isHostelBased() != null && payloads.isRoomBased() != null) {
            if (payloads.isHostelBased() && payloads.isRoomBased()) {
                return new ResponseEntity<>(Utils.CANNOT_ENABLE_HOSTEL_ROOM_READINGS, HttpStatus.BAD_GATEWAY);
            }
        }

        if (payloads != null && payloads.isProRate() != null) {
            if (payloads.isProRate()) {
                if (!Utils.checkNullOrEmpty(payloads.calculationStartingDate())) {
                    return new ResponseEntity<>(Utils.INVALID_STARTING_DATE, HttpStatus.BAD_REQUEST);
                }
            }
        }

        ElectricityConfig ebConfig = hostel.getElectricityConfig();
        if (ebConfig == null) {
            ebConfig = new ElectricityConfig();
            ebConfig.setBillDate(1);
            ebConfig.setShouldIncludeInRent(true);
        }
        if (payloads != null && payloads.isHostelBased() != null) {
            if (payloads.isHostelBased()) {
                ebConfig.setUpdated(true);
                ebConfig.setTypeOfReading(EBReadingType.HOSTEL_READING.name());
            }
        }

        if (payloads != null && payloads.isRoomBased() != null) {
            if (payloads.isRoomBased()) {
                ebConfig.setUpdated(true);
                ebConfig.setTypeOfReading(EBReadingType.ROOM_READING.name());
            }
        }

        if (payloads != null && payloads.isProRate() != null) {
            ebConfig.setProRate(payloads.isProRate());
            ebConfig.setUpdated(true);
            if (payloads.isProRate()) {
                ebConfig.setBillDate(payloads.calculationStartingDate());
            }
        }

        if (payloads != null && payloads.shouldIncludeInRent() != null) {
            ebConfig.setUpdated(true);
            ebConfig.setShouldIncludeInRent(payloads.shouldIncludeInRent());
        }

        hostel.setElectricityConfig(ebConfig);
        ebConfig.setHostel(hostel);

        hostelV1Repository.save(hostel);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }


    public ResponseEntity<?> viewBillingRules(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, users.getParentId());
        if (hostel == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        BillingRules billingRules = null;
        if (hostel.getBillingRulesList() != null && !hostel.getBillingRulesList().isEmpty()) {
            billingRules = hostel.getBillingRulesList().get(hostel.getBillingRulesList().size()-1);

            com.smartstay.smartstay.responses.hostelConfig.BillingRules rules = new com.smartstay.smartstay.responses.hostelConfig.BillingRules(
                    billingRules.getBillingStartDate(),
                    billingRules.getBillDueDays(),
                    billingRules.getNoticePeriod(),
                    Utils.dateToString(billingRules.getStartFrom()));

            return new ResponseEntity<>(rules, HttpStatus.OK);
        }

        else {
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> updateBillingRules(String hostelId, BillRules billRules) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelV1Repository.findByHostelIdAndParentId(hostelId, users.getParentId());
        if (hostel == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        List<BookingsV1> bookingsV1 = bookingsService.checkAllByHostelId(hostelId);
        if (bookingsV1 != null && !bookingsV1.isEmpty()) {
            return new ResponseEntity<>(Utils.CANNOT_MODIFY_BILLING_DATE_TENANT_EXIST_ERROR, HttpStatus.BAD_REQUEST);
        }

        BillingDates billDates = getBillingRuleOnDate(hostelId, new Date());

        BillingRules currentBillingRules = hostelConfigService.getCurrentBillingRule(hostel.getHostelId());
        BillingRules newBillingRules =  new BillingRules();


        if (Utils.checkNullOrEmpty(billRules.startDate())) {
            newBillingRules.setBillingStartDate(billRules.startDate());

        }
        else {
            if (currentBillingRules != null) {
                newBillingRules.setBillingStartDate(currentBillingRules.getBillingStartDate());
            }
            else {
                newBillingRules.setBillingStartDate(1);
            }
        }
        if (Utils.checkNullOrEmpty(billRules.dueDate())) {
            newBillingRules.setBillDueDays(billRules.dueDate());
        }
        else {
            if (currentBillingRules != null) {
                newBillingRules.setBillDueDays(currentBillingRules.getBillDueDays());
            }
            else {
                newBillingRules.setBillDueDays(10);
            }
        }
        if (Utils.checkNullOrEmpty(billRules.noticeDays())) {
            newBillingRules.setNoticePeriod(billRules.noticeDays());
        }
        else {
            if (currentBillingRules != null) {
                newBillingRules.setNoticePeriod(currentBillingRules.getNoticePeriod());
            }else {
                newBillingRules.setNoticePeriod(10);
            }
        }

        newBillingRules.setHostel(hostel);
        newBillingRules.setInitial(false);
        newBillingRules.setCreatedAt(new Date());
        newBillingRules.setCreatedBy(authentication.getName());

        List<BillingRules> listBillingRules = hostel.getBillingRulesList();
        listBillingRules.add(newBillingRules);

        hostel.setBillingRulesList(listBillingRules);
        hostelV1Repository.save(hostel);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public BillingDates getBillingRuleOnDate(String hostelId, Date date) {
        return hostelConfigService.getBillingRuleByDateAndHostelId(hostelId, date);
    }

    public ResponseEntity<?> updatePgInformation(String hostelId, UpdatePg updatePg, MultipartFile mainImage, List<MultipartFile> additionalImages) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelV1Repository.findByHostelId(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (updatePg == null && mainImage == null && additionalImages == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (mainImage != null) {
            String mainImageUrl = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(mainImage), "Hostel-Images");
            hostelV1.setMainImage(mainImageUrl);
        }

        List<String> listImageUrls = new ArrayList<>();
        if (additionalImages != null && !additionalImages.isEmpty()) {
            listImageUrls = additionalImages.stream().map(multipartFile -> uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(multipartFile), "Hostel-Images")).collect(Collectors.toList());
        }

        if (!listImageUrls.isEmpty()) {
            List<HostelImages> listImg = hostelV1.getAdditionalImages();
            if (listImg == null) {
                listImg = new ArrayList<>();
            }

            if (hostelV1.getAdditionalImages() != null) {
                listImageUrls.forEach(item -> {
                    HostelImages hostelImg = new HostelImages();
                    hostelImg.setCreatedBy(authentication.getName());
                    hostelImg.setImageUrl(item);
                    hostelImg.setHostel(hostelV1);


                    hostelV1.getAdditionalImages().add(hostelImg);
                });
            }

            else {
                listImg.addAll(listImageUrls.stream().map(item -> {
                    HostelImages hostelImg = new HostelImages();
                    hostelImg.setCreatedBy(authentication.getName());
                    hostelImg.setImageUrl(item);
                    hostelImg.setHostel(hostelV1);
                    return hostelImg;
                }).toList());

                hostelV1.setAdditionalImages(listImg);
            }



//            hostelV1.setAdditionalImages(new ArrayList<>(listImg));
        }

        if (updatePg != null) {
            if (updatePg.city() != null) {
                hostelV1.setCity(updatePg.city());
            }

            if (updatePg.street() != null) {
                hostelV1.setStreet(updatePg.street());
            }
            else if (hostelV1.getStreet() != null) {
                hostelV1.setStreet(null);
            }

            if (updatePg.landmark() != null) {
                hostelV1.setLandmark(updatePg.landmark());
            }
            else if (hostelV1.getLandmark() != null) {
                hostelV1.setLandmark(null);
            }

            if (updatePg.city() != null) {
                hostelV1.setCity(updatePg.city());
            }

            if (updatePg.pincode() != null) {
                if (updatePg.pincode() != 0) {
                    hostelV1.setPincode(updatePg.pincode());
                }
            }
            if (updatePg.state() != null) {
                hostelV1.setState(updatePg.state());
            }
            if (updatePg.hostelName() != null && !updatePg.hostelName().isEmpty()) {
                hostelV1.setHostelName(updatePg.hostelName());
            }
            if (updatePg.mobile() != null && !updatePg.mobile().isEmpty()) {
                hostelV1.setMobile(updatePg.mobile());
            }

            if (updatePg.houseNo() != null) {
                hostelV1.setHouseNo(updatePg.houseNo());
            }

            if (updatePg.emailId() != null) {
                hostelV1.setEmailId(updatePg.emailId());
            }
        }

        hostelV1Repository.save(hostelV1);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public BillingDates getNextBillingDates(String hostelId) {
        return hostelConfigService.getNextMonthBillingDates(hostelId);
    }

    public List<BillingRules> findAllHostelsHavingBillingToday() {
        return hostelConfigService.findAllHostelsHavingBillingToday();
    }

    public List<HostelV1> findAHostelsHavingBillingRuleEndingToday() {
        return hostelConfigService.findAHostelsHavingBillingRuleEndingToday();
    }

    public void updateHostel(HostelV1 hostelV1) {
        hostelV1Repository.save(hostelV1);
    }
}

