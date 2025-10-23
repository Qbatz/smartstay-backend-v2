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
}
