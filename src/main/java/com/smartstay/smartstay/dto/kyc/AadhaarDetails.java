package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AadhaarDetails {
    @JsonProperty("id_number")
    private String idNumber;
    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("id_proof_type")
    private String idProofType;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("image")
    private String image;
    @JsonProperty("name")
    private String name;
    @JsonProperty("last_refresh_date")
    private String lastRefreshedDate;
    @JsonProperty("dob")
    private String dateOfBirth;
    @JsonProperty("current_address")
    private String currentAddress;
    @JsonProperty("permanent_address")
    private String permanentAddressString;
    @JsonProperty("permanent_address_details")
    private KycAddressDetails permanentAddress;
    @JsonProperty("current_address_details")
    private KycAddressDetails currentAddressDetails;
    @JsonProperty("completed_at")
    private String completedAt;

}
