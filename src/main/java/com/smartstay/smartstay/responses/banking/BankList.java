package com.smartstay.smartstay.responses.banking;

import com.smartstay.smartstay.dto.transaction.TransactionDto;
import com.smartstay.smartstay.responses.beds.Bank;

import java.util.List;

public record BankList(List<Bank> listBanks, List<TransactionDto> listTransactions) {
}
