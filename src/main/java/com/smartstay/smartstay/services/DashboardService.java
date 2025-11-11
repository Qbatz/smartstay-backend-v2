package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.responses.dashboard.Dashboard;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

        Dashboard dashboard = new Dashboard(totalRooms,
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
}
