package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class TransctionsForCustomerDetails implements Function<TransactionDto, com.smartstay.smartstay.responses.customer.TransactionDto> {

    List<InvoicesV1> listInvoices = null;
    List<BankingV2> listBankings = null;
    List<Users> listUsers = null;

    public TransctionsForCustomerDetails(List<InvoicesV1> listInvoices, List<BankingV2> listBankings, List<Users> listUsers) {
        this.listInvoices = listInvoices;
        this.listBankings = listBankings;
        this.listUsers = listUsers;
    }

    @Override
    public com.smartstay.smartstay.responses.customer.TransactionDto apply(TransactionDto transactionDto) {
        String billName = null;
        String paymentMode = null;
        String bankName = null;
        String accountHolderName = null;
        String paidTo = null;
        StringBuilder rentMonth = new StringBuilder();
        boolean isInvoiceCancelled = false;

        InvoicesV1 invoicesV1 = null;
        if (listInvoices != null) {
            invoicesV1 = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(transactionDto.invoiceId()))
                    .findFirst()
                    .orElse(null);
        }

        BankingV2 bankingV2 = null;
        if (listBankings != null) {
            bankingV2 = listBankings
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(transactionDto.bankId()))
                    .findFirst()
                    .orElse(null);
        }

        if (invoicesV1 != null) {
            String invoiceType = invoicesV1.getInvoiceType() != null
                    ? invoicesV1.getInvoiceType().trim().toUpperCase()
                    : "";

            billName = switch (invoiceType) {
                case "ADVANCE" -> "Advance";
                case "ADDITIONAL_ADVANCE" -> "Additional Advance";
                case "SETTLEMENT" -> "Settlement";
                case "RENT" -> "Rent";
                case "REASSIGN_RENT" -> "Bed change - Rent";
                case "BOOKING" -> "Booking";
                case "AMOUNT_HOLDING" -> "Advance Holding";
                case "EB_HOLDING" -> "EB Holding";
                case "EB" -> "EB";
                default -> "Other";
            };

            if (invoiceType.equals("RENT") || invoiceType.equals("REASSIGN_RENT")) {
                rentMonth.append(Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate()));
                rentMonth.append(" - ");
                rentMonth.append(Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate()));
            }

            if (invoicesV1.isCancelled()) {
                isInvoiceCancelled = true;
            }
        }

        if (bankingV2 != null) {
            if (bankingV2.getAccountType() != null) {
                if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                    paymentMode = "Cash";
                }
                else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                    paymentMode = "Card";
                }
                else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                    paymentMode = "Bank";
                }
            }

            bankName = isPresent(bankingV2.getBankName()) ? bankingV2.getBankName() : bankingV2.getDisplayName();
            accountHolderName = bankingV2.getAccountHolderName();
        }

        if (listUsers != null && transactionDto.collectedBy() != null) {
            Users collectedBy = listUsers
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(transactionDto.collectedBy()))
                    .findFirst()
                    .orElse(null);
            if (collectedBy != null) {
                paidTo = NameUtils.getFullName(collectedBy.getFirstName(), collectedBy.getLastName());
            }
        }

        return new com.smartstay.smartstay.responses.customer.TransactionDto(transactionDto.transactionId(),
                transactionDto.referenceNumber(),
                transactionDto.transactionReferenceId(),
                billName,
                transactionDto.transctionDate(),
                transactionDto.transactionAmount(),
                transactionDto.bankId(),
                "Paid",
                paidTo,
                paymentMode,
                bankName,
                accountHolderName,
                rentMonth.toString(),
                isInvoiceCancelled);
    }

    private boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
