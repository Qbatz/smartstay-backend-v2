package com.smartstay.smartstay.payloads.drafts;

import com.smartstay.smartstay.payloads.customer.NonRefundable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UpdateDrafts(
        @NotNull(message = "Operation type is required")
        @NotEmpty(message = "Operation type is required")
        @Pattern(regexp = "BOOKING|booking|Booking|CHECK_IN|check_in|Check_In", message = "source must be either 'BOOKING' or 'booking' or 'CHECK_IN' or 'check_in'")
        String operationType,
        @Pattern(regexp = "SHORT|short|Short|LONG|long|Long", message = "source must be either 'SHORT' or 'short' or 'LONG' or 'long'")
                           String stayType,
                           String bookingDate,
                           Double bookingAmount,
                           String joiningDate,
                           Integer floorId,
                           Integer roomId,
                           Integer bedId,
                           Double rent,
                           String bankId,
                           Double refundableAdvance,
                           Boolean shouldCollectFullRent,
                           Double customRent,
                           String transactionId,
                           @Valid
                           List<NonRefundable> advanceDeductions,
                           @Valid
                           List<NonRefundable> oneTimeDeductions) {
}
