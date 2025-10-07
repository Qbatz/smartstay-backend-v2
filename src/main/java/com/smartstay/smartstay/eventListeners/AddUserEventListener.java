package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.BankPurpose;
import com.smartstay.smartstay.events.AddUserEvents;
import com.smartstay.smartstay.services.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class AddUserEventListener {

    @Autowired
    private BankingService bankingService;
    @Async
    @EventListener
    public void addHandleUserCreated(AddUserEvents addUserEvents) {
        BankingV1 bankingV1 = new BankingV1();
        bankingV1.setCreatedAt(new Date());
        bankingV1.setCreatedBy(addUserEvents.getUserId());
        bankingV1.setUserId(addUserEvents.getUserId());
        bankingV1.setAccountHolderName(addUserEvents.getFullName());
        bankingV1.setActive(true);
        bankingV1.setAccountType(BankAccountType.CASH.name());
        bankingV1.setParentId(addUserEvents.getParentId());
        bankingV1.setDeleted(false);
        bankingV1.setHostelId(addUserEvents.getHostelId());
        bankingV1.setTransactionType(BankPurpose.BOTH.name());

        List<BankingV1> listBanks = new ArrayList<>();
        listBanks.add(bankingV1);

        bankingService.saveAllBankInfo(listBanks);
    }
}
