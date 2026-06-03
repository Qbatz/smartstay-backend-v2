package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.SettlementItems;
import com.smartstay.smartstay.dto.settlement.CurrentOtherItems;
import com.smartstay.smartstay.dto.settlement.CurrentRentBreakUp;
import com.smartstay.smartstay.dto.settlement.SettlementUnpaidInvoices;
import com.smartstay.smartstay.dto.settlement.WalltetItems;
import com.smartstay.smartstay.repositories.SettlementItemsRepository;
import com.smartstay.smartstay.responses.customer.FinalSettlement;
import com.smartstay.smartstay.responses.customer.UnpaidInvoices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SettlementItemService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private SettlementItemsRepository settlementItemsRepository;

    public SettlementItems generateSettlementItems(String customerId, String hostelId, String invoiceId, FinalSettlement settlementInfo) {
        SettlementItems settlementItems = new SettlementItems();
        if (settlementInfo != null) {
            if (settlementInfo.unpaidInvoiceInfo() != null) {
                List<UnpaidInvoices> listUnpaidInvoices = settlementInfo.unpaidInvoiceInfo().listUnpaidInvoices();
                if (listUnpaidInvoices != null) {
                    List<SettlementUnpaidInvoices> settlementUnpaidInvoicesList = listUnpaidInvoices
                            .stream()
                            .map(i -> new SettlementUnpaidInvoices(i.invoiceNumber(), i.invoiceTotalAmount(), i.type(), i.invoiceId(), i.payableAmount()))
                            .toList();
                    settlementItems.setUnpaidInvoices(settlementUnpaidInvoicesList);
                }
            }

            if (settlementInfo.walletInfo() != null) {
                if (settlementInfo.walletInfo().transactions() != null) {
                    List<WalltetItems> settlementWalletItems = settlementInfo.walletInfo().transactions()
                            .stream()
                            .map(i -> new WalltetItems(i.source(), i.amount(), i.walletId()))
                            .toList();
                    settlementItems.setWalltetItems(settlementWalletItems);
                }
            }

            if (settlementInfo.currentMonthRentInfo() != null) {
                if (settlementInfo.currentMonthRentInfo().rentLists() != null) {
                    List<CurrentRentBreakUp> listCurrentRentBreakUp = settlementInfo
                            .currentMonthRentInfo()
                            .rentLists()
                            .stream()
                            .map(i -> new CurrentRentBreakUp(i.bedName(), i.roomName(), i.floorName(), i.dStartDate(), i.dEndDate(),i.rentPerDay(),  i.rent(), false))
                            .toList();
                    settlementItems.setCurrentRentBreakUps(listCurrentRentBreakUp);
                }
                settlementItems.setCurrentMonthPayableAmount(settlementInfo.currentMonthRentInfo().currentMonthTotalAmount());
                settlementItems.setCurrentMonthPaidAmount(settlementInfo.currentMonthRentInfo().currentRentPaid());

                if (settlementInfo.currentMonthRentInfo().currentMonthOtherItems() != null) {
                    List<CurrentOtherItems> listCurrentOtherItems = settlementInfo
                            .currentMonthRentInfo()
                            .currentMonthOtherItems()
                            .stream()
                            .map(i -> new CurrentOtherItems(i.item(), i.amount()))
                            .toList();
                    settlementItems.setCurrentMonthOtherItems(listCurrentOtherItems);
                }

            }
            settlementItems.setHostelId(hostelId);
            settlementItems.setCustomerId(customerId);
            settlementItems.setInvoiceId(invoiceId);
            if (settlementInfo.bookingItems().availableAdvanceBalance() != null) {
                settlementItems.setBookingBalance(settlementInfo.bookingItems().availableAdvanceBalance());
            }
            if (settlementInfo.advanceItems().availableAdvanceBalance() != null) {
                settlementItems.setAdvanceBalance(settlementInfo.advanceItems().availableAdvanceBalance());
            }
            settlementItems.setCreateAt(new Date());
            settlementItems.setCreatedBy(authentication.getName());


        }
        return settlementItemsRepository.save(settlementItems);
    }

    public SettlementItems getSettlemtItems(String invoiceId) {
        return settlementItemsRepository.findByInvoiceId(invoiceId);
    }

    public void updateEBItemsFromSettlement(SettlementItems settlementItems) {
        settlementItemsRepository.save(settlementItems);
    }
}
