package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.PlansRepository;
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
                features
        );

        return new ResponseEntity<>(planDetails, HttpStatus.OK);
    }

    public Plans findPlanByPlanCode(String s) {
        return plansRepository.findPlanByPlanCode(s);
    }
}
