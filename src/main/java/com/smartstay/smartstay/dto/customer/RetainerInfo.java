package com.smartstay.smartstay.dto.customer;

import com.smartstay.smartstay.dto.retainer.RetainerSummary;

import java.util.List;

public record RetainerInfo(RetainerSummary summary,
                           List<RetainerListItems> retainerList) {
}
