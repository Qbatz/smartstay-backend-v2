package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycAddressDetails {
    @JsonProperty("address")
    private String address;
    @JsonProperty("locality_or_post_office")
    private String localityOrPostOffice;
    @JsonProperty("district_or_city")
    private String districtOrCity;
    @JsonProperty("state")
    private String state;
    @JsonProperty("pincode")
    private String pincode;
}
