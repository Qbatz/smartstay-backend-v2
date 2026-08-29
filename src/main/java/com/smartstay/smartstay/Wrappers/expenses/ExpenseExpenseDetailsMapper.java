package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.responses.expenseForReport.ExpenseReportResponse;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExpenseExpenseDetailsMapper implements Function<ExpensesV1, ExpenseReportResponse.ExpenseDetail> {

    Map<Long, ExpenseCategory> categoryMap = null;
    Map<String, BankingV1> bankMap = null;
    Map<String, Users> userMap = null;
    List<VendorV1> listVendors = null;

    public ExpenseExpenseDetailsMapper(Map<Long, ExpenseCategory> categoryMap, Map<String, BankingV1> bankingMap, Map<String, Users> userMap, List<VendorV1> listVendors) {
        this.categoryMap = categoryMap;
        this.bankMap = bankingMap;
        this.userMap = userMap;
        this.listVendors = listVendors;
    }

    @Override
    public ExpenseReportResponse.ExpenseDetail apply(ExpensesV1 e) {
        String vendorName = null;
        String status = null;
        ExpenseCategory cat = categoryMap.get(e.getCategoryId());
        String catName = (cat != null) ? cat.getCategoryName() : null;
        String subCatName = null;
        if (cat != null && e.getSubCategoryId() != null && cat.getListSubCategories() != null) {
            subCatName = cat.getListSubCategories().stream()
                    .filter(s -> s.getSubCategoryId().equals(e.getSubCategoryId()))
                    .map(ExpenseSubCategory::getSubCategoryName).findFirst().orElse(null);
        }

        BankingV1 b = bankMap.get(e.getBankId());
        String pMode = (b != null) ? Utils.capitalize(b.getAccountType()) : null;
        String account = (b != null) ? (b.getAccountHolderName() + "-" + Utils.capitalize(b.getAccountType()))
                : null;

        Users u = userMap.get(e.getCreatedBy());
        String creatorName = (u != null)
                ? (u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : ""))
                : null;
        if (e.getIsVendorExpense() != null && e.getIsVendorExpense()) {
            VendorV1 vendorV1 = listVendors
                    .stream()
                    .filter(i -> e.getVendorId().equals(i.getVendorId()))
                    .findFirst()
                    .orElse(null);
            if (vendorV1 != null) {
                vendorName = NameUtils.getFullName(vendorV1.getFirstName(), vendorV1.getLastName());
            }
        }

        if (e.getPaymentStatus().name().equalsIgnoreCase(ExpensePaymentStatus.Full.name())) {
            status = "Full";
        }
        else if (e.getPaymentStatus().name().equalsIgnoreCase(ExpensePaymentStatus.Partial.name())) {
            status = "Partial";
        }
        else if (e.getPaymentStatus().name().equalsIgnoreCase(ExpensePaymentStatus.Pending.name())) {
            status = "Pending";
        }
        else if (e.getPaymentStatus().name().equalsIgnoreCase(ExpensePaymentStatus.Overdue.name())) {
            status = "Overdue";
        }

        return ExpenseReportResponse.ExpenseDetail.builder()
                .expenseId(e.getExpenseId())
                .expenseTitle(e.getTitle())
                .expenseNumber(e.getExpenseNumber())
                .date(Utils.dateToString(e.getTransactionDate()))
                .expenseCategory(catName)
                .expenseSubCategory(subCatName)
                .description(e.getDescription())
                .status(status)
                .counts(e.getUnitCount() != null ? e.getUnitCount() : 0)
                .assetName(null)
                .vendorName(vendorName)
                .paymentMode(pMode)
                .account(account)
                .paidAmount(e.getPaidAmount())
                .balanceAmount(e.getBalanceAmount())
                .amount(e.getTransactionAmount())
                .createdBy(creatorName != null ? creatorName.trim() : null)
                .build();
    }
}
