package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.HostelsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.payloads.AddHostelPayloads;
import com.smartstay.smartstay.payloads.RemoveUserFromHostel;
import com.smartstay.smartstay.payloads.ZohoSubscriptionRequest;
import com.smartstay.smartstay.payloads.electricity.UpdateEBConfigs;
import com.smartstay.smartstay.payloads.hostel.UpdateElectricityPrice;
import com.smartstay.smartstay.payloads.templates.BillTemplates;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import com.smartstay.smartstay.responses.hostel.EBSettings;
import com.smartstay.smartstay.responses.hostel.FloorDetails;
import com.smartstay.smartstay.responses.hostel.HostelDetails;
import com.smartstay.smartstay.util.Utils;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private SubscriptionService subscriptionService;

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
    private TemplatesService hostelTemplates;

    @Autowired
    private BankingService bankingService;
    @Autowired
    private HostelBankingService hostelBankingMapper;


    public ResponseEntity<?> addHostel(MultipartFile mainImage, List<MultipartFile> additionalImages, AddHostelPayloads payloads) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
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

        ZohoSubscriptionRequest request = formSubscription(payloads, emailId);

        String hostelID = hostelIdGenerator();
        HostelV1 hostelV1 = new HostelV1();
        hostelV1.setHostelId(hostelID);
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

        //Create a CashAccount for the hostel
        BankingV1 bankingV1 = new BankingV1();
        bankingV1.setActive(true);
        bankingV1.setDeleted(false);
        bankingV1.setCreatedBy(users.getUserId());
        bankingV1.setCreatedAt(new Date());
        bankingV1.setAccountType(BankAccountType.CASH.name());
        bankingV1.setTransactionType(BankPurpose.BOTH.name());
        bankingV1.setDefaultAccount(true);
        BankingV1 v1 = bankingService.saveBankingData(bankingV1);

        hostelBankingMapper.addBankToHostel(hostelID, v1.getBankId());

        ElectricityConfig config = new ElectricityConfig();
        config.setProRate(true);
        config.setHostel(hostelV1);
        config.setCharge(5.0);
        config.setBillDate(1);
        config.setUpdated(false);
        config.setShouldIncludeInRent(true);
        config.setLastUpdate(new Date());
        config.setUpdatedBy(users.getUserId());
        config.setTypeOfReading(EBReadingType.ROOM_READING.name());
        hostelV1.setElectricityConfig(config);

        if (mainImage != null) {
            String mainImageUrl = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(mainImage), "Hostel-Images");
            hostelV1.setMainImage(mainImageUrl);
        }


        List<String> listImageUrls = new ArrayList<>();
        if (additionalImages != null && !additionalImages.isEmpty()) {
            listImageUrls = additionalImages.stream().map(multipartFile -> uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(multipartFile), "Hostel-Images")).collect(Collectors.toList());
        }

        if (!listImageUrls.isEmpty()) {
            List<HostelImages> listHostelImages = listImageUrls.stream().map(item -> {
                HostelImages hostelImg = new HostelImages();
                hostelImg.setCreatedBy(userId);
                hostelImg.setImageUrl(item);
                hostelImg.setHostel(hostelV1);
                return hostelImg;
            }).toList();

            hostelV1.setAdditionalImages(listHostelImages);
        }
        //Adding billing rules for a hostel
        //By default
        BillingRules billingRules = new BillingRules();
        billingRules.setBillingStartDate(1);
        billingRules.setBillingDueDate(10);
        billingRules.setNoticePeriod(30);
        billingRules.setHostel(hostelV1);
        List<BillingRules> listBillings = new ArrayList<>();
        listBillings.add(billingRules);

        BillTemplates templates = new BillTemplates(hostelV1.getHostelId(), payloads.mobile(), payloads.emailId(), payloads.hostelName());
        hostelTemplates.initialTemplateSetup(templates);

        Subscription subscription = subscriptionService.addSubscription(request, 1);

        if (subscription != null) {
            subscription.setHostel(hostelV1);
            hostelV1.setSubscription(List.of(subscription));
            hostelV1Repository.save(hostelV1);
//            mapUserHostel(userId, hostelV1.getHostelId(), users.getParentId());

            int result = userHostelService.addHostelToExistingUsers(users.getParentId(), hostelV1.getHostelId());

            if (result == 100) {
                List<Users> listUsers = usersService.findAllByParentId(users.getParentId());
                if (listUsers != null) {
                    userHostelService.addHostelToExistingUsers(users.getParentId(), listUsers, hostelV1.getHostelId());
                }
            }

            return new ResponseEntity<>("Created successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to subscribe in zoho", HttpStatus.BAD_REQUEST);
        }
    }


    public String hostelIdGenerator() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        HostelV1 v1 = hostelV1Repository.findById(uuidString).orElse(null);
        if (v1 == null) {
            return uuidString;
        }
        return hostelIdGenerator();
    }

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

    public ResponseEntity<?> getAllHostels() {
        List<HostelV1> listHotels = hostelV1Repository.findAll();

        List<Hostels> list = listHotels.stream().map(hostelV1 -> new HostelsMapper(0, 0, 0,0, 0).apply(hostelV1)).toList();

        return new ResponseEntity<>(list, HttpStatus.OK);
    }


    public ResponseEntity<?> fetchAllHostels() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user", HttpStatus.OK);
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
            return new HostelsMapper(noOfFloors, noOfRooms, noOfBeds[0], noOfOccupiedBeds.get(), Integer.parseInt(String.valueOf(noOfAvailableBeds[0]))).apply(Objects.requireNonNull(hostelV1Repository.findById(item.getHostelId()).orElse(null)));
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
        assert hostelV1 != null;
        hostelV1Repository.delete(hostelV1);

        userHostelService.deleteAllHostels(hostelId);

        if (hostelV1 != null) {
            return new ResponseEntity<>("Deleted", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("No hostels found", HttpStatus.BAD_REQUEST);
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

        Subscription subscription = subscriptionService.getSubscriptionByHostelId(hostelId);
        List<Floors> floors = floorsService.getFloorByHostelID(hostelId, user.getParentId());

        List<FloorDetails> floorDetails = floors.stream().map(floor -> new FloorDetails(floor.getFloorId(), floor.getFloorName())).toList();

        String nextBillingDate = Utils.dateToString(subscription.getNextBillingAt());
        boolean isSubscriptionActive = Utils.compareWithTodayDate(subscription.getNextBillingAt());
        int remainingDays = Utils.calculateRemainingDays(subscription.getNextBillingAt());

        HostelDetails details = new HostelDetails(hostel.getHostelId(), hostel.getMainImage(), hostel.getCity(), String.valueOf(hostel.getCountry()), hostel.getEmailId(), hostel.getHostelName(), hostel.getHouseNo(), hostel.getLandmark(), hostel.getMobile(), hostel.getPincode(), hostel.getState(), hostel.getStreet(), Utils.dateToString(hostel.getUpdatedAt()), isSubscriptionActive, nextBillingDate, remainingDays, floorDetails.size(), floorDetails);

        return ResponseEntity.ok(details);
    }

    public HostelV1 getHostelInfo(String hostelId) {
        return hostelV1Repository.findByHostelId(hostelId);
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
}

