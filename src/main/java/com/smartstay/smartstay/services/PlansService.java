package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.Wrappers.subscription.SubscriptionOrderHistoryMapper;
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

import java.util.*;
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
    private OrderHistoryService orderHistoryService;

    private HostelService hostelService;
    private SubscriptionService subscriptionService;

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }
    @Autowired
    public void setSubscriptionService(@Lazy SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
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

        List<String> planType = new ArrayList<>();
        planType.add(PlanType.TRIAL.name());
        planType.add("EXPANDABLE_TRIAL");

        List<Plans> trialPlans = plansRepository.findPlansByPlanType(planType);
        List<String> planCodes = trialPlans
                .stream()
                .map(Plans::getPlanCode)
                .toList();

        List<Plans> listAllPlansExceptTrial = plansRepository.findActivePayablePlans(planCodes)
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

        Subscription latestSubscription = subscriptionService.findLatestSubscription(hostelId);

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

        boolean isTrial = hostelPlan.isTrial()
                || (plans != null && PlanType.TRIAL.name().equalsIgnoreCase(plans.getPlanType()));



        List<Subscription> listSubscriptions = subscriptionService.getSubscriptionList(hostelId);
        List<Subscription> subscriptionsWithOrderIds = listSubscriptions
                .stream()
                .filter(i -> i.getOrderId() != null)
                .toList();
        List<Long> listOrderIds = new ArrayList<>();
        if (subscriptionsWithOrderIds != null) {
            listOrderIds = subscriptionsWithOrderIds
                    .stream()
                    .map(Subscription::getOrderId)
                    .toList();
        }

        List<OrderHistory> listOrderHistory;
        String currentPaymentMethod = null;
        Map<String, Users> paidByUserMap;
        List<String> listPlanCodes = new ArrayList<>();
        List<Plans> listPlans;
        if (!listOrderIds.isEmpty()) {

            listOrderHistory = orderHistoryService.findOrderHistoryByOrderHistoryId(listOrderIds);

            listPlanCodes = listOrderHistory.stream()
                    .map(OrderHistory::getPlanCode)
                    .distinct()
                    .toList();

            listPlans = plansRepository.findPlansByPlanCodes(listPlanCodes);

            List<String> paidByIds = listOrderHistory.stream()
                    .map(OrderHistory::getPaidBy)
                    .filter(id -> id != null && !id.isBlank())
                    .distinct()
                    .collect(Collectors.toList());

            paidByUserMap = usersService.findAllUsersFromUserId(paidByIds)
                    .stream()
                    .collect(Collectors.toMap(Users::getUserId, u -> u));

            currentPaymentMethod = listOrderHistory.stream()
                    .filter(oh -> "PAID".equalsIgnoreCase(oh.getOrderStatus()))
                    .findFirst()
                    .map(Utils::resolvePaymentMethod)
                    .orElse("N/A");
        } else {
            listPlans = new ArrayList<>();
            paidByUserMap = new HashMap<>();
            listOrderHistory = new ArrayList<>();
        }

        List<BillingHistoryItem> billingHistory = listSubscriptions.stream().map(sub -> new SubscriptionOrderHistoryMapper(listOrderHistory, paidByUserMap, listPlans).apply(sub)).toList();


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
                isTrial,
                billingHistory
        );

        return new ResponseEntity<>(planDetails, HttpStatus.OK);
    }




    public Plans findPlanByPlanCode(String s) {
        return plansRepository.findPlanByPlanCode(s);
    }
}
