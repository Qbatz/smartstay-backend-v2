package com.smartstay.smartstay.ennum;

public enum RequestStatus {
    PENDING("Pending"),
    OPEN("Open"),
    ONHOLD("Hold"),
    REJECTED("Rejected"),
    CLOSED("Closed"),
    INPROGRESS("In-Progress");


    RequestStatus(String pending) {
    }
}
