package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.UserHostel;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.events.AddAdminEvents;
import com.smartstay.smartstay.services.BankingService;
import com.smartstay.smartstay.services.UserHostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AddAdminEventListener {

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private BankingService bankingService;

    @Async
    @EventListener
    public void handleAdminUserCreated(AddAdminEvents events) {
        List<UserHostel> listUserHostels = userHostelService.findByUserId(events.getAdminId());

        List<BankingV1> listBanks = listUserHostels
                .stream()
                .map(item -> {
                    BankingV1 bankingV1 = new BankingV1();
                    bankingV1.setUserId(events.getAdminId());
                    bankingV1.setActive(true);
                    bankingV1.setHostelId(item.getHostelId());
                    bankingV1.setParentId(events.getParentId());
                    bankingV1.setAccountType(BankAccountType.CASH.name());
                    bankingV1.setTransactionType(BankPurpose.BOTH.name());
                    bankingV1.setAccountHolderName(events.getAdminName());
                    bankingV1.setCreatedBy(events.getAdminId());
                    bankingV1.setCreatedAt(new Date());

                    return bankingV1;

                })
                .toList();

        bankingService.saveAllBankInfo(listBanks);

    }
}
