package com.smartstay.smartstay.Wrappers.booking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.responses.bookings.AdvanceInfo;
import com.smartstay.smartstay.responses.bookings.CustomerInfo;
import com.smartstay.smartstay.responses.bookings.InitializeInvoiceItems;
import com.smartstay.smartstay.responses.bookings.InitializeRedemption;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.function.Function;

public class InitializeRedemptionMapper implements Function<InvoicesV1, InitializeInvoiceItems> {

    private List<TransactionV1> listTransactions = null;
    private List<BankingV1> listBanks = null;

    public InitializeRedemptionMapper(List<TransactionV1> listTransactions, List<BankingV1> listBanks) {
        this.listTransactions = listTransactions;
        this.listBanks = listBanks;
    }

    @Override
    public InitializeInvoiceItems apply(InvoicesV1 invoicesV1) {
        double pendingAmount = 0;
        String paymentMode = null;
        String latestPaidDate = null;
        Double latestPaidAmount = null;
        if (invoicesV1.getPaidAmount() != null) {
            pendingAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();
        }
        else {
            pendingAmount = invoicesV1.getTotalAmount();
        }

        if (listTransactions != null) {
            TransactionV1 transactionV1 = listTransactions
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(invoicesV1.getInvoiceId()))
                    .findFirst()
                    .orElse(null);
            if (transactionV1 != null) {
                if (transactionV1.getPaidAmount() != null) {
                    latestPaidAmount = Utils.roundOffWithTwoDigit(transactionV1.getPaidAmount());
                    latestPaidDate = Utils.dateToString(transactionV1.getPaymentDate());
                }

                if (listBanks != null) {
                    BankingV1 bankingV1 = listBanks
                            .stream()
                            .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                            .findFirst()
                            .orElse(null);
                    if (bankingV1 != null) {
                        paymentMode = bankingV1.getAccountType();
                    }
                }
            }
        }

        return new InitializeInvoiceItems(invoicesV1.getInvoiceType(),
                invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                invoicesV1.getTotalAmount(),
                pendingAmount,
                invoicesV1.getInvoiceId(),
                paymentMode,
                latestPaidAmount,
                latestPaidDate);
    }
}
