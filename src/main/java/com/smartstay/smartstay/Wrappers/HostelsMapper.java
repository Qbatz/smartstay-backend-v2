package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.HostelImages;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.util.Utils;
import jdk.jshell.execution.Util;

import java.util.function.Function;
import java.util.stream.Collectors;

public class HostelsMapper implements Function<HostelV1, Hostels> {

    @Override
    public Hostels apply(HostelV1 hostelV1) {
        return new Hostels(hostelV1.getHostelId(),
                hostelV1.getMainImage(),
                hostelV1.getCity(),
                String.valueOf(hostelV1.getCountry()),
                hostelV1.getEmailId(),
                hostelV1.getHostelName(),
                hostelV1.getHouseNo(),
                hostelV1.getLandmark(),
                hostelV1.getMobile(),
                hostelV1.getPincode(),
                hostelV1.getState(),
                hostelV1.getStreet(),
                Utils.dateToString(hostelV1.getUpdatedAt()),
                Utils.dateToString(hostelV1.getSubscription().getNextBillingAt()),
                Utils.compareWithTodayDate(hostelV1.getSubscription().getNextBillingAt()),
                "");
    }
}
