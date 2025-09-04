package com.smartstay.smartstay.dto.customer;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deductions {
    private String type;
    private Double amount;
}
