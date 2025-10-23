package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.dto.expenses.ExpenseList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpensesRepository extends JpaRepository<ExpensesV1, String> {
    ExpensesV1 findByExpenseNumberAndHostelId(String expenseNumber, String hostelId);

    @Query(value = """
            SELECT exp.expense_id as expenseId, exp.unit_count as noOfItems, exp.category_id as categoryId, 
            exp.description, exp.hostel_id as hostelId, exp.bank_id as bankId, exp.sub_category_id as subCategoryId, 
            exp.total_price as totalAmount, exp.transaction_amount as transactionAmount, exp.transaction_date as transactionDate, 
            exp.unit_price as unitPrice, exp.vendor_id as vendorId, exp.expense_number as referenceNumber, 
            banking.account_holder_name as holderName, banking.account_type as accountType, banking.bank_name as bankName, expCat.category_name as categoryName, 
            expSub.sub_category_name as subCategoryName  FROM expensesv1 exp LEFT OUTER JOIN 
            bankingv1 banking on banking.bank_id=exp.bank_id LEFT OUTER JOIN expense_category expCat on expCat.category_id=exp.category_id 
            LEFT OUTER JOIN expense_sub_category expSub on expSub.sub_category_id=exp.sub_category_id WHERE exp.hostel_id=:hostelId and exp.is_active=true;
            """, nativeQuery = true)
    List<ExpenseList> findAllExpensesByHostelId(@Param("hostelId") String hostelId);
}
