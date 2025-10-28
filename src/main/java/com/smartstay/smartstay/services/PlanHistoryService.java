package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.HostelPlanHistory;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.repositories.PlanHistoryRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlanHistoryService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansService plansService;
    @Autowired
    private PlanHistoryRepository planHistoryRepository;

    public void addHostel(String hostelId, Date joiningDate) {
        if (!authentication.isAuthenticated()) {
            return;
        }

        Plans plans = plansService.getTrialPlan();
        Date endingDate = null;
        if (plans != null) {
            endingDate = Utils.addDaysToDate(joiningDate, plans.getDuration().intValue());
        }
        HostelPlanHistory history = new HostelPlanHistory();
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
}
