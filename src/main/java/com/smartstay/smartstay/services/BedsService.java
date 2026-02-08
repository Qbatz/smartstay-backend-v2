package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BedsMapper;
import com.smartstay.smartstay.Wrappers.FreeBedsMapper;
import com.smartstay.smartstay.Wrappers.beds.BedInitializationMapper;
import com.smartstay.smartstay.Wrappers.beds.CustomersTenantMapper;
import com.smartstay.smartstay.Wrappers.beds.InitializeBedsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.dto.beds.*;
import com.smartstay.smartstay.dto.beds.FreeBeds;
import com.smartstay.smartstay.dto.booking.BedBookingStatus;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.invoices.InvoiceCustomer;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.payloads.beds.AddBed;
import com.smartstay.smartstay.payloads.beds.EditBedRent;
import com.smartstay.smartstay.payloads.beds.UpdateBed;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.beds.*;
import com.smartstay.smartstay.responses.beds.BedDetails;
import com.smartstay.smartstay.responses.customer.InitializeBooking;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BedsService {

    @Autowired
    RolesRepository rolesRepository;
    @Autowired
    BedsRepository bedsRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private BookingsService bookingService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RoomsService roomsService;
    private CustomersService customersService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private InvoiceV1Service invoiceService;

    @Autowired
    public void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }

    public ResponseEntity<?> getAllBedsNew(int roomId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<Beds> listBeds = bedsRepository.findAllByRoomIdAndParentId(roomId, user.getParentId());
        List<Integer> listBedId = listBeds
                .stream()
                .map(Beds::getBedId)
                .toList();
        List<FloorNameRoomName> nameMapping = bedsRepository.getBedNameRoomName(listBedId);
        List<BedBookingStatus> bedsCurrentStatus = bookingService.getBookingDetailsByBedIds(listBedId);

        List<String> lisCustomerIds = bedsCurrentStatus
                .stream()
                .map(BedBookingStatus::customerId)
                .toList();

        List<InvoiceCustomer> listInvoiceIdCustomerId = bookingService.findDueCustomers(lisCustomerIds);

        List<BedsResponse> bedsResponses = listBeds
                .stream()
                .map(item -> new BedsMapper(nameMapping, bedsCurrentStatus, listInvoiceIdCustomerId).apply(item)).toList();
        return new ResponseEntity<>(bedsResponses, HttpStatus.OK);
    }

    public ResponseEntity<?> getBedByIdNew(Integer id) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Beds beds = bedsRepository.findById(id).orElse(null);
        if (beds == null) {
            return new ResponseEntity<>(Utils.INVALID_BED_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), beds.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        BedInformations bedInformations = bedsRepository.getBedInformation(beds.getBedId());

        boolean isOccupied = false;
        boolean onNotice = false;
        boolean hasBooking = false;
        String freeFrom = null;
        Integer floorId = 0;
        String floorName = null;
        String roomName = null;

        if (bedInformations != null) {
            floorId = bedInformations.floorId();
            floorName = bedInformations.floorName();
            roomName = bedInformations.roomName();
        }

        List<TenantInfo> currentTenantInfo = new ArrayList<>();
        List<TenantInfo> newTenantInfo = new ArrayList<>();

        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name()) || beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            isOccupied = true;
            List<BookingsV1> bookingsV1 = bookingService.checkOccupiedByBedId(beds.getBedId());
            if (bookingsV1 != null) {
                BookingsV1 currentCheckIn = bookingsV1
                        .stream()
                        .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()))
                        .findFirst()
                        .orElse(null);
                if (currentCheckIn != null) {
                    Customers customers = customersService.getCustomerInformation(currentCheckIn.getCustomerId());
                    String tenantId = null;
                    String firstName = null;
                    String lastName = null;
                    String profilePic = null;
                    StringBuilder fullName = new StringBuilder();
                    StringBuilder initials = new StringBuilder();
                    String joiningDate = null;
                    String bookingDate = null;
                    Double bookingAmount = 0.0;
                    String mobile = null;
                    double advance = 0.0;
                    Double rentAmount = null;
                    Double lastInvoiceAmount = null;
                    String lastInvoiceNumber = null;
                    int totalInvoice = 0;
                    String leavingDate = null;
                    String currentStatus = BookingStatus.CHECKIN.name();

                    bookingAmount = currentCheckIn.getBookingAmount();

                    totalInvoice = invoiceService.findAllRentalInvoicesAmountExceptCurrentMonth(customers.getCustomerId(), customers.getHostelId());
                    InvoicesV1 latestInvoices = invoiceService.findLatestInvoice(customers.getCustomerId());
                    if (latestInvoices != null) {
                        lastInvoiceAmount = latestInvoices.getTotalAmount();
                        lastInvoiceNumber = latestInvoices.getInvoiceNumber();
                    }
                    if (currentCheckIn.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())) {
                        currentStatus = BookingStatus.NOTICE.name();
                        onNotice = true;
                        freeFrom = Utils.dateToString(currentCheckIn.getLeavingDate());
                        leavingDate = Utils.dateToString(currentCheckIn.getLeavingDate());
                    }

                    joiningDate = Utils.dateToString(currentCheckIn.getJoiningDate());
                    bookingDate = Utils.dateToString(currentCheckIn.getBookingDate());
                    rentAmount = currentCheckIn.getRentAmount();

                    if (customers != null) {
                        tenantId = customers.getCustomerId();
                        firstName = customers.getFirstName();
                        lastName = customers.getLastName();
                        profilePic = customers.getProfilePic();
                        mobile = customers.getMobile();
                        if (customers.getAdvance() != null) {
                            advance = customers.getAdvance().getAdvanceAmount();
                        }

                        if (customers.getFirstName() != null) {
                            fullName.append(customers.getFirstName());
                            initials.append(customers.getFirstName().toUpperCase().charAt(0));
                        }
                        if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
                            fullName.append(" ");
                            fullName.append(customers.getLastName());
                            initials.append(customers.getLastName().toUpperCase().charAt(0));
                        }
                        else if (customers.getFirstName() != null) {
                            if (customers.getFirstName().length() > 1) {
                                initials.append(customers.getFirstName().toUpperCase().charAt(1));
                            }
                        }

                    }

                    TenantInfo checkInTenantInfo = new TenantInfo(tenantId,
                            firstName,
                            lastName,
                            fullName.toString(),
                            profilePic,
                            initials.toString(),
                            joiningDate,
                            bookingDate,
                            bookingAmount,
                            mobile,
                            advance,
                            rentAmount,
                            lastInvoiceAmount,
                            lastInvoiceNumber,
                            totalInvoice,
                            leavingDate,
                            currentStatus,
                            Utils.COUNTRY_CODE);

                    currentTenantInfo.add(checkInTenantInfo);
                }

                BookingsV1 noticeTenant = bookingsV1
                        .stream()
                        .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name()))
                        .findFirst()
                        .orElse(null);

                if (noticeTenant != null) {
                    Customers customers = customersService.getCustomerInformation(noticeTenant.getCustomerId());
                    String tenantId = null;
                    String firstName = null;
                    String lastName = null;
                    String profilePic = null;
                    StringBuilder fullName = new StringBuilder();
                    StringBuilder initials = new StringBuilder();
                    String joiningDate = null;
                    String bookingDate = null;
                    Double bookingAmount = 0.0;
                    String mobile = null;
                    double advance = 0.0;
                    Double rentAmount = null;
                    Double lastInvoiceAmount = null;
                    String lastInvoiceNumber = null;
                    int totalInvoice = 0;
                    String leavingDate = null;
                    String currentStatus = BookingStatus.CHECKIN.name();

                    bookingAmount = noticeTenant.getBookingAmount();

                    totalInvoice = invoiceService.findAllRentalInvoicesAmountExceptCurrentMonth(customers.getCustomerId(), customers.getHostelId());
                    InvoicesV1 latestInvoices = invoiceService.findLatestInvoice(customers.getCustomerId());
                    if (latestInvoices != null) {
                        lastInvoiceAmount = latestInvoices.getTotalAmount();
                        lastInvoiceNumber = latestInvoices.getInvoiceNumber();
                    }
                    if (noticeTenant.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())) {
                        currentStatus = BookingStatus.NOTICE.name();
                        onNotice = true;
                        freeFrom = Utils.dateToString(noticeTenant.getLeavingDate());
                        leavingDate = Utils.dateToString(noticeTenant.getLeavingDate());
                    }

                    joiningDate = Utils.dateToString(noticeTenant.getJoiningDate());
                    bookingDate = Utils.dateToString(noticeTenant.getBookingDate());
                    rentAmount = noticeTenant.getRentAmount();

                    if (customers != null) {
                        tenantId = customers.getCustomerId();
                        firstName = customers.getFirstName();
                        lastName = customers.getLastName();
                        profilePic = customers.getProfilePic();
                        mobile = customers.getMobile();
                        if (customers.getAdvance() != null) {
                            advance = customers.getAdvance().getAdvanceAmount();
                        }

                        if (customers.getFirstName() != null) {
                            fullName.append(customers.getFirstName());
                            initials.append(customers.getFirstName().toUpperCase().charAt(0));
                        }
                        if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
                            fullName.append(" ");
                            fullName.append(customers.getLastName());
                            initials.append(customers.getLastName().toUpperCase().charAt(0));
                        }
                        else if (customers.getFirstName() != null) {
                            if (customers.getFirstName().length() > 1) {
                                initials.append(customers.getFirstName().toUpperCase().charAt(1));
                            }
                        }

                    }

                    TenantInfo noticeTenantInfo = new TenantInfo(tenantId,
                            firstName,
                            lastName,
                            fullName.toString(),
                            profilePic,
                            initials.toString(),
                            joiningDate,
                            bookingDate,
                            bookingAmount,
                            mobile,
                            advance,
                            rentAmount,
                            lastInvoiceAmount,
                            lastInvoiceNumber,
                            totalInvoice,
                            leavingDate,
                            currentStatus,
                            Utils.COUNTRY_CODE);

                    currentTenantInfo.add(noticeTenantInfo);
                }
            }
        }
        if (beds.isBooked()) {
            hasBooking = true;
            List<BookingsV1> bookingsV1 = bookingService.getBookingInfoByBedId(beds.getBedId());
            if (bookingsV1 != null) {
                List<String> customerIds = bookingsV1
                        .stream()
                        .map(BookingsV1::getCustomerId)
                        .toList();
                List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
                if (listCustomers != null) {
                    newTenantInfo.addAll(listCustomers
                            .stream()
                            .map(i -> new CustomersTenantMapper(bookingsV1).apply(i))
                            .toList());
                }

            }
        }

        BedDetails bedDetails = new BedDetails(beds.getBedName(),
                beds.getBedId(),
                beds.getHostelId(),
                hasBooking,
                onNotice,
                isOccupied,
                beds.getRentAmount(),
                beds.getRoomId(),
                freeFrom,
                floorId,
                floorName,
                roomName,
                currentTenantInfo,
                newTenantInfo);

        return new ResponseEntity<>(bedDetails, HttpStatus.OK);


    }

    public ResponseEntity<?> updateBedById(int bedId, UpdateBed updateBed) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId, user.getParentId());
        if (existingBed == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }

        if (updateBed == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (updateBed.bedName() != null && !updateBed.bedName().isEmpty()) {
            int duplicateCount = bedsRepository.countByBedNameAndBedId(
                    updateBed.bedName(), bedId, existingBed.getRoomId()
            );
            if (duplicateCount > 0) {
                return new ResponseEntity<>("Bed name already exists in this room", HttpStatus.CONFLICT);
            }
            existingBed.setBedName(updateBed.bedName());
        }
        if (updateBed.isActive() != null) {
            existingBed.setIsActive(updateBed.isActive());
        }
        if (updateBed.amount() != null) {
            if (updateBed.amount() != 0) {
                existingBed.setRentAmount(updateBed.amount());
            }
        }
        existingBed.setUpdatedAt(new Date());
        bedsRepository.save(existingBed);
        usersService.addUserLog(existingBed.getHostelId(), String.valueOf(existingBed.getBedId()), ActivitySource.BEDS, ActivitySourceType.UPDATE, user);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addBed(AddBed addBed) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        boolean exists = roomRepository.checkRoomExistInTable(addBed.roomId(), user.getParentId(), addBed.hostelId()) == 1;
        if (!exists) {
            return new ResponseEntity<>("Room Doesn't exist for this hostel", HttpStatus.BAD_REQUEST);
        }

        int duplicateCount = bedsRepository.countByBedNameAndRoomAndHostelAndParent(
                addBed.bedName(),
                addBed.roomId(),
                addBed.hostelId(),
                user.getParentId()
        );
        if (duplicateCount > 0) {
            return new ResponseEntity<>("Bed name already exists in this room", HttpStatus.CONFLICT);
        }

        Beds beds = new Beds();
        beds.setCreatedAt(new Date());
        beds.setUpdatedAt(new Date());
        beds.setIsActive(true);
        beds.setIsDeleted(false);
        beds.setBedName(addBed.bedName());
        beds.setParentId(user.getParentId());
        beds.setRoomId(addBed.roomId());
        beds.setHostelId(addBed.hostelId());
        beds.setRentAmount(addBed.amount());
        beds.setStatus(BedStatus.VACANT.name());
        beds.setCurrentStatus(BedStatus.VACANT.name());
        beds.setFreeFrom(null);
        beds.setRentAmount(addBed.amount());
        Beds bedsV1 = bedsRepository.save(beds);
        usersService.addUserLog(addBed.hostelId(), String.valueOf(bedsV1.getBedId()), ActivitySource.BEDS, ActivitySourceType.CREATE, user);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteBedById(int bedId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (bookingService.checkIsBedOccupied(bedId)) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_OCCUPIED_BEDS, HttpStatus.BAD_REQUEST);

        }
        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId, users.getParentId());
        if (existingBed == null) {
            return new ResponseEntity<>(Utils.NO_BED_FOUND_ERROR, HttpStatus.BAD_REQUEST);
        }

        bedsRepository.delete(existingBed);
        usersService.addUserLog(existingBed.getHostelId(), String.valueOf(existingBed.getBedId()), ActivitySource.BEDS, ActivitySourceType.DELETE, users);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);


    }

    //assign bed
    public ResponseEntity<?> addUserToBed(int bedId, String joiningDate, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId, users.getParentId());
        if (existingBed != null) {
            if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(joiningDate, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
                existingBed.setStatus(BedStatus.BOOKED.name());
                existingBed.setBooked(true);
            } else {
                boolean isOtherBooingExist = bookingService.checkOtherBookings(existingBed.getBedId(), customerId);
                if (isOtherBooingExist) {
                    existingBed.setBooked(true);
                }
                else {
                    existingBed.setBooked(false);
                }
                existingBed.setStatus(BedStatus.OCCUPIED.name());
                existingBed.setCurrentStatus(BedStatus.OCCUPIED.name());
                existingBed.setFreeFrom(null);
            }

            existingBed.setUpdatedAt(new Date());

            bedsRepository.save(existingBed);

        }
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    /**
     * this works when booking a customer
     *
     * @return
     */
    public ResponseEntity<?> assignCustomer(int bedId, String joiningDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId, users.getParentId());
        if (existingBed != null) {

            existingBed.setStatus(BedStatus.BOOKED.name());
            existingBed.setBooked(true);

            existingBed.setUpdatedAt(new Date());

            bedsRepository.save(existingBed);

        }
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public boolean isBedAvailable(int bedId, String parentId, Date joiningDate) {
        Beds beds = bedsRepository.findByBedIdAndParentId(bedId, parentId);

        if (beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name()) || beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            BookingsV1 bookingsV1 = bookingService.checkLatestStatusForBed(bedId);
            if (bookingsV1.getLeavingDate() != null) {
                return Utils.compareWithTwoDates(bookingsV1.getLeavingDate(), joiningDate) <= 0;
            }
        } else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            return false;
        } else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
            //check if the bed is checked in on this date
            List<BookingsV1> bookingsV1 = bookingService.findAvailableBookingOnDate(bedId, joiningDate);
            if (bookingsV1 != null && !bookingsV1.isEmpty()) {
                return false;
            }
            return true;
        } else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            BookingsV1 bookingsV1 = bookingService.checkLatestStatusForBed(bedId);

            if (bookingsV1 != null) {
                if (bookingsV1.getLeavingDate() != null) {
                    if (Utils.compareWithTwoDates(bookingsV1.getJoiningDate(), joiningDate) > 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
                if (bookingsV1.getExpectedJoiningDate() != null) {
                    if (Utils.compareWithTwoDates(bookingsV1.getExpectedJoiningDate(), joiningDate) > 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return true;
            }
        }

        return false;

    }

    public boolean isBedAvailableNew(int bedId, String parentId, String joiningDate) {
        Beds beds = bedsRepository.findByBedIdAndParentId(bedId, parentId);

        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
           return true;
        }
        else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            if (bookingService.isBedAvailableForCheckIn(bedId, joiningDate)) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            if (bookingService.isBedAvailableForCheckIn(bedId, joiningDate)) {
                return true;
            }
            else {
                return false;
            }
        }

        return false;

    }

    public boolean checkBedExistForRoom(int bedId, int roomId, String hostelId) {
        return bedsRepository.findByBedIdAndRoomIdAndHostelId(bedId, roomId, hostelId) != null;
    }

    public boolean checkIsBedExsits(Integer bedId, String parentId, String hostelId) {
        return bedsRepository.findByBedIdAndParentIdAndHostelId(bedId, parentId, hostelId) != null;
    }



    public int updateBedToNotice(int bedId, String relievingDate) {
        Beds bed = bedsRepository.findById(bedId).orElse(null);
        if (bed == null) {
            return 0;
        }

        bed.setUpdatedAt(new Date());
        bed.setCurrentStatus(BedStatus.NOTICE.name());
        bed.setFreeFrom(Utils.stringToDate(relievingDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

        bedsRepository.save(bed);

        return 1;
    }

    public List<BedsStatusCount> findBedCount(String hostelId) {
        return bedsRepository.getBedCountByStatus(hostelId);
    }

    public ResponseEntity<?> findFreeBeds(String hostelId) {
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

        List<FreeBeds> freeBeds = bedsRepository.getFreeBeds(hostelId);
        List<com.smartstay.smartstay.responses.beds.FreeBeds> beds = freeBeds
                .stream()
                .map(item -> new FreeBedsMapper().apply(item))
                .toList();
        return new ResponseEntity<>(beds, HttpStatus.OK);
    }

    public ResponseEntity<?> initializeBooking(String hostelId, String joiningDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        if (!Utils.checkNullOrEmpty(joiningDate)) {
            return new ResponseEntity<>(Utils.INVALID_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }


//        List<Beds> noticeBooked = listAllBeds
//                .stream()
//                .filter(i -> i.isBooked() && i.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name()))
//                .toList();

//        bedsForGettingBookingInfo.addAll(vacantBookedBeds);
//        bedsForGettingBookingInfo.addAll(noticeNotBooked);
//        bedsForGettingBookingInfo.addAll(noticeBooked);
//
//        List<Integer> bedIdsToGetBookingDetails = bedsForGettingBookingInfo
//                .stream()
//                .map(Beds::getBedId)
//                .toList();

//        List<BookingsV1> listLatestBookingInfo = bookingService.getBookingInfoByListOfBeds(bedIdsToGetBookingDetails);
//        Date dtJoiningDate = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        List<com.smartstay.smartstay.dto.beds.InitializeBooking> freeBeds = bedsRepository
                .getFreeBeds(hostelId, Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

        List<BookingBankInfo> listBanks = bankingService.getAllAccounts(hostelId);

        List<Integer> bookedBedIds = freeBeds
                .stream()
                .map(com.smartstay.smartstay.dto.beds.InitializeBooking::getBedId)
                .toList();
        List<BookingsV1> bookedBeds = bookingService.getBookingInfoByListOfBeds(bookedBedIds);

//        List<com.smartstay.smartstay.responses.beds.FreeBeds> listFreeBeds = listAllBeds
//                .stream()
//                .map(i -> new InitializeBedsMapper(listBedDetails, listLatestBookingInfo, dtJoiningDate).apply(i))
//                .toList();

        List<BedInitializations> initializations = freeBeds.stream()
                .map(i -> new BedInitializationMapper(bookedBeds).apply(i))
                .toList();

        InitializeBooking initializeBooking = new InitializeBooking(initializations, listBanks);
        return new ResponseEntity<>(initializeBooking, HttpStatus.OK);
    }

    /**
     * this is compatible only for the booked users.
     *
     * @param bedId
     * @param joiningDate
     * @return
     */
    public Beds isBedAvailabeForCheckIn(Integer bedId, Date joiningDate) {
        Beds bed = bedsRepository.checkBedAvailability(bedId);
        if (bed != null) {
            if (bed.getFreeFrom() != null) {
                if (Utils.compareWithTwoDates(new Date(), joiningDate) <= 0) {
                    return bed;
                }
                return null;
            }
            return bed;
        }
        return null;
    }

    public Beds checkAvailabilityForCheckIn(Integer bedId, Date joiningDate) {
        return bedsRepository.checkIsBedAvailable(bedId, joiningDate);
    }


    public void cancelBooking(int bedId, String parentId) {
        Beds beds = bedsRepository.findByBedIdAndParentId(bedId, parentId);
        if (beds != null) {
            beds.setBooked(false);
            if (!beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
                beds.setStatus(BedStatus.VACANT.name());
            }
            bedsRepository.save(beds);
        }
    }

    public com.smartstay.smartstay.dto.beds.BedDetails getBedDetails(Integer bedId) {
        return bedsRepository.findByBedId(bedId);
    }

    public boolean isBedAvailableForReassign(Integer bedId, String joiningDate) {
        Beds beds = bedsRepository.findById(bedId).orElse(null);
        if (beds == null) {
            return false;
        }
        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name()) && !beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
           return false;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            return bookingService.isBedAvailableByDate(bedId, joiningDate);
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            return bookingService.isBedAvailableByDate(bedId, joiningDate);
        }

        return true;
    }

    public void unassignBed(Integer bedId) {

        bedsRepository.findById(bedId).ifPresent(currentBed -> {
            if (currentBed.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name()) && currentBed.isBooked()) {
                currentBed.setCurrentStatus(BedStatus.VACANT.name());
            }
            else {
                currentBed.setCurrentStatus(BedStatus.VACANT.name());
            }
            if (currentBed.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
                currentBed.setFreeFrom(null);
            }
            bedsRepository.save(currentBed);
        });
    }

    public void reassignBed(String customerId, Integer bedId) {
        Beds beds = bedsRepository.findById(bedId).orElse(null);
        if (beds != null) {
            BookingsV1 bookingsV1 = bookingService.getBookingInfoByCustomerId(customerId);
            if (bookingsV1 != null) {
                if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())) {
                    beds.setCurrentStatus(BedStatus.NOTICE.name());
                    beds.setFreeFrom(bookingsV1.getLeavingDate());
                }
                else {
                    beds.setCurrentStatus(BedStatus.OCCUPIED.name());
                    beds.setFreeFrom(null);
                }

                bedsRepository.save(beds);
            }


        }
    }

    public BedRoomFloor findRoomAndFloorByBedIdAndHostelId(Integer bedId, String hostelId) {
        return bedsRepository.findRoomAndFloorByBedIdAndHostelId(bedId, hostelId);
    }

    public Beds makeABedVacant(int bedId) {
        Beds bed = bedsRepository.findById(bedId).orElse(null);
        if (bed == null) {
            return null;
        }
        bed.setCurrentStatus(BedStatus.VACANT.name());
        bed.setFreeFrom(new Date());
        return bedsRepository.save(bed);
    }

    public BedsStatus getBedCountsForDashboard(String hostelId) {
        List<Beds> listBeds = bedsRepository.findByHostelIdAndIsDeletedFalse(hostelId);
        Integer totalBeds = listBeds.size();
        long occupiedBeds = listBeds
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name()) ||  i.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name()))
                .count();
        Integer freeBeds = totalBeds - (int) occupiedBeds;
        Integer bookedBedCount = bookingService.getBookedBedCount(hostelId);

        return new BedsStatus(totalBeds,
                freeBeds,
                (int) occupiedBeds,
                bookedBedCount);
    }

    public List<com.smartstay.smartstay.dto.beds.BedDetails> getBedDetails(List<Integer> bedIds) {
        return bedsRepository.findByBedIds(bedIds);
    }

    public ResponseEntity<?> updateRentAmount(Integer bedId, EditBedRent bedRent) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (bedRent == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (bedRent.newRent() == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (bedRent.newRent() == 0.0) {
            return new ResponseEntity<>(Utils.RENT_AMOUNT_REQUIRED_TO_UPDATE_RENT, HttpStatus.BAD_REQUEST);
        }
        Beds beds = bedsRepository.findById(bedId).orElse(null);
        if (beds == null) {
            return new ResponseEntity<>(Utils.INVALID_BED_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), beds.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        beds.setRentAmount(bedRent.newRent());
        beds.setUpdatedAt(new Date());
        bedsRepository.save(beds);
        usersService.addUserLog(beds.getHostelId(), String.valueOf(bedId), ActivitySource.BEDS, ActivitySourceType.UPDATE, users);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public int countOccupiedByHostelId(String hostelId) {
        return bedsRepository.countOccupiedByHostelId(hostelId);
    }

    public int countAllByHostelId(String hostelId) {
        return bedsRepository.countAllByHostelId(hostelId);
    }

    public List<RoomBedCount> countBedsByRoomForHostel(String hostelId) {
        return bedsRepository.countBedsByRoomForHostel(hostelId);
    }

    public void deleteBedsByRoomId(Integer roomId, String parentId) {
        List<Beds> listBeds = bedsRepository.findAllByRoomIdAndParentId(roomId, parentId)
                .stream()
                .map(i -> {
                    i.setIsDeleted(true);
                    return i;
                })
                .toList();
        bedsRepository.saveAll(listBeds);
    }
}
