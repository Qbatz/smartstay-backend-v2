package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.OrderHistoryRepository;
import com.smartstay.smartstay.repositories.PlansRepository;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import com.smartstay.smartstay.responses.plans.BillingHistoryItem;
import com.smartstay.smartstay.responses.plans.PlanDetails;
import com.smartstay.smartstay.responses.plans.PlansList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlansService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PlansRepository plansRepository;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    private HostelService hostelService;

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }

    public Plans getTrialPlan() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findPlanByPlanTypeAndIsActiveTrue(PlanType.TRIAL.name());
    }

    public ResponseEntity<?> getAllPlans() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<Plans> listAllPlansExceptTrial = plansRepository.findAll()
                .stream().filter(item -> !item.getPlanType().equalsIgnoreCase(PlanType.TRIAL.name()) && item.isShouldShow())
                .toList();

        List<PlansList> plans = listAllPlansExceptTrial
                .stream()
                .map(item -> new PlanListMapper().apply(item))
                .toList();

        return new ResponseEntity<>(plans, HttpStatus.OK);
    }

    public ResponseEntity<?> getPlanByHostelId(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelV1.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        HostelPlan hostelPlan = hostelV1.getHostelPlan();
        boolean isReactivated = false;
        long noOfDaysRemaining = 0;
        double subscriptionAmount = 0.0;
        if (hostelPlan != null) {
            noOfDaysRemaining = Utils.findNumberOfDays(new Date(), hostelPlan.getCurrentPlanEndsAt());
        }

        Plans plans = plansRepository.findPlanByPlanCode(hostelPlan.getCurrentPlanCode());
        List<String> features = new ArrayList<>();
        if (plans != null) {
            features = plans.getFeaturesList()
                    .stream()
                    .map(PlanFeatures::getFeatureName)
                    .toList();
        }

        Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);

        double resolvedPlanAmount = 0.0;
        if (latestSubscription != null && latestSubscription.getPlanAmount() != null) {
            resolvedPlanAmount = latestSubscription.getPlanAmount();
        } else if (hostelPlan.getCurrentPlanPrice() != null) {
            resolvedPlanAmount = hostelPlan.getCurrentPlanPrice();
        } else if (plans != null && plans.getPrice() != null) {
            resolvedPlanAmount = plans.getPrice();
        }
        String formattedPlanAmount = "₹" + (long) resolvedPlanAmount + "/month";


        String renewalDate;
        if (latestSubscription != null && latestSubscription.getNextBillingAt() != null) {
            renewalDate = Utils.dateToString(latestSubscription.getNextBillingAt());
        } else if (latestSubscription != null && latestSubscription.getPlanEndsAt() != null) {
            renewalDate = Utils.dateToString(latestSubscription.getPlanEndsAt());
        } else {
            renewalDate = Utils.dateToString(hostelPlan.getTrialEndingAt());
        }

        String status;
        if (hostelPlan.isTrial()) {
            status = "TRIAL";
        } else if (latestSubscription != null && Boolean.TRUE.equals(latestSubscription.getIsActive())) {
            status = "ACTIVE";
        } else {
            status = "EXPIRED";
        }

        List<OrderHistory> orderHistoryList = orderHistoryRepository.findByHostelIdOrderByCreatedAtDesc(hostelId);
        List<String> listPlanCodes = orderHistoryList.stream()
                .map(OrderHistory::getPlanCode)
                .distinct()
                .toList();

        List<Plans> listPlans = plansRepository.findPlansByPlanCodes(listPlanCodes);

        List<String> paidByIds = orderHistoryList.stream()
                .map(OrderHistory::getPaidBy)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());

        Map<String, Users> paidByUserMap = usersService.findAllUsersFromUserId(paidByIds)
                .stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        List<BillingHistoryItem> billingHistory = orderHistoryList.stream().map(oh -> {

            String paymentMethod = resolvePaymentMethod(oh);
            String subscriptionNo = hostelPlan.getSubscriptionNumber() != null
                    ? hostelPlan.getSubscriptionNumber() : "";

            String paidById = oh.getPaidBy();
            String paidByName = "";
            String planName = null;
            if (paidById != null && paidByUserMap.containsKey(paidById)) {
                Users paidByUser = paidByUserMap.get(paidById);
                String firstName = paidByUser.getFirstName() != null ? paidByUser.getFirstName() : "";
                String lastName = paidByUser.getLastName() != null ? paidByUser.getLastName() : "";
                paidByName = (firstName + " " + lastName).trim();
            }

            Plans plans1 = listPlans.stream()
                    .filter(i -> i.getPlanCode().equalsIgnoreCase(oh.getPlanCode()))
                    .findFirst()
                    .orElse(null);
            if (plans1 != null) {
                planName = plans1.getPlanName();
            }

            return new BillingHistoryItem(
                    oh.getHistoryId(),
                    subscriptionNo,
                    planName,
                    oh.getPlanCode(),
                    oh.getPlanAmount(),
                    oh.getDiscountAmount(),
                    oh.getTotalAmount(),
                    oh.getOrderStatus(),
                    oh.getPaymentType(),
                    paymentMethod,
                    paidById,
                    paidByName,
                    Utils.dateToDateTime(oh.getCreatedAt())
            );
        }).toList();

        String currentPaymentMethod = orderHistoryList.stream()
                .filter(oh -> "PAID".equalsIgnoreCase(oh.getOrderStatus()))
                .findFirst()
                .map(this::resolvePaymentMethod)
                .orElse("N/A");


        PlanDetails planDetails = new PlanDetails(
                String.valueOf(hostelPlan.getHostelPlanId()),
                hostelPlan.getCurrentPlanCode(),
                hostelPlan.getCurrentPlanName(),
                hostelPlan.getSubscriptionNumber(),
                noOfDaysRemaining,
                isReactivated,
                Utils.dateToString(hostelPlan.getCurrentPlanStartsAt()),
                Utils.dateToString(hostelPlan.getCurrentPlanEndsAt()),
                Utils.roundOffWithTwoDigit(subscriptionAmount),
                features,
                formattedPlanAmount,
                renewalDate,
                currentPaymentMethod,
                status,
                billingHistory
        );

        return new ResponseEntity<>(planDetails, HttpStatus.OK);
    }


    private String resolvePaymentMethod(OrderHistory oh) {
        if (oh.getPaymentType() == null) return "N/A";
        if ("UPI".equalsIgnoreCase(oh.getPaymentType())) {
            return oh.getUpiId() != null ? "UPI - " + oh.getUpiId() : "UPI";
        }
        if ("CARD".equalsIgnoreCase(oh.getPaymentType())) {
            StringBuilder card = new StringBuilder();
            if (oh.getCardBrand() != null) card.append(oh.getCardBrand()).append(" ");
            if (oh.getCardType() != null) card.append(oh.getCardType()).append(" ");
            if (oh.getCardNo() != null) card.append("****").append(oh.getCardNo());
            String label = card.toString().trim();
            return label.isEmpty() ? "Card" : label;
        }
        return oh.getPaymentType();
    }

    public Plans findPlanByPlanCode(String s) {
        return plansRepository.findPlanByPlanCode(s);
    }
}
