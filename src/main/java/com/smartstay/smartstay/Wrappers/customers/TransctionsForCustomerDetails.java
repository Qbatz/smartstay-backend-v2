package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dto.customer.TransactionDto;

import java.util.function.Function;

public class TransctionsForCustomerDetails implements Function<TransactionDto, com.smartstay.smartstay.responses.customer.TransactionDto> {
    @Override
    public com.smartstay.smartstay.responses.customer.TransactionDto apply(TransactionDto transactionDto) {
        return new com.smartstay.smartstay.responses.customer.TransactionDto(transactionDto.transactionId(),
                transactionDto.referenceNumber(),
                "",
                transactionDto.transctionDate(),
                transactionDto.transactionAmount(),
                transactionDto.bankId(),
                "Paid",
                "",
                "Cash");
    }
}
