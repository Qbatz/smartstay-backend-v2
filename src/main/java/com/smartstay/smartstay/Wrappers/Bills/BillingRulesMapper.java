package com.smartstay.smartstay.Wrappers.Bills;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dto.bills.BillingRulesDto;

public class BillingRulesMapper {

    public static BillingRulesDto toDto(BillingRules billingRules) {
        if (billingRules == null) {
            return null;
        }
        BillingRulesDto dto = new BillingRulesDto();
        dto.setId(billingRules.getId());
        dto.setBillingStartDate(billingRules.getBillingStartDate());
        dto.setBillingDueDate(billingRules.getBillingDueDate());
        dto.setNoticePeriod(billingRules.getNoticePeriod());
        return dto;
    }

    public static BillingRules toEntity(BillingRulesDto dto) {
        if (dto == null) {
            return null;
        }
        BillingRules billingRules = new BillingRules();
        billingRules.setId(dto.getId());
        billingRules.setBillingStartDate(dto.getBillingStartDate());
        billingRules.setBillingDueDate(dto.getBillingDueDate());
        billingRules.setNoticePeriod(dto.getNoticePeriod());
        return billingRules;
    }
}
