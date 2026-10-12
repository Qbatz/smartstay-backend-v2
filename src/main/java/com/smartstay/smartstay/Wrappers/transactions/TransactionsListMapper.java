package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.responses.invoices.ReceiptsList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class TransactionsListMapper implements Function<TransactionV1, ReceiptsList> {

    List<Customers> customers = null;
    List<BankingV2> banking = null;
    List<InvoicesV1> invoices = null;

    public TransactionsListMapper(List<Customers> customers, List<BankingV2> banking, List<InvoicesV1> invoices) {
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
        StringBuilder rentalPeriod = new StringBuilder();

        InvoicesV1 invoice = invoices
                .stream()
                .filter(i -> i.getInvoiceId().equalsIgnoreCase(transactionV1.getInvoiceId()))
                .findFirst()
                .orElse(null);

        if (invoice != null) {
            invoiceNumber = invoice.getInvoiceNumber();
            if (invoice.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoice.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
                rentalPeriod.append(Utils.dateToMonth(invoice.getInvoiceStartDate()));
                rentalPeriod.append("-");
                rentalPeriod.append(Utils.dateToMonth(invoice.getInvoiceEndDate()));
            }


            String type = switch (invoice.getInvoiceType()) {
                case "ADVANCE", "ADDITIONAL_ADVANCE" -> "Advance";
                case "BOOKING" -> "Booking";
                case "RENT" -> "Rent";
                case "REASSIGN_RENT" -> "Reassigned Rent";
                case "SETTLEMENT" ->  "Settlement";
                case "REFUND" -> "Refund";
                case "AMOUNT_HOLDING" -> "Advance Holding";
                case "EB_HOLDING" -> "EB Holding";
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
            profilePic = com.smartstay.smartstay.util.CustomerUtils.getProfilePic(cus);
            firstName = cus.getFirstName();
            lastName = cus.getLastName();
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

        BankingV2 bank = banking
                .stream()
                .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                .findFirst()
                .orElse(null);
        if (bank != null) {
            bankName.append(bank.getDisplayName());
            if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                bankName.append("-");
                bankName.append("Cash");
            }
            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {

                if (bank.getAccountHolderName() != null) {

                    bankName.append("-");
                    bankName.append("Credit Card");
                }
            }
            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
//                if (bank.getAccountHolderName() != null) {
//                    bankName.append("-");
//                    if (bank.getBankName() != null) {
//                        bankName.append(bank.getBankName());
//                    }
//                    bankName.append("Bank");
//
//                }

                if (transactionV1.getPaymentMethodId() != null) {
                    BankingMethods paymentMethod = bank
                            .getBankingMethods()
                            .stream()
                            .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(transactionV1.getPaymentMethodId()))
                            .findFirst()
                            .orElse(null);
                    if (paymentMethod != null) {
                        bankName.append("-");
                        bankName.append(paymentMethod.getPaymentMethod());
                    }
                }


            }
//            else if(bank.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
//                if (bank.getAccountHolderName() != null) {
//                    bankName.append("-");
//                    bankName.append("Upi");
//                }
//            }
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
                initials.toString(),
                rentalPeriod.toString());
    }
}
