package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.responses.expenses.ExpenseHeaderAdditionalFields;
import com.smartstay.smartstay.util.BankingUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.ExpenseColumnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExpensesListWebMapper implements Function<ExpensesV1, List<Object>> {
    private List<String> columns = null;
    private List<VendorV1> vendorList = null;
    private List<BankingV2> listBanks = null;
    private List<ExpenseCategory> listExpenseCategory;

    public ExpensesListWebMapper(List<String> columns, List<VendorV1> vendorList, List<BankingV2> listBanks, List<ExpenseCategory> listExpenseCategory) {
        this.columns = columns;
        this.vendorList = vendorList;
        this.listBanks = listBanks;
        this.listExpenseCategory = listExpenseCategory;
    }

    @Override
    public List<Object> apply(ExpensesV1 expensesV1) {
        List<Object> columnItems = new ArrayList<>();
        columns.forEach(column -> columnItems.add(getColumnItem(expensesV1, column)));
        String status = ExpensePaymentStatus.fromString(expensesV1.getPaymentStatus()).name();
        columnItems.add(new ExpenseHeaderAdditionalFields(expensesV1.getExpenseId(), status));
        return columnItems;
    }

    private String getColumnItem(ExpensesV1 expensesV1, String column) {
        if (column.equalsIgnoreCase(ExpenseColumnUtils.EXPENSE_NO)) {
            return expensesV1.getExpenseNumber();
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.TITLE)) {
            return expensesV1.getTitle();
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.DATE)) {
            if (expensesV1.getTransactionDate() != null) {
                return Utils.dateToString(expensesV1.getTransactionDate());
            }
            return null;
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.CATEGORY)) {
            if (listExpenseCategory != null) {
                ExpenseCategory ec = listExpenseCategory
                        .stream()
                        .filter(i -> i.getCategoryId().equals(expensesV1.getCategoryId()))
                        .findFirst()
                        .orElse(null);
                if (ec != null) {
                    return ec.getCategoryName();
                }
            }
            return "NA";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.SUB_CATEGORY)) {
            if (listExpenseCategory != null) {
                ExpenseCategory ec = listExpenseCategory
                        .stream()
                        .filter(i -> i.getCategoryId().equals(expensesV1.getCategoryId()))
                        .findFirst()
                        .orElse(null);
                if (ec != null) {
                    if (ec.getListSubCategories() != null) {
                        ExpenseSubCategory expenseSubCategory = ec.getListSubCategories()
                                .stream()
                                .filter(i -> i.getSubCategoryId().equals(expensesV1.getSubCategoryId()))
                                .findFirst()
                                .orElse(null);
                        if (expenseSubCategory != null) {
                            return expenseSubCategory.getSubCategoryName();
                        }
                    }
                }
            }
            return "NA";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.VENDOR)) {
            if (vendorList != null) {
                VendorV1 vendor = vendorList
                        .stream()
                        .filter(i -> i.getVendorId().equals(expensesV1.getVendorId()))
                        .findFirst()
                        .orElse(null);
                if (vendor != null) {
                    if (vendor.getBusinessName() != null) {
                        return vendor.getBusinessName();
                    }
                    return NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName());
                }
            }
            return "NA";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.STATUS)) {
            return ExpensePaymentStatus.fromString(expensesV1.getPaymentStatus()).name();
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.PAYMENT_MODE)) {
            if (listBanks != null) {
                BankingV2 bankingV2 = listBanks
                        .stream()
                        .filter(i -> i.getBankId().equalsIgnoreCase(expensesV1.getBankId()))
                        .findFirst()
                        .orElse(null);
                if (bankingV2 != null) {
                    if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                        return bankingV2.getCashAccountType() + "-" + bankingV2.getDisplayName();
                    }
                    if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                        return bankingV2.getAccountType() + "-" + bankingV2.getDisplayName();
                    }
                    if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                        BankingMethods bm = bankingV2
                                .getBankingMethods()
                                .stream()
                                .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(expensesV1.getPaymentMethodId()))
                                .findFirst()
                                .orElse(null);
                        if (bm != null) {
                            String paymentType = BankingUtils.getPaymentMethodName(bm);
                            return bm.getPaymentMethod() + "-" + paymentType;
                        }
                    }
                }
            }
            return "NA";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.TOTAL_AMOUNT)) {
            return String.valueOf(Utils.roundOffWithTwoDigit(expensesV1.getTransactionAmount()));
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.PAID_AMOUNT)) {
            if (expensesV1.getPaidAmount() != null) {
                return String.valueOf(Utils.roundOffWithTwoDigit(expensesV1.getPaidAmount()));
            }
            return "-";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.BALANCE_AMOUNT)) {
            if (expensesV1.getBalanceAmount() != null) {
                return String.valueOf(Utils.roundOffWithTwoDigit(expensesV1.getBalanceAmount()));
            }
            return "-";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.ACTUAL_TOTAL)) {
            // Original total before discount; older rows without it fall back to the payable total.
            return String.valueOf(Utils.roundOffWithTwoDigit(expensesV1.getTransactionAmount()));
        }
        return "NA";
    }
}
