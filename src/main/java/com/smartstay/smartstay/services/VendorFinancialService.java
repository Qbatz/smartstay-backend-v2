package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import com.smartstay.smartstay.repositories.ExpensePaymentRepository;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps the denormalized financial summary on {@code vendorv1} ({@code totalExpense},
 * {@code totalPaid}, {@code balance}, {@code paymentStatus}) in sync with the underlying
 * {@code expensesv1} and {@code expense_payments} tables.
 *
 * <p>Rather than applying fragile incremental deltas, the totals are recomputed from the
 * authoritative source rows on each write. This guarantees the vendor row can never drift across
 * create/update/delete operations, which is the intent of "always synchronized". The recompute is
 * two indexed scalar aggregates — cheap on the (less frequent) write path — while the read path
 * (vendor listing) stays free of aggregation.
 */
@Service
public class VendorFinancialService {

    private static final double EPSILON = 0.0001;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private ExpensePaymentRepository expensePaymentRepository;

    @Transactional
    public void recalculate(String vendorId) {
        if (vendorId == null || vendorId.isBlank()) {
            return;
        }
        try {
            recalculate(Integer.parseInt(vendorId.trim()));
        } catch (NumberFormatException ignored) {
            // A non-numeric vendor id cannot map to a vendor row; nothing to synchronize.
        }
    }

    @Transactional
    public void recalculate(Integer vendorId) {
        if (vendorId == null) {
            return;
        }
        VendorV1 vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            return;
        }
        String vendorIdStr = String.valueOf(vendorId);
        double totalExpense = nullSafe(expensesRepository.sumVendorExpense(vendorIdStr, null, null));
        double totalPaid = nullSafe(expensePaymentRepository.sumVendorPaid(vendorIdStr, null, null));
        double balance = totalExpense - totalPaid;

        vendor.setTotalExpense(totalExpense);
        vendor.setTotalPaid(totalPaid);
        vendor.setBalance(balance);
        vendor.setPaymentStatus(deriveStatus(totalExpense, totalPaid, balance));
        vendorRepository.save(vendor);
    }

    private VendorPaymentStatus deriveStatus(double totalExpense, double totalPaid, double balance) {
        if (totalExpense <= EPSILON) {
            return VendorPaymentStatus.NO_TRANSACTION;
        }
        if (Math.abs(balance) <= EPSILON && totalPaid > EPSILON) {
            return VendorPaymentStatus.FULLY_SETTLED;
        }
        if (balance > EPSILON && totalPaid <= EPSILON) {
            return VendorPaymentStatus.NOT_PAID;
        }
        if (balance > EPSILON) {
            return VendorPaymentStatus.PARTIALLY_PAID;
        }
        // balance < 0 (over-paid): treat as fully settled.
        return VendorPaymentStatus.FULLY_SETTLED;
    }

    private double nullSafe(Double value) {
        return value != null ? value : 0.0;
    }
}
