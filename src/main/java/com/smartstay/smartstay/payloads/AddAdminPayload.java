package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddAdminPayload(@NotNull(message = "Firstname is required")@NotEmpty(message = "Firstname is required") String firstName, String lastName, @NotEmpty(message = "mobile no is required") @NotNull(message = "mobile no is required") String mobile, @NotEmpty(message = "mailid is required") @NotNull(message = "mailid is required") String mailId, @NotNull(message = "password is required") @NotEmpty(message = "password is required") String password, String houseNo, String street, String landmark, @NotEmpty(message = "city is required") @NotNull(message = "city is required")String city, @NotNull(message = "pincode is required")Integer pincode, @NotEmpty(message = "state is required") @NotNull(message = "state is required") String state) {

}
