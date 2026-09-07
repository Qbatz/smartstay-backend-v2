package com.smartstay.smartstay.dto.kyc;

public record KycRequestMessage(boolean canRequest, Integer count, String errorMessage) {
}
