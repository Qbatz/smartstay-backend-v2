package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.responses.expenseForReport.ExpenseReportResponse;
import com.smartstay.smartstay.util.BankingUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExpenseExpenseDetailsMapper implements Function<ExpensesV1, ExpenseReportResponse.ExpenseDetail> {

    Map<Long, ExpenseCategory> categoryMap = null;
    Map<String, BankingV2> bankMap = null;
    Map<String, Users> userMap = null;
    Map<Integer, VendorV1> vendorMap = null;

    public ExpenseExpenseDetailsMapper(Map<Long, ExpenseCategory> categoryMap, Map<String, BankingV2> bankingMap, Map<String, Users> userMap, List<VendorV1> listVendors) {
        this.categoryMap = categoryMap;
        this.bankMap = bankingMap;
        this.userMap = userMap;
        // index vendors once instead of scanning the list for every expense
        this.vendorMap = listVendors == null ? Map.of() : listVendors
                .stream()
                .filter(v -> v.getVendorId() != null)
                .collect(Collectors.toMap(VendorV1::getVendorId, v -> v, (a, b) -> a));
    }

    @Override
    public ExpenseReportResponse.ExpenseDetail apply(ExpensesV1 e) {
        String vendorName = null;
        ExpenseCategory cat = categoryMap.get(e.getCategoryId());
        String catName = (cat != null) ? cat.getCategoryName() : null;
        String subCatName = null;
        if (cat != null && e.getSubCategoryId() != null && cat.getListSubCategories() != null) {
            subCatName = cat.getListSubCategories().stream()
                    .filter(s -> s.getSubCategoryId().equals(e.getSubCategoryId()))
                    .map(ExpenseSubCategory::getSubCategoryName).findFirst().orElse(null);
        }

        BankingV2 b = bankMap.get(e.getBankId());
        String pMode = BankingUtils.getBankName(b, e.getPaymentMethodId());

        Users u = userMap.get(e.getCreatedBy());
        String creatorName = (u != null)
                ? (u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : ""))
                : null;
        if (Boolean.TRUE.equals(e.getIsVendorExpense()) && e.getVendorId() != null) {
            VendorV1 vendorV1 = vendorMap.get(e.getVendorId());
            if (vendorV1 != null) {
                vendorName = NameUtils.getFullName(vendorV1.getFirstName(), vendorV1.getLastName());
            }
        }

        return ExpenseReportResponse.ExpenseDetail.builder()
                .expenseId(e.getExpenseId())
                .expenseTitle(e.getTitle())
                .expenseNumber(e.getExpenseNumber())
                .date(Utils.dateToString(e.getTransactionDate()))
                .expenseCategory(catName)
                .expenseSubCategory(subCatName)
                .description(e.getDescription())
                .status(resolveStatus(e.getPaymentStatus()))
                .counts(e.getUnitCount() != null ? e.getUnitCount() : 0)
                .assetName(null)
                .vendorName(vendorName)
                .paymentMode(pMode)
                .account(pMode)
                .paidAmount(e.getPaidAmount())
                .balanceAmount(e.getBalanceAmount())
                .amount(e.getTransactionAmount())
                .createdBy(creatorName != null ? creatorName.trim() : null)
                .build();
    }

    private static String resolveStatus(String paymentStatus) {
        if (paymentStatus == null) {
            return ExpensePaymentStatus.Pending.name();
        }
        for (ExpensePaymentStatus status : ExpensePaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(paymentStatus)) {
                return status.name();
            }
        }
        return null;
    }
}
