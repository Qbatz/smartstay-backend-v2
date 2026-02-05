package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import com.smartstay.smartstay.responses.plans.PlansList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    private HostelService hostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }

    public void addHostel(String hostelId, Date joiningDate) {
        if (!authentication.isAuthenticated()) {
            return;
        }

        Plans plans = plansService.getTrialPlan();
        Date endingDate = null;
        if (plans != null) {
            endingDate = Utils.addDaysToDate(joiningDate, plans.getDuration().intValue());
        }
        Subscription history = new Subscription();
        history.setHostelId(hostelId);
        history.setPlanAmount(plans.getPrice());
        history.setPaidAmount(0.0);
        history.setPlanCode(plans.getPlanCode());
        history.setPlanName(plans.getPlanName());
        history.setPlanStartsAt(joiningDate);
        history.setPlanEndsAt(endingDate);
        history.setActivatedAt(new Date());
        history.setCreatedAt(new Date());

        subscriptionRepository.save(history);

    }


    public ResponseEntity<?> getCurrentPlan(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(),hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        List<Subscription> subscription = subscriptionRepository.findByHostelId(hostelId);
        if (subscription == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }

        List<com.smartstay.smartstay.responses.subscriptions.Subscription> listSubscriptionResponse = subscription
                .stream()
                .map(i ->  new com.smartstay.smartstay.responses.subscriptions.Subscription(
                        i.getSubscriptionId(),
                        Utils.dateToString(i.getPlanStartsAt()),
                        Utils.dateToString(i.getPlanEndsAt()),
                        i.getSubscriptionNumber(),
                        i.getPlanName(),
                        i.getPlanCode()))
                .toList();

        return new ResponseEntity<>(listSubscriptionResponse, HttpStatus.OK);

    }

    public boolean isSubscriptionValidToday(String hostelId) {
        Subscription subscription = subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
        return subscription != null;
    }

    public SubscriptionDto getCurrentSubscriptionDetails(String hostelId) {
        Subscription subscriptionToday = subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
        SubscriptionDto subscriptionDto = null;
        if (subscriptionToday != null) {
            boolean isValid = false;
            int planEndsIn = 0;

            Date nextBillingDate = Utils.addDaysToDate(subscriptionToday.getPlanEndsAt(), 1);

            if (Utils.compareWithTwoDates(subscriptionToday.getPlanStartsAt(), new Date()) <= 0) {
                if (Utils.compareWithTwoDates(subscriptionToday.getPlanEndsAt(), new Date()) >= 0) {
                    isValid = true;
                }
            }
            if (isValid) {
                long numberOfDays = Utils.findNumberOfDays(new Date(), subscriptionToday.getPlanEndsAt());
                planEndsIn =  (int) numberOfDays;
            }
            subscriptionDto = new SubscriptionDto(subscriptionToday.getPlanStartsAt(),
                    subscriptionToday.getPlanEndsAt(),
                    nextBillingDate,
                    isValid,
                    planEndsIn);
        }

        return subscriptionDto;
    }

    public ResponseEntity<?> subscribeSingleHostel(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);
        if (latestSubscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }
        Subscription newSubscription = new Subscription();
        newSubscription.setSubscriptionNumber(latestSubscription.getSubscriptionNumber());
        newSubscription.setHostelId(hostelId);
        newSubscription.setPlanCode(latestSubscription.getPlanCode());
        newSubscription.setPlanName(latestSubscription.getPlanName());
        newSubscription.setPlanStartsAt(new Date());
        newSubscription.setPaidAmount(0.0);
        newSubscription.setPlanAmount(0.0);
        newSubscription.setDiscount(0.0);
        newSubscription.setDiscountAmount(0.0);
        newSubscription.setCreatedAt(new Date());

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
                newSubscription.setPlanStartsAt(new Date());
                Date endDate = Utils.addDaysToDate(new Date(), 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(endDate);
            }
            else {
                Date startDate = Utils.addDaysToDate(latestSubscription.getPlanEndsAt(), 1);
                newSubscription.setPlanStartsAt(startDate);
                Date endDate = Utils.addDaysToDate(startDate, 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(new Date());
            }
        }

        subscriptionRepository.save(newSubscription);

        if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) <=0 ){
            HostelPlan hostelPlan = hostelV1.getHostelPlan();
            if (hostelPlan == null) {
                hostelPlan = new HostelPlan();
                hostelPlan.setCurrentPlanCode(latestSubscription.getPlanCode());
                hostelPlan.setCurrentPlanName(latestSubscription.getPlanName());
                hostelPlan.setHostel(hostelV1);
            }
            hostelPlan.setCurrentPlanStartsAt(newSubscription.getPlanStartsAt());
            hostelPlan.setCurrentPlanEndsAt(newSubscription.getPlanEndsAt());
            hostelPlan.setCurrentPlanPrice(0.0);
            hostelPlan.setPaidAmount(0.0);
            hostelPlan.setTrial(true);
            hostelPlan.setTrialEndingAt(newSubscription.getPlanEndsAt());

            hostelService.updateHostel(hostelV1);
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }
}
