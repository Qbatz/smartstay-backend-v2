package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.responses.expenses.ExpenseList;
import com.smartstay.smartstay.util.BankingUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class NewExpenseListMapper implements Function<ExpensesV1, ExpenseList> {
    private List<BankingV2> listBanks = null;
    private List<ExpenseCategory> listCategories = null;
    private List<VendorV1> listVendors = null;

    public NewExpenseListMapper(List<BankingV2> listBanks, List<ExpenseCategory> listCategories, List<VendorV1> listVendors) {
        this.listBanks = listBanks;
        this.listCategories = listCategories;
        this.listVendors = listVendors;
    }

    @Override
    public ExpenseList apply(ExpensesV1 expensesV1) {
        String accountHolderName = null;
        String bankName = null;
        String catName = null;
        String subCatName = null;
        String paymentMethod = null;
        Double balanceAmount = 0.0;
        String accountType = null;
        String cashAccountType = null;

        if (listBanks != null) {
            BankingV2 bankingV2 = listBanks
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(expensesV1.getBankId()))
                    .findFirst()
                    .orElse(null);
            if (bankingV2 != null) {
                accountType = bankingV2.getAccountType();
                accountHolderName = bankingV2.getAccountHolderName();
//                bankName = BankingUtils.getBankName(bankingV2, expensesV1.getPaymentMethodId());
                bankName = bankingV2.getBankName();
                cashAccountType = bankingV2.getCashAccountType();

                paymentMethod = BankingUtils.getPaymentMethod(bankingV2, expensesV1.getPaymentMethod());
            }
        }

        if (listCategories != null) {
            ExpenseCategory ec = listCategories
                    .stream()
                    .filter(i -> i.getCategoryId().equals(expensesV1.getCategoryId()))
                    .findFirst()
                    .orElse(null);
            if (ec != null) {
                catName = ec.getCategoryName();
                if (ec.getListSubCategories() != null) {
                    ExpenseSubCategory ecs = ec.getListSubCategories()
                            .stream()
                            .filter(i -> i.getSubCategoryId().equals(expensesV1.getSubCategoryId()))
                            .findFirst()
                            .orElse(null);
                    if (ecs != null) {
                        subCatName = ecs.getSubCategoryName();
                    }
                }
            }
        }

        Double transactionAmount = 0.0;
        Double paidAmount = 0.0;

        if (expensesV1.getTransactionAmount() != null) {
            transactionAmount = Utils.roundOffWithTwoDigit(expensesV1.getTransactionAmount());
        }

        if (expensesV1.getPaidAmount() != null) {
            paidAmount = Utils.roundOffWithTwoDigit(expensesV1.getPaidAmount());
        }
        return new ExpenseList(expensesV1.getExpenseId(),
                expensesV1.getUnitCount(),
                expensesV1.getCategoryId(),
                expensesV1.getSubCategoryId(),
                expensesV1.getDescription(),
                expensesV1.getBankId(),
                transactionAmount,
                Utils.dateToString(expensesV1.getTransactionDate()),
                expensesV1.getUnitPrice(),
                expensesV1.getVendorId(),
                expensesV1.getExpenseNumber(),
                accountHolderName,
                bankName,
                accountType,
                cashAccountType,
                catName,
                subCatName,
                expensesV1.getTitle(),
                expensesV1.getIsVendorExpense(),
                expensesV1.getPaymentStatus(),
                paidAmount,
                Utils.roundOffWithTwoDigit(balanceAmount),
                paymentMethod,
                expensesV1.getNote(),
                transactionAmount,
                paidAmount);
    }
}
