package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dao.Rooms;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.responses.dashboard.Dashboard;
import com.smartstay.smartstay.responses.dashboard.DashboardNew;
import com.smartstay.smartstay.responses.dashboard.RoomsAndBedInfo;
import com.smartstay.smartstay.responses.dashboard.SharingInfo;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private HostelService hostelService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private AssetsService assetsService;
    @Autowired
    private ElectricityService electricityService;

    public ResponseEntity<?> getDashboardInfo(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_DASHBOARD, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        int totalRooms = 0;
        Integer totalBeds = 0;
        Integer freebeds = 0;
        Integer occupiedBeds = 0;
        Integer totalCustomers = 0;
        Integer bookedBeds = 0;
        Double assetsValue = 0.0;
        Double advanceAmount = 0.0;
        Double electricityAmount = 0.0;
        Double nextMonthProjections = 0.0;
        Double currentMonthProfit = 0.0;
        Double otherProfit = 0.0;
        Integer pendingInvoiceCount = 0;

        BedsStatus bedsStatus = bedsService.getBedCountsForDashboard(hostelId);
        if (bedsStatus != null) {
            totalBeds = bedsStatus.totalBeds();
            freebeds = bedsStatus.freeBeds();
            occupiedBeds = bedsStatus.occupiedBeds();
            bookedBeds = bedsStatus.bookedBeds();
        }

        totalRooms = roomsService.getRoomCount(hostelId);
        totalCustomers = bookingsService.getAllCheckedInCustomersCount(hostelId);
        assetsValue = assetsService.getAllAssetsValue(hostelId);
        advanceAmount = customersService.getAdvanceAmountFromAllCustomers(hostelId);
        electricityAmount = electricityService.getPreviousMonthEbAmount(hostelId);
        nextMonthProjections = bookingsService.getNextMonthProjections(hostelId, billingDates);
        pendingInvoiceCount = invoiceV1Service.getPendingInvoiceCounts(hostelId);

        Dashboard dashboard = new Dashboard(
                hostelId,
                totalRooms,
                totalBeds,
                freebeds,
                occupiedBeds,
                bookedBeds,
                totalCustomers,
                assetsValue,
                advanceAmount,
                electricityAmount,
                nextMonthProjections,
                currentMonthProfit,
                otherProfit,
                pendingInvoiceCount);


        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }

    public ResponseEntity<?> getDashboardInfoNew(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_DASHBOARD, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        DashboardNew dashboardNew = null;
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        List<Rooms> listRooms = roomsService.findByHostelId(hostelId);
        List<Beds> listBeds = bedsService.findByHostelId(hostelId);
        List<Beds> filledBeds = bedsService.findFilledBeds(hostelId);

        AtomicInteger totallyFilledBeds = new AtomicInteger();
        HashMap<Integer, Integer> sharingInfo = new HashMap<>();
        listRooms.forEach(item -> {
            long totalBeds = listBeds
                    .stream()
                    .filter(i -> i.getRoomId().equals(item.getRoomId()))
                    .count();
            long filledRooms = filledBeds
                    .stream()
                    .filter(i -> i.getRoomId().equals(item.getRoomId()))
                    .count();
            if (totalBeds > 0) {
                if (totalBeds == filledRooms) {
                    totallyFilledBeds.set(totallyFilledBeds.get() + 1);
                }
            }
            if (sharingInfo.containsKey(item.getSharingType())) {
                sharingInfo.put(item.getSharingType(), sharingInfo.get(item.getSharingType()) + 1);
            }
            else {
                sharingInfo.put(item.getSharingType(), 1);
            }

        });

        List<SharingInfo> shareType = new ArrayList<>();

        for (Integer key : sharingInfo.keySet()) {
            List<Rooms> roomsBasedOnSharing = listRooms
                    .stream()
                    .filter(i -> i.getSharingType().equals(key))
                    .toList();

            // Get roomIds of those rooms
            Set<Integer> roomIds = roomsBasedOnSharing
                    .stream()
                    .map(Rooms::getRoomId)
                    .collect(Collectors.toSet());

            // Filter beds belonging to those rooms
            List<Beds> filledBedsBasedOnSharing = filledBeds
                    .stream()
                    .filter(bed -> roomIds.contains(bed.getRoomId()))
                    .toList();

            List<Beds> totalBedsBasedOnSharingType = listBeds
                    .stream()
                    .filter(bed -> roomIds.contains(bed.getRoomId()))
                    .toList();

            String sharingType = null;
            if (key == 1) {
                sharingType = "Single";
            }
            else {
                sharingType = key + "";
            }

            sharingType = sharingType + " Sharing";
            int totalBedCount = totalBedsBasedOnSharingType.size();
            int filledBedsCount = filledBedsBasedOnSharing.size();

            double occupancyRatio = 0.0;

            if (totalBedCount > 0) {
                occupancyRatio = ((double) filledBedsCount / totalBedCount) * 100;
            }

            SharingInfo sharingInfo1 = new SharingInfo(sharingType,
                    totalBedCount,
                    filledBedsCount,
                    Utils.roundOffWithTwoDigit(occupancyRatio));
            shareType.add(sharingInfo1);
        }

        RoomsAndBedInfo roomsAndBedInfo = new RoomsAndBedInfo(listRooms.size(),
                totallyFilledBeds.get(),
                listBeds.size(),
                shareType);

        dashboardNew = new DashboardNew(roomsAndBedInfo);


        return new ResponseEntity<>(dashboardNew, HttpStatus.OK);
    }
}
