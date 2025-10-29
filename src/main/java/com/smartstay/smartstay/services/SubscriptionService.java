package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Modules;
import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SubscriptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private SubscriptionRepository planHistoryRepository;

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

        planHistoryRepository.save(history);

    }

    public ResponseEntity<?> getAllPlans() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE))
        return null;
    }
}
