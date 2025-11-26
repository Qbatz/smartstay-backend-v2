package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.util.Utils;
import org.apache.catalina.User;

import java.util.List;
import java.util.function.Function;

public class TransctionsForCustomerDetails implements Function<TransactionDto, com.smartstay.smartstay.responses.customer.TransactionDto> {

    List<InvoicesV1> listInvoices = null;
    List<BankingV1> listBankings = null;
    List<Users> listUsers = null;

    public TransctionsForCustomerDetails(List<InvoicesV1> listInvoices, List<BankingV1> listBankings, List<Users> listUsers) {
        this.listInvoices = listInvoices;
        this.listBankings = listBankings;
        this.listUsers = listUsers;
    }

    @Override
    public com.smartstay.smartstay.responses.customer.TransactionDto apply(TransactionDto transactionDto) {
        String billName = null;
        String paymentMode = null;
        String paidTo = null;
        StringBuilder rentMonth = new StringBuilder();
        boolean isInvoiceCancelled = false;

        InvoicesV1 invoicesV1 = listInvoices
                .stream()
                .filter(i -> i.getInvoiceId().equalsIgnoreCase(transactionDto.invoiceId()))
                .findFirst()
                .orElse(null);

        BankingV1 bankingV1 = listBankings
                .stream()
                .filter(i -> i.getBankId().equalsIgnoreCase(transactionDto.bankId()))
                .findFirst()
                .orElse(null);

        if (invoicesV1 != null) {
            if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                billName = "Advance";
            }
            else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
                billName = "Settlement";
            }
            else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
                billName = "Rent";
                String invoiceStart = Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate());
                String invoiceEnd = Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate());

                rentMonth.append(invoiceStart);
                rentMonth.append(" - ");
                rentMonth.append(invoiceEnd);
            }
            else if ( invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
                billName = "Bed change - Rent";

                String invoiceStart = Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate());
                String invoiceEnd = Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate());

                rentMonth.append(invoiceStart);
                rentMonth.append(" - ");
                rentMonth.append(invoiceEnd);
            }

            if (invoicesV1.isCancelled()) {
                isInvoiceCancelled = true;
            }
        }

        if (bankingV1 != null) {
            if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                paymentMode = "Cash";
            }
            else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                paymentMode = "Card";
            }
            else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
                paymentMode = "Upi";
            }
            else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                paymentMode = "Bank";
            }

            Users users = listUsers.stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(bankingV1.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (users != null) {
                StringBuilder fullName = new StringBuilder();
                if (users.getFirstName() != null) {
                    fullName.append(users.getFirstName());
                }
                if (users.getLastName() != null) {
                    if (users.getFirstName() != null && !users.getFirstName().equalsIgnoreCase(" ")) {
                        fullName.append(" ");
                    }
                    fullName.append(users.getLastName());
                }

                paidTo = fullName.toString();
            }
        }

        return new com.smartstay.smartstay.responses.customer.TransactionDto(transactionDto.transactionId(),
                transactionDto.referenceNumber(),
                billName,
                transactionDto.transctionDate(),
                transactionDto.transactionAmount(),
                transactionDto.bankId(),
                "Paid",
                paidTo,
                paymentMode,
                rentMonth.toString(),
                isInvoiceCancelled);
    }
}
