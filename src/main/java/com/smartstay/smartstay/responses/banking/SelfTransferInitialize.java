package com.smartstay.smartstay.responses.banking;

import com.smartstay.smartstay.responses.beds.Bank;

import java.util.List;

public record SelfTransferInitialize(Bank fromBank, List<Bank> toBanks) {
}
