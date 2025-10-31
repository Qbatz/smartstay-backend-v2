package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.PlansRepository;
import com.smartstay.smartstay.responses.plans.PlansList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlansService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PlansRepository plansRepository;

    public Plans getTrialPlan() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findPlanByPlanType(PlanType.TRIAL.name());
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

}
