package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.events.AddRoomSettlementEbEvents;
import com.smartstay.smartstay.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoomSettlementEbListener {
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomerEbHistoryService ebHistoryService;
    @Autowired
    private CustomersBedHistoryService customerBedHistory;
    @Autowired
    private EbCalculationService ebCalculationService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private CustomerWalletHistoryService walletHistoryService;

    @Async
    @EventListener
    public void addRoomSettlementEbListener(AddRoomSettlementEbEvents ebEvents) {
        String customerId = ebEvents.getCustomerId();
        String createdBy = ebEvents.getCreatedBy();
        if (customerId != null) {
            List<CustomersBedHistory> listBedHistory = customerBedHistory.getCustomersBedHistoryList(customerId);
            if (!listBedHistory.isEmpty()) {
                List<ElectricityReadings> electricityReadingsForInvoice = new ArrayList<>();
                listBedHistory.forEach(item -> {
                    System.out.println(ebEvents.getHostelId());
                    Date endDate = item.getEndDate();
                    if (item.getEndDate() == null) {
                        endDate = ebEvents.getEndDate();
                    }
                    List<ElectricityReadings> listElectricityReadings = electricityService.getAllEbReadingByEndDate(ebEvents.getHostelId(), item.getRoomId(), endDate);
                    electricityReadingsForInvoice.addAll(listElectricityReadings);
                });
                if (!electricityReadingsForInvoice.isEmpty()) {
                    List<Integer> readingIds = electricityReadingsForInvoice
                            .stream()
                            .map(ElectricityReadings::getId)
                            .toList();
                    if (!readingIds.isEmpty()) {
                        List<CustomersEbHistory> listCustomerEbs = ebHistoryService.getAllByReadingId(readingIds);

                        Set<Integer> existingReadingIds = listCustomerEbs.stream()
                                .map(CustomersEbHistory::getReadingId)
                                .collect(Collectors.toSet());

                        List<Integer> missingReadingIds = readingIds.stream()
                                .filter(id -> !existingReadingIds.contains(id))
                                .toList();

                        List<ElectricityReadings> listMissingReadings = electricityService.getAllByIds(missingReadingIds);
                        if (!listMissingReadings.isEmpty()) {
                            List<CustomersEbHistory> customersEbHistories = ebCalculationService.calculateEbAmountAndUnitForAll(ebEvents.getHostelId(), listMissingReadings);
                            if (!customersEbHistories.isEmpty()) {
                                ebHistoryService.saveCustomerEb(customersEbHistories);
                            }

                        }


                    }

                    List<ElectricityReadings> newElectricityReadings = electricityReadingsForInvoice
                            .stream()
                            .map(i -> {
                                i.setBillStatus(ElectricityBillStatus.INVOICE_GENERATED.name());
                                return i;
                            })
                            .toList();

                    electricityService.saveAll(newElectricityReadings);

                    List<CustomersEbHistory> customerHistory = ebHistoryService
                            .getAllByReadingId(readingIds);

                    if (!customerHistory.isEmpty()) {
                        List<String> customerIds = customerHistory
                                .stream()
                                .map(CustomersEbHistory::getCustomerId)
                                .toList();
                        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
                        List<Customers> customerWallets = new ArrayList<>();
                        List<CustomerWalletHistory> listCustomerWallets = new ArrayList<>();

                        customerHistory.forEach(history -> {
                            Customers cus = listCustomers
                                    .stream()
                                    .filter(f -> f.getCustomerId().equalsIgnoreCase(history.getCustomerId()))
                                    .findFirst()
                                    .orElse(null);
                            if (cus != null && !customerId.equalsIgnoreCase(history.getCustomerId())) {
                                CustomerWallet cusWallet = cus.getWallet();
                                if (cusWallet != null) {
                                    if (cusWallet.getAmount() != null) {
                                        cusWallet.setAmount(cusWallet.getAmount() + history.getAmount());
                                        cusWallet.setTransactionDate(new Date());
                                    }
                                    else {
                                        cusWallet.setAmount(history.getAmount());
                                        cusWallet.setTransactionDate(new Date());
                                    }
                                }
                                else {
                                    cusWallet = new CustomerWallet();
                                    cusWallet.setAmount(history.getAmount());
                                    cusWallet.setTransactionDate(new Date());
                                    cusWallet.setCustomers(cus);
                                    cus.setWallet(cusWallet);
                                }
                                if (!cus.getCustomerId().equalsIgnoreCase(customerId)) {
                                    customerWallets.add(cus);
                                    listCustomerWallets.add(walletHistoryService.formWalletHistory(cus.getCustomerId(), history, createdBy));
                                }

                            }
                        });

                        if (!listCustomerWallets.isEmpty()) {
                            walletHistoryService.saveAll(listCustomerWallets);
                        }

                        if (!customerWallets.isEmpty()) {
                            customersService.updateCustomerWallets(customerWallets);
                        }
                    }
                }

            }
        }
    }


}
