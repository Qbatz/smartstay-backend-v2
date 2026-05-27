package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentOtherItems {
    private String otherItem;
    private Double amount;
}
