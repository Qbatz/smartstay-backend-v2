package com.smartstay.smartstay.util;

import com.smartstay.smartstay.dao.Address;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.KycDetails;
import com.smartstay.smartstay.dto.customer.AddressInfo;

public class CustomerUtils {
    public static String getProfilePic(Customers customers) {
        if (customers != null) {
            if (customers.getProfilePic() != null && !customers.getProfilePic().trim().isEmpty()) {
                return customers.getProfilePic();
            }
            KycDetails kycDetails = customers.getKycDetails();
            if (kycDetails != null) {
                if (kycDetails.getCurrentStatus() != null && kycDetails.getCurrentStatus().equalsIgnoreCase("VERIFIED")) {
                    if (kycDetails.getIdPic() != null && !kycDetails.getIdPic().trim().isEmpty()) {
                        return kycDetails.getIdPic();
                    }
                }
            }
            
            // Fallback to initials
            return null;
        }
        return null;
    }

    public static AddressInfo getCustomerAddress(Customers customers) {
        if (customers != null) {
            return new AddressInfo(customers.getHouseNo(),
                    customers.getLandmark(),
                    customers.getCity(),
                    customers.getState(),
                    customers.getStreet(),
                    customers.getPincode());
        }

        return null;
    }
}
