package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AadharMediaResponse {
    @JsonProperty("file_in_base64")
    private String file;
    @JsonProperty("file_type")
    private String fileType;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("size_in_bytes")
    private int fileSize;
}
