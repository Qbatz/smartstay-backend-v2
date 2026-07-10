package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CheckInBookedCustomerNew(Integer floorId,
                                       Integer bedId,
                                       Integer roomId,
                                       String joiningDate,
                                       Double refundableAmount,
                                       @NotNull(message = "Rental amount required")
                                    Double rentalAmount,

                                       @NotNull(message = "Stay type required")
                                    @NotEmpty(message = "Stay type required")
                                    @NotBlank(message = "Stay type is required")
                                    @Pattern(regexp = "long|short|SHORT|LONG", message = "Type must be either 'long' or 'short'")
                                    String stayType,

                                       @Valid
                                    List<NonRefundable> deductions,

                                       Boolean shouldCollectFullRent,
                                       Double customRent,
                                       @Valid
                                    List<NonRefundable> oneTimeDeduction) {
}
