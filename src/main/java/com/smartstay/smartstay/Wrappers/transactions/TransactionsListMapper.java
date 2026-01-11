package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.responses.invoices.ReceiptsList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class TransactionsListMapper implements Function<TransactionV1, ReceiptsList> {

    List<Customers> customers = null;
    List<BankingV1> banking = null;
    List<InvoicesV1> invoices = null;

    public TransactionsListMapper(List<Customers> customers, List<BankingV1> banking, List<InvoicesV1> invoices) {
        this.customers = customers;
        this.banking = banking;
        this.invoices = invoices;
    }

    @Override
    public ReceiptsList apply(TransactionV1 transactionV1) {
        String invoiceNumber = null;
        String firstName = null;
        String lastName = null;
        String profilePic = null;
        String invoiceType = null;
        String invoiceMode = null;
        StringBuilder fullName = new StringBuilder();
        StringBuilder bankName = new StringBuilder();
        StringBuilder initials = new StringBuilder();

        InvoicesV1 invoice = invoices
                .stream()
                .filter(i -> i.getInvoiceId().equalsIgnoreCase(transactionV1.getInvoiceId()))
                .findFirst()
                .orElse(null);

        if (invoice != null) {
            invoiceNumber = invoice.getInvoiceNumber();

            String type = switch (invoice.getInvoiceType()) {
                case "ADVANCE" -> "Advance";
                case "BOOKING" -> "Booking";
                case "RENT" -> "Rent";
                case "REASSIGN_RENT" -> "Reassigned Rent";
                case "SETTLEMENT" ->  "Settlement";
                case "REFUND" -> "Refund";
                default -> "Others";
            };
            invoiceType = type;

            if (invoice.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())) {
                invoiceMode = "Recurring";
            }
            else if (invoice.getInvoiceMode().equalsIgnoreCase(InvoiceMode.MANUAL.name())) {
                invoiceMode = "Manual";
            }
            else if (invoice.getInvoiceMode().equalsIgnoreCase(InvoiceMode.AUTOMATIC.name())) {
                invoiceMode = "Automatic";
            }
        }

        Customers cus = customers
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(transactionV1.getCustomerId()))
                .findFirst()
                .orElse(null);
        if (cus != null) {
            profilePic = cus.getProfilePic();
            firstName = cus.getFirstName();
            lastName = cus.getLastName();
            profilePic = cus.getProfilePic();
            if (cus.getFirstName() != null && !cus.getFirstName().trim().equalsIgnoreCase("")) {
                fullName.append(cus.getFirstName());
                initials.append(cus.getFirstName().toUpperCase().charAt(0));
            }
            if (cus.getLastName() != null && !cus.getLastName().trim().equalsIgnoreCase("")) {
                fullName.append(cus.getLastName());
                initials.append(cus.getLastName().toUpperCase().charAt(0));
            }
            else {
                if (cus.getFirstName() != null && cus.getFirstName().trim().length() > 1) {
                    initials.append(cus.getFirstName().toUpperCase().charAt(1));
                }
            }

        }

        BankingV1 bank = banking
                .stream()
                .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                .findFirst()
                .orElse(null);
        if (bank != null) {
            bankName.append(bank.getAccountHolderName());
            if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                bankName.append("-");
                bankName.append("Cash");
            }
            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                if (bank.getAccountHolderName() != null) {

                    bankName.append("-");
                    bankName.append("Card");
                }
            }
            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                if (bank.getAccountHolderName() != null) {
                    bankName.append("-");
                    bankName.append(bank.getBankName());
                }
            }
            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
                if (bank.getAccountHolderName() != null) {
                    bankName.append("-");
                    bankName.append("Upi");
                }
            }
        }


        return new ReceiptsList(firstName,
                lastName,
                fullName.toString(),
                transactionV1.getTransactionId(),
                transactionV1.getReferenceNumber(),
                transactionV1.getTransactionReferenceId(),
                invoiceNumber,
                "PAID",
                Utils.dateToString(transactionV1.getPaymentDate()),
                transactionV1.getPaidAmount(),
                invoiceType,
                invoiceMode,
                transactionV1.getInvoiceId(),
                transactionV1.getCustomerId(),
                bankName.toString(),
                transactionV1.getBankId(),
                profilePic,
                initials.toString());
    }
}
