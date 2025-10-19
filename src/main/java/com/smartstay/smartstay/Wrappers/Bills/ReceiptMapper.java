package com.smartstay.smartstay.Wrappers.Bills;

import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.responses.invoices.ReceiptsList;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;


public class ReceiptMapper implements Function<Receipts, ReceiptsList> {
    @Override
    public ReceiptsList apply(Receipts receipts) {
        StringBuilder initials = new StringBuilder();
        StringBuilder fullName = new StringBuilder();
        StringBuilder invoiceType = new StringBuilder();
        StringBuilder invoiceMode = new StringBuilder();
        StringBuilder bankName = new StringBuilder();

        if (receipts.getFirstName() != null && !receipts.getFirstName().equalsIgnoreCase("")){
            initials.append( receipts.getFirstName().toUpperCase().charAt(0));
            fullName.append(receipts.getFirstName());
        }

        String lastName = receipts.getLastName();
        if (receipts.getLastName() != null && !receipts.getLastName().equalsIgnoreCase("")) {
            initials.append(receipts.getLastName().toUpperCase().charAt(0));
            fullName.append(" ");
            fullName.append(receipts.getLastName());
        }
        else {
            if (receipts.getFirstName() !=null && receipts.getFirstName().length() > 1) {
                initials.append(receipts.getFirstName().toUpperCase().charAt(1));
            }

        }
        if (receipts.getHolderName() != null) {
            bankName.append(receipts.getHolderName());
        }
        if (receipts.getBankName() != null) {
            if (receipts.getHolderName() != null) {
                bankName.append("-");
            }
            bankName.append(receipts.getBankName());
        }
        if (receipts.getBankName() == null) {
            if (receipts.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                if (receipts.getHolderName() != null) {
                    bankName.append("-");
                }
                bankName.append(receipts.getAccountType());
            }
        }

        String type = switch (receipts.getInvoiceType()) {
            case "ADVANCE" -> "Advance";
            case "BOOKING" -> "Booking";
            case "RENT" -> "Rent";
            default -> "Others";
        };
        invoiceType.append(type);

        String mode = switch (receipts.getInvoiceMode()) {
            case "MANUAL" -> "Manual";
            case "AUTOMATIC" -> "Automatic";
            case "RECURRING" -> "Recurring";
            default ->  "System Generated";
        };
        invoiceMode.append(mode);


        return new ReceiptsList(receipts.getFirstName(),
                receipts.getLastName(),
                fullName.toString(),
                receipts.getTransactionId(),
                receipts.getReferenceNumber(),
                receipts.getInvoiceNumber(),
                "PAID",
                Utils.dateToString(receipts.getPaidAt()),
                receipts.getPaidAmount(),
                invoiceType.toString(),
                invoiceMode.toString(),
                receipts.getInvoiceId(),
                receipts.getCustomerId(),
                bankName.toString(),
                receipts.getBankId(),
                receipts.getProfilePic(),
                initials.toString());
    }
}
