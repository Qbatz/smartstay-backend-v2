package com.smartstay.smartstay.responses.assets;

public record Vendors(Integer vendorId,
                      String vendorCode,
                      String businessName,
                      String vendorFullName,
                      String vendorFirstName,
                      String vendorLastName,
                      String vendorInitialize) {
}
