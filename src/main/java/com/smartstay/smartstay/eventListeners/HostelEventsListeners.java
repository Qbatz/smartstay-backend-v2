package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.events.HostelEvents;
import com.smartstay.smartstay.payloads.templates.BillTemplates;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class HostelEventsListeners {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private TemplatesService hostelTemplates;
    @Autowired
    private SubscriptionService planHistoryService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private BankingService bankingService;


    @Async
    @EventListener
    public void handleHostelCreated(HostelEvents events) {
        String hostelId = events.getHostelId();

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        ElectricityConfig config = new ElectricityConfig();
        config.setProRate(true);
        config.setHostel(hostelV1);
        config.setCharge(5.0);
        config.setBillDate(1);
        config.setUpdated(false);
        config.setShouldIncludeInRent(true);
        config.setLastUpdate(new Date());
        config.setUpdatedBy(events.getUserId());
        config.setTypeOfReading(EBReadingType.ROOM_READING.name());
        hostelV1.setElectricityConfig(config);

        BillingRules billingRules = new BillingRules();
        billingRules.setBillingStartDate(1);
        billingRules.setBillDueDays(10);
        billingRules.setNoticePeriod(30);
        billingRules.setInitial(true);
        billingRules.setHostel(hostelV1);
        List<BillingRules> listBillings = new ArrayList<>();
        listBillings.add(billingRules);

        hostelV1.setBillingRulesList(listBillings);

        BillTemplates templates = new BillTemplates(hostelV1.getHostelId(),hostelV1.getMobile(), hostelV1.getEmailId(), hostelV1.getHostelName(), events.getUserId());
        hostelTemplates.initialTemplateSetup(templates);

        Plans trialPlans = plansService.getTrialPlan();

        if (trialPlans != null) {
            Date endingDate = Utils.addDaysToDate(new Date(), trialPlans.getDuration().intValue());
            HostelPlan hostelPlan = new HostelPlan();
            hostelPlan.setCurrentPlanCode(trialPlans.getPlanCode());
            hostelPlan.setCurrentPlanName(trialPlans.getPlanName());
            hostelPlan.setCurrentPlanStartsAt(new Date());
            hostelPlan.setCurrentPlanEndsAt(endingDate);
            hostelPlan.setCurrentPlanPrice(trialPlans.getPrice());
            hostelPlan.setTrial(true);
            hostelPlan.setTrialEndingAt(new Date());
            hostelPlan.setHostel(hostelV1);

            hostelV1.setHostelPlan(hostelPlan);
        }

        planHistoryService.addHostel(hostelV1.getHostelId(), new Date());

        hostelService.updateHostelFromEvents(hostelV1);

        List<String> users = userHostelService.listAllUsersFromHostelId(events.getHostelId());

        List<Users> listUsers =  usersService.findAllUsersFromUserId(users);

        List<BankingV1> listBankings = listUsers.stream()
                .map(item -> {
                    StringBuilder fullName = new StringBuilder();
                    if (item.getFirstName() != null) {
                        fullName.append(item.getFirstName());
                    }
                    if (item.getLastName() != null && !item.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(item.getLastName());
                    }
                    BankingV1 bankingV1 = new BankingV1();
                    bankingV1.setParentId(events.getParentId());
                    bankingV1.setAccountType(BankAccountType.CASH.name());
                    bankingV1.setTransactionType(BankPurpose.BOTH.name());
                    bankingV1.setCreatedAt(new Date());
                    bankingV1.setUserId(item.getUserId());
                    bankingV1.setHostelId(hostelId);
                    bankingV1.setActive(true);
                    bankingV1.setDeleted(false);
                    bankingV1.setCreatedBy(events.getUserId());
                    bankingV1.setAccountHolderName(fullName.toString());
                    return bankingV1;
                })
                .toList();
        bankingService.saveAllBankInfo(listBankings);


    }
}
