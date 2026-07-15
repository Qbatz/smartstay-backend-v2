package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dto.expenses.ExpenseList;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.responses.expenses.ExpenseHeaderAdditionalFields;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.ExpenseColumnUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps an {@link ExpenseList} projection into an ordered list of cell values, one per
 * visible/configured column, followed by a trailing {@link ExpenseHeaderAdditionalFields} object
 * carrying the expense id and payment status.
 *
 * <p>Vendor display names are passed in as a pre-built map so the listing resolves them with a
 * single bulk query rather than per-row lookups (no N+1).
 */
public class ExpenseTableMapper implements Function<ExpenseList, List<Object>> {

    private final List<String> columns;
    private final Map<String, String> vendorNamesById;

    public ExpenseTableMapper(List<String> columns, Map<String, String> vendorNamesById) {
        this.columns = columns != null ? columns : List.of();
        this.vendorNamesById = vendorNamesById != null ? vendorNamesById : Collections.emptyMap();
    }

    @Override
    public List<Object> apply(ExpenseList expense) {
        List<Object> columnItems = new ArrayList<>();
        columns.forEach(column -> columnItems.add(getColumnItem(expense, column)));
        String status = ExpensePaymentStatus.fromString(expense.getPaymentStatus()).name();
        columnItems.add(new ExpenseHeaderAdditionalFields(expense.getExpenseId(), status));
        return columnItems;
    }

    private String getColumnItem(ExpenseList expense, String column) {
        if (column.equalsIgnoreCase(ExpenseColumnUtils.EXPENSE_NO)) {
            return notBlank(expense.getReferenceNumber());
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.TITLE)) {
            return notBlank(expense.getTitle());
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.DATE)) {
            return expense.getTransactionDate() != null ? Utils.dateToString(expense.getTransactionDate()) : "NA";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.CATEGORY)) {
            return notBlank(expense.getCategoryName());
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.SUB_CATEGORY)) {
            return notBlank(expense.getSubCategoryName());
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.VENDOR)) {
            if (expense.getVendorId() == null) {
                return "-";
            }
            return vendorNamesById.getOrDefault(expense.getVendorId(), "-");
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.STATUS)) {
            return ExpensePaymentStatus.fromString(expense.getPaymentStatus()).name();
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.PAYMENT_MODE)) {
            return resolvePaymentMode(expense.getAccountType(), expense.getBankName());
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.TOTAL_AMOUNT)) {
            return expense.getTotalAmount() != null ? String.valueOf(expense.getTotalAmount()) : "-";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.PAID_AMOUNT)) {
            return expense.getPaidAmount() != null ? String.valueOf(expense.getPaidAmount()) : "-";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.BALANCE_AMOUNT)) {
            return expense.getBalanceAmount() != null ? String.valueOf(expense.getBalanceAmount()) : "-";
        }
        if (column.equalsIgnoreCase(ExpenseColumnUtils.ACTUAL_TOTAL)) {
            // Original total before discount; older rows without it fall back to the payable total.
            Double actualTotal = expense.getActualTotalPrice() != null
                    ? expense.getActualTotalPrice() : expense.getTotalAmount();
            return actualTotal != null ? String.valueOf(actualTotal) : "-";
        }
        return "NA";
    }

    private String resolvePaymentMode(String accountType, String bankName) {
        if (bankName != null && !bankName.trim().isEmpty()) {
            return bankName;
        }
        if (accountType == null) {
            return "-";
        }
        if (accountType.equalsIgnoreCase(BankAccountType.CASH.name())) {
            return "Cash";
        }
        if (accountType.equalsIgnoreCase(BankAccountType.CARD.name())) {
            return "Card";
        }
        if (accountType.equalsIgnoreCase(BankAccountType.UPI.name())) {
            return "Upi";
        }
        return "-";
    }

    private String notBlank(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }
}
