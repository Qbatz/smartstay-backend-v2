package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.transactions.TransactionsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.ennum.BankSource;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.payloads.invoice.RefundInvoice;
import com.smartstay.smartstay.repositories.BankTransactionRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class BankTransactionService {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService usersService;

    @Autowired
    private BankTransactionRepository bankRepository;

    private BankingService bankingService;

    @Autowired
    public void setBankingService(@Lazy BankingService bankingService) {
        this.bankingService = bankingService;
    }


    public int addTransaction(TransactionDto transactionDto) {
        if (authentication.isAuthenticated()) {
            BankTransactionsV1 transactionsV1 = new BankTransactionsV1();
            BankTransactionsV1 v1 = bankRepository.findTopByBankIdOrderByTransactionDateDesc(transactionDto.bankId());
            if (v1 == null) {
                if (transactionDto.type().equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
                    transactionsV1.setAccountBalance(-transactionDto.amount());
                }
                else {
                    transactionsV1.setAccountBalance(transactionDto.amount());
                }
            }
            else {
                if (transactionDto.type().equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
                    transactionsV1.setAccountBalance(v1.getAccountBalance() - transactionDto.amount());
                }
                else {
                    Double accountBalance = 0.0;
                    if (v1.getAccountBalance() != null) {
                        accountBalance = v1.getAccountBalance();
                    }
                    transactionsV1.setAccountBalance(transactionDto.amount() + accountBalance);
                }
            }
            transactionsV1.setTransactionDate(Utils.stringToDate(transactionDto.transactionDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            transactionsV1.setBankId(transactionDto.bankId());
            transactionsV1.setTransactionNumber(transactionDto.transactionId());
            transactionsV1.setReferenceNumber(transactionDto.referenceNumber());
            transactionsV1.setAmount(transactionDto.amount());
            transactionsV1.setType(transactionDto.type());
            transactionsV1.setSource(transactionDto.source());
            transactionsV1.setHostelId(transactionDto.hostelId());
            transactionsV1.setCreatedAt(new Date());
            transactionsV1.setCreatedBy(authentication.getName());

            bankRepository.save(transactionsV1);
        }

        return 1;
    }


    public List<com.smartstay.smartstay.dto.transaction.TransactionDto> getAllTransactions(String hostelId) {
        return bankRepository.findByHostelId(hostelId)
                .stream()
                .map(item -> new TransactionsMapper().apply(item))
                .toList();
    }

    public BankTransactionsV1 getTransaction(String bankId,String hostelId) {
        return bankRepository.findByBankIdAndHostelId(bankId,hostelId);
    }

    public BankTransactionsV1 getLatestTransaction(String bankId,String hostelId) {
        return bankRepository.findTopByBankIdAndHostelIdOrderByCreatedAtDesc(bankId,hostelId);
    }

    public BankTransactionsV1 saveTransaction(BankTransactionsV1 transaction) {
        return bankRepository.save(transaction);
    }

    /**
     *
     * this is to refund the booking amount
     */
    public void cancelBooking(TransactionDto transactionDto) {
        if (authentication.isAuthenticated()) {
            BankTransactionsV1 transactionsV1 = new BankTransactionsV1();
            BankTransactionsV1 v1 = bankRepository.findTopByBankIdOrderByTransactionDateDesc(transactionDto.bankId());

            if (v1 != null) {
                transactionsV1.setTransactionDate(Utils.stringToDate(transactionDto.transactionDate(), Utils.USER_INPUT_DATE_FORMAT));
                transactionsV1.setAccountBalance(v1.getAccountBalance() - transactionDto.amount());
                transactionsV1.setBankId(transactionDto.bankId());
                transactionsV1.setHostelId(transactionDto.hostelId());
                transactionsV1.setReferenceNumber(transactionDto.referenceNumber());
                transactionsV1.setAmount(transactionDto.amount());
                transactionsV1.setType(BankTransactionType.DEBIT.name());
                transactionsV1.setSource(BankSource.BOOKING_REFUND.name());
                transactionsV1.setCreatedAt(new Date());
                transactionsV1.setCreatedBy(authentication.getName());

                bankRepository.save(transactionsV1);
            }
        }
    }

    public boolean addExpenseTransaction(TransactionDto transactionDto) {
        if (authentication.isAuthenticated()) {

            BankTransactionsV1 transactionsV1 = new BankTransactionsV1();

            if (!bankingService.updateBalanceForExpense(transactionDto.amount(), BankTransactionType.DEBIT.name(), transactionDto.bankId(), transactionDto.transactionDate())) {
                return false;
            }

            Calendar calendar = Calendar.getInstance();
            Date dt = null;
            if (transactionDto.transactionDate() != null) {
                dt = Utils.stringToDate(transactionDto.transactionDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            }
            else {
                dt = new Date();
            }
            calendar.setTime(dt);
            if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR) == 0) {
                Calendar cal2 = Calendar.getInstance();
                calendar.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
                calendar.set(Calendar.HOUR, cal2.get(Calendar.HOUR));
                calendar.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
            }

            transactionsV1.setTransactionDate(calendar.getTime());
            transactionsV1.setBankId(transactionDto.bankId());
            transactionsV1.setReferenceNumber(transactionDto.referenceNumber());
            transactionsV1.setAmount(transactionDto.amount());
            transactionsV1.setType(transactionDto.type());
            transactionsV1.setSource(transactionDto.source());
            transactionsV1.setHostelId(transactionDto.hostelId());
            transactionsV1.setCreatedAt(new Date());
            transactionsV1.setCreatedBy(authentication.getName());

            bankRepository.save(transactionsV1);

            return true;
        }
        return false;
    }

    public boolean refundInvoice(InvoicesV1 invoicesV1, RefundInvoice refundInvoice) {
        BankTransactionsV1 transactionsV1 = new BankTransactionsV1();

        String transactionDate = refundInvoice.refundDate();
        if (!Utils.checkNullOrEmpty(refundInvoice.refundDate())) {
            transactionDate = Utils.dateToString(new Date());
        }

        if (!bankingService.updateBalanceForExpense(refundInvoice.refundAmount(), BankTransactionType.DEBIT.name(), refundInvoice.bankId(), transactionDate)) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        Date dt = null;
        if (refundInvoice.refundDate() != null) {
            dt = Utils.stringToDate(refundInvoice.refundDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        else {
            dt = new Date();
        }
        calendar.setTime(dt);
        if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR) == 0) {
            Calendar cal2 = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
            calendar.set(Calendar.HOUR, cal2.get(Calendar.HOUR));
            calendar.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
        }

        transactionsV1.setTransactionDate(calendar.getTime());
        transactionsV1.setBankId(refundInvoice.bankId());
        transactionsV1.setReferenceNumber(refundInvoice.referenceNumber());
        transactionsV1.setAmount(refundInvoice.refundAmount());
        transactionsV1.setType(BankTransactionType.DEBIT.name());
        transactionsV1.setSource(BankSource.INVOICE.name());
        transactionsV1.setHostelId(invoicesV1.getHostelId());
        transactionsV1.setCreatedAt(new Date());
        transactionsV1.setCreatedBy(authentication.getName());

        bankRepository.save(transactionsV1);

        return true;

    }

    public void deleteReceipt(TransactionDto transaction) {
        if (transaction != null) {
            BankTransactionsV1 transactionsV1 = bankRepository.findByTransactionNumber(transaction.transactionId());
            if (transactionsV1 != null) {
                bankRepository.delete(transactionsV1);
            }
        }
    }

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return bankRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }
}
