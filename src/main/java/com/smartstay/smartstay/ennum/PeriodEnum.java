package com.smartstay.smartstay.ennum;

import lombok.Getter;

@Getter
public enum PeriodEnum {

    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months");

    private final String label;

    PeriodEnum(String label) {
        this.label = label;
    }
}
