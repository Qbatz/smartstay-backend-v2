package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.converters.*;
import com.smartstay.smartstay.dto.settlement.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementInvoiceId;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = SettlementUnpaidInvoicesConverter.class)
    private List<SettlementUnpaidInvoices> unpaidInvoices;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = SettlementWalletConverter.class)
    private List<WalltetItems> walltetItems;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = CurrentRentBreakUpConverter.class)
    private List<CurrentRentBreakUp> currentRentBreakUps;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = CurrentOtherItemConverter.class)
    private List<CurrentOtherItems> currentMonthOtherItems;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = SettlementEBItemsConverter.class)
    private List<EBItems> ebItems;

    private Double currentMonthPayableAmount;
    private Double currentMonthPaidAmount;
    private Double fullRent;
    private Boolean isFullRentCollected;
    private String hostelId;
    private String customerId;
    private String invoiceId;
    private Double bookingBalance;
    private Double advanceBalance;
    private Date createAt;
    private String createdBy;
}
