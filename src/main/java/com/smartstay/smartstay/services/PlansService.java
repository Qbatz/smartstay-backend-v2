package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.PlansRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlansService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansRepository plansRepository;

    public Plans getTrialPlan() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findPlanByPlanType(PlanType.TRIAL.name());
    }
}
