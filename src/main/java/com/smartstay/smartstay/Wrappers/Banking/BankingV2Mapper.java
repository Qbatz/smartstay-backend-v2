package com.smartstay.smartstay.Wrappers.banking;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BankingV2Mapper implements Function<BankingV2, BankV2Response> {

    @Override
    public BankV2Response apply(BankingV2 bank) {
        return new BankV2Response(
                bank.getBankId(),
                bank.getDisplayName(),
                bank.getBankName(),
                bank.getAccountNumber(),
                bank.getIfscCode(),
                bank.getBranchName(),
                bank.getAccountHolderName(),
                bank.getAccountType(),
                bank.getBankAccountType(),
                bank.getDescription(),
                bank.getBalance(),
                bank.isActive(),
                bank.isDefaultAccount(),
                bank.getHostelId(),
                bank.getPlatform(),
                bank.getCreatedBy(),
                bank.getCreatedAt() != null ? Utils.dateToString(bank.getCreatedAt()) : null);
    }
}
