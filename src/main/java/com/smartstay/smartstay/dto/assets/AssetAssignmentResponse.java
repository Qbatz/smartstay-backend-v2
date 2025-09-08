package com.smartstay.smartstay.dto.assets;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetAssignmentResponse {

    private Integer assetId;
    private String assetName;
    private String brandName;
    private String productName;
    private String serialNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date purchaseDate;
    private Double price;
    private String hostelName;
    private String assignmentStatus;
}
