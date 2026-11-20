package com.smartstay.smartstay.services;


import com.smartstay.smartstay.Exceptions.BadRequestException;
import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.BillingModel;
import com.smartstay.smartstay.ennum.BillingType;
import com.smartstay.smartstay.ennum.PeriodEnum;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.util.DateUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HostelConfigService {

    @Autowired
    private BillingRuleRepository billingRuleRepository;

    Optional<BillingRules> getBillingRuleByIdAndHostelId(Integer id, String hostelId) {
        return billingRuleRepository.findBillingRuleByIdAndHostelId(id, hostelId);
    }

    Optional<BillingRules> getBillingRuleByHostelId(String hostelId) {
        return billingRuleRepository.findByHostel_hostelId(hostelId);
    }



    public void saveBillingRule(BillingRules billingRule) {
        billingRuleRepository.save(billingRule);
    }

    public BillingRules getCurrentMonthTemplate(String hostelId) {
        return billingRuleRepository.findCurrentBillingRules(hostelId);
    }

    public BillingDates getBillingRuleByDateAndHostelId(String hostelId, Date dateJoiningDate) {
        BillingRules billingRules = billingRuleRepository.findCurrentBillingRules(hostelId);
        BillingDates billDates = null;

        int billStartDate = 1;
        int billingRuleDueDate = 10;
        int billMonth;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateJoiningDate);

        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();

            if (billingRules.getTypeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                calendar.set(Calendar.DAY_OF_MONTH, billStartDate);
                Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

                return new BillingDates(calendar.getTime(),
                        findEndDate,
                        null,
                        billingRules.getBillDueDays(),
                        billingRules.isHasGracePeriod(),
                        billingRules.getGracePeriodDays(),
                        billingRules.getTypeOfBilling(),
                        billingRules.getBillingModel());
            }
        }

        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);
        if (Utils.compareWithTwoDates(dateJoiningDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        if (billingRules != null) {
            billDates = new BillingDates(calendar.getTime(),
                    findEndDate,
                    dueDate,
                    billingRuleDueDate,
                    billingRules.isHasGracePeriod(),
                    billingRules.getGracePeriodDays(),
                    billingRules.getTypeOfBilling(),
                    billingRules.getBillingModel());
        }
        return billDates;
    }

    public List<BillingRules> findAllHostelsHavingBillingToday() {
        Date date = new Date();
        String day = Utils.getDayFromDate(date);

        return billingRuleRepository.findAllHostelsHavingTodaysRecurring(day);
    }

    public BillingDates getNextMonthBillingDates(String hostelId) {

        BillingRules billingRules = billingRuleRepository.findCurrentBillingRules(hostelId);
        BillingDates billingDates = null;
        if (billingRules != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, billingRules.getBillingStartDate());

            if (Utils.compareWithTwoDates(cal.getTime(), new Date()) < 0) {
                cal.add(Calendar.MONTH, 1);
            }

            Date findEndDate = Utils.findLastDate(billingRules.getBillingStartDate(), cal.getTime());
            Date dueDate = Utils.addDaysToDate(cal.getTime(), billingRules.getBillDueDays());

            billingDates = new BillingDates(cal.getTime(),
                    findEndDate,
                    dueDate,
                    billingRules.getBillDueDays(),
                    billingRules.isHasGracePeriod(),
                    billingRules.getGracePeriodDays(),
                    billingRules.getTypeOfBilling(),
                    billingRules.getBillingModel());
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            Date findEndDate = Utils.findLastDate(1, cal.getTime());
            Date dueDate = Utils.addDaysToDate(cal.getTime(), 10);

            billingDates = new BillingDates(cal.getTime(),
                    findEndDate,
                    dueDate,
                    10,
                    false,
                    0,
                    BillingType.FIXED_DATE.name(),
                    BillingModel.PREPAID.name());
        }


        return billingDates;
    }

    public BillingRules getCurrentBillingRule(String hostelId) {
        return billingRuleRepository.findCurrentBillingRules(hostelId);
    }

    public List<HostelV1> findAHostelsHavingBillingRuleEndingToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        int billingDay = Utils.findDateFromDate(calendar.getTime());
        List<BillingRules> listBillingDates = billingRuleRepository.findAllHostelsHavingTodaysRecurring(billingDay);
        List<HostelV1> listHostels = null;
        if (listBillingDates != null) {
            listHostels = listBillingDates
                    .stream()
                    .map(BillingRules::getHostel)
                    .toList();
        }
        return listHostels;
    }

    /**
     * this will triggered for postpaid services
     *
     * @return
     */
    public List<BillingRules> findHostelsHavingBillingStartDateToday() {

        int billingDay = Utils.findDateFromDate(new Date());

        List<BillingRules> listBillingDates = billingRuleRepository.findPostpaidHostelsHavingBillingRuleToday(billingDay);
        if (listBillingDates == null) {
            listBillingDates = new ArrayList<>();
        }
        return listBillingDates;
    }

    public List<BillingRules> findHostelsHavingJoiningBsedInvoice() {
        List<BillingRules> listJoiningBasedHostels = billingRuleRepository.findHostelsHavingJoiningDateBasedBillings();
        if (listJoiningBasedHostels == null) {
            listJoiningBasedHostels = new ArrayList<>();
        }
        return listJoiningBasedHostels;
    }

    public BillingDates computeBillingDates(BillingRules billingRules, Date requestedDate) {

        if (requestedDate == null){
            throw new BadRequestException("Date is null");
        }

        requestedDate = DateUtils.getStartOfDay(requestedDate);

        int billStartDate = billingRules != null ? billingRules.getBillingStartDate() : 1;
        int billingRuleDueDate = billingRules != null ? billingRules.getBillDueDays() : 10;

        boolean hasGracePeriod = false;
        int gracePeriodDays = 0;
        String typeOfBilling = null;
        String billingModel = null;
        if (billingRules != null){
            hasGracePeriod = billingRules.isHasGracePeriod();
            gracePeriodDays = billingRules.getGracePeriodDays() != null ? billingRules.getGracePeriodDays() : 0;
            typeOfBilling = billingRules.getTypeOfBilling();
            billingModel = billingRules.getBillingModel();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedDate);

        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        if (DateUtils.compareWithTwoDates(requestedDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = DateUtils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);
        Date endDate = DateUtils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(calendar.getTime(), endDate, dueDate, billingRuleDueDate,
                hasGracePeriod, gracePeriodDays, typeOfBilling, billingModel);
    }

    public BillingDates getBillingDatesByPeriod(String period, BillingRules billingRules) {

        PeriodEnum periodEnum;
        try {
            periodEnum = PeriodEnum.valueOf(period);
        } catch (Exception e) {
            throw new BadRequestException("Invalid period");
        }

        if (billingRules == null) {
            throw new BadRequestException("Billing rules not found");
        }

        Date today = DateUtils.getStartOfDay(new Date());

        BillingDates currentBillingDates = computeBillingDates(billingRules, today);

        int monthsToSubtract = switch (periodEnum) {
            case THIS_MONTH -> 0;
            case LAST_MONTH -> 1;
            case LAST_3_MONTHS -> 2;
            case LAST_6_MONTHS -> 5;
            default -> 0;
        };

        if (monthsToSubtract == 0) {
            return currentBillingDates;
        }

        Date pastDate = DateUtils.getLastMonthDateByNoOfMonths(today, monthsToSubtract);

        BillingDates pastBillingDates = computeBillingDates(billingRules, pastDate);

        return new BillingDates(
                pastBillingDates.currentBillStartDate(),
                currentBillingDates.currentBillEndDate(),
                currentBillingDates.dueDate(),
                currentBillingDates.dueDays(),
                currentBillingDates.hasGracePeriod(),
                currentBillingDates.gracePeriodDays(),
                currentBillingDates.typeOfBilling(),
                currentBillingDates.billingModel()
        );
    }
}
