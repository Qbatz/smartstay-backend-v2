package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.responses.banking.BankV2Response;
import com.smartstay.smartstay.util.NameUtils;

import java.util.List;
import java.util.function.Function;

public class BankV2Mapper implements Function<BankingV2, BankV2Response> {

    private List<Users> listUsers = null;

    public BankV2Mapper(List<Users> listUsers) {
        this.listUsers = listUsers;
    }

    @Override
    public BankV2Response apply(BankingV2 bankingV2) {
        String responsiblePerson = null;
        if (listUsers != null) {
            if (bankingV2.getResponsiblePerson() != null) {
                Users users = listUsers
                        .stream()
                        .filter(i -> i.getUserId().equalsIgnoreCase(bankingV2.getResponsiblePerson()))
                        .findFirst()
                        .orElse(null);
                if (users != null) {
                    responsiblePerson = NameUtils.getFullName(users.getFirstName(), users.getLastName());
                }
            }
        }

        return new BankV2Response(bankingV2.getBankId(),
                bankingV2.getDisplayName(),
                bankingV2.getBankName(),
                bankingV2.getAccountNumber(),
                bankingV2.getAccountHolderName(),
                bankingV2.getAccountType(),
                bankingV2.getBankAccountType(),
                bankingV2.getCashAccountType(),
                responsiblePerson,
                bankingV2.getBalance(),
                bankingV2.isActive(),
                bankingV2.isDefaultAccount());
    }
}
