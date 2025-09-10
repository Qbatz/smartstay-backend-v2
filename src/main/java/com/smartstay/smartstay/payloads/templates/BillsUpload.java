package com.smartstay.smartstay.payloads.templates;

public record BillsUpload(String mobile,
                          String mailId,
                          Long countryCode) {
}
