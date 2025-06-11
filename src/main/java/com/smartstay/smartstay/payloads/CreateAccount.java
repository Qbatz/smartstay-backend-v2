package com.smartstay.smartstay.payloads;

import jakarta.annotation.Nonnull;

public record CreateAccount(@Nonnull String firstName, String lastName, @Nonnull String mailId, @Nonnull String password, @Nonnull String confirmPassword, @Nonnull String mobile) {

}
