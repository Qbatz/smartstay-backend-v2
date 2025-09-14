package com.smartstay.smartstay.dto.bills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillingRulesDto {
    private Integer id;
    private Integer billingStartDate;
    private Integer billingDueDate;
    private Integer noticePeriod;
}
