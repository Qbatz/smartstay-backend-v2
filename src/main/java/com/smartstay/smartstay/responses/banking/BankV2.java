package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record BankV2(String hostelId,
                     int totalBanks,
                     List<BankV2Response> listBanks) {
}
