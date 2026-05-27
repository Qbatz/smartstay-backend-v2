package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalltetItems {
    private String type;
    private Double amount;
    private Long walletId;
}
