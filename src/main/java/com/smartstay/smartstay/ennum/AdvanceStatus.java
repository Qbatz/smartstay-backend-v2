package com.smartstay.smartstay.ennum;

public enum AdvanceStatus {
    PAID("PAID"),
    PENDING("PENDING"),
    WAVED_OFF("WAVED"),
    INVOICE_GENERATED("invoiced"),
    DUE("in due");

    AdvanceStatus(String paid) {
    }
}
