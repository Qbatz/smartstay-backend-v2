package com.smartstay.smartstay.dto.expenses;

import java.util.Date;

public interface ExpenseList {
    String getExpenseId();
    Integer getNoOfItems();
    Long getCategoryId();
    Long getSubCategoryId();
    String getDescription();
    String getHostelId();
    String getBankId();
    Double getTotalAmount();
    // Original total before any discount (expensesv1.actual_total_price).
    Double getActualTotalPrice();
    Double getTransactionAmount();
    Date getTransactionDate();
    Double getUnitPrice();
    String getVendorId();
    String getReferenceNumber();
    String getHolderName();
    String getAccountType();
    String getBankName();
    String getCategoryName();
    String getSubCategoryName();
    String getTitle();
    Boolean getIsVendorExpense();
    String getPaymentStatus();
    Double getPaidAmount();
    Double getBalanceAmount();
    String getPaymentMethod();
    String getNote();
    Date getCreatedAt();
    Integer getCreditPeriod();
}
