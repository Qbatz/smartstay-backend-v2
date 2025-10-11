package com.smartstay.smartstay.ennum;

public enum AdvanceStatus {
    PAID("PAID"),
    PENDING("PENDING"),
    WAVED_OFF("WAVED"),
    INVOICE_GENERATED("INVOICED"),
    DUE("DUE");

    AdvanceStatus(String paid) {
    }
}
