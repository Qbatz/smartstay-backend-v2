package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.WalletBillingStatus;
import com.smartstay.smartstay.events.JoiningBasedPrepaidEvents;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.BillingCycleUtil;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JoiningBasedPrepaidEventListener {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private TemplatesService templatesService;

    @Async
    @EventListener
    public void RecurringSetupForJoiningBasedPrepaid(JoiningBasedPrepaidEvents jbpe) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(jbpe.getHostelId());
        BookingsV1 bookingsV1 = bookingsService.getBookingInfoByCustomerId(jbpe.getCustomerId());
        Customers customers = customersService.getCustomerInformation(jbpe.getCustomerId());
        BillingDates billingDates = hostelService.getBillingRuleOnDate(jbpe.getHostelId(), new Date());
        String customerId = jbpe.getCustomerId();
        if (bookingsV1 != null) {
            List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService.getAllInvoiceNotGeneratedWallets(customerId);
            double rentAmount = bookingsV1.getRentAmount();

            List<CustomersAmenity> listCustomersAmenity = amenitiesService.getAllCustomerAmenitiesForRecurring(customerId, new Date());
            Double amenityAmount = listCustomersAmenity
                    .stream()
                    .mapToDouble(CustomersAmenity::getAmenityPrice)
                    .sum();
            double ebAmount = 0.0;
            double rentEbAmount = rentAmount + ebAmount;
            double rentEbAndAmenity = rentEbAmount + amenityAmount;
            double walletAmount = 0.0;
            double finalAmount = rentEbAndAmenity;

            CustomerWallet customerWallet = customers.getWallet();
            if (customerWallet != null) {
                if (customerWallet.getAmount() != null) {
                    walletAmount = customerWallet.getAmount();
                    finalAmount = finalAmount + walletAmount;
                }
            }

            StringBuilder prefixSuffix = new StringBuilder();

            String prefix = "INV";
            com.smartstay.smartstay.dao.BillTemplates templates = templatesService.getTemplateByHostelId(customers.getHostelId());
            if (templates != null && templates.getTemplateTypes() != null) {
                if (!templates.getTemplateTypes().isEmpty()) {
                    BillTemplateType rentTemplateType = templates.getTemplateTypes()
                            .stream()
                            .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                            .findFirst()
                            .get();
                    prefix = rentTemplateType.getInvoicePrefix();
                }
                prefixSuffix.append(prefix);
            }

            InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(prefix, hostelV1.getHostelId());

            if (inv != null) {
                String[] prefArr = inv.getInvoiceNumber().split("-");
                if (prefArr.length > 1) {
                    int suffix = Integer.parseInt(prefArr[prefArr.length - 1]) + 1;
                    prefixSuffix.append("-");
                    if (suffix < 10) {
                        prefixSuffix.append("00");
                        prefixSuffix.append(suffix);
                    } else if (suffix < 100) {
                        prefixSuffix.append("0");
                        prefixSuffix.append(suffix);
                    } else {
                        prefixSuffix.append(suffix);
                    }
                }
            } else {
                //this is going to be the first invoice
                prefixSuffix.append("-");
                prefixSuffix.append("001");
            }

            Date dueDate = Utils.addDaysToDate(new Date(), billingDates.dueDays());
            int cycleStartDate = Utils.dateToDate(new Date());
            Date invoiceEndDate = Utils.findLastDate(cycleStartDate, new Date());
            InvoicesV1 invoicesV1 = new InvoicesV1();
            invoicesV1.setCancelled(false);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setCustomerMailId(customers.getEmailId());
            invoicesV1.setCustomerMobile(customers.getMobile());
            invoicesV1.setHostelId(jbpe.getHostelId());
            invoicesV1.setInvoiceNumber(prefixSuffix.toString());
            invoicesV1.setInvoiceType(InvoiceType.RENT.name());
            invoicesV1.setBasePrice(finalAmount);
            invoicesV1.setTotalAmount(finalAmount);
            invoicesV1.setPaidAmount(0.0);
            invoicesV1.setCgst(0.0);
            invoicesV1.setSgst(0.0);
            invoicesV1.setGst(0.0);
            invoicesV1.setGstPercentile(0.0);
            invoicesV1.setPaymentStatus(com.smartstay.smartstay.ennum.PaymentStatus.PENDING.name());
            invoicesV1.setOthersDescription(null);
            invoicesV1.setInvoiceMode(InvoiceMode.RECURRING.name());
            invoicesV1.setCreatedBy(hostelV1.getCreatedBy());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setInvoiceDueDate(dueDate);
            invoicesV1.setInvoiceStartDate(new Date());
            invoicesV1.setInvoiceEndDate(invoiceEndDate);
            invoicesV1.setCreatedAt(new Date());

            List<InvoiceItems> invoicesItems = new ArrayList<>();
            if (rentAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
                item1.setAmount(rentAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (ebAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                item1.setAmount(ebAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (amenityAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
                item1.setAmount(amenityAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (!listCustomerWallets.isEmpty()) {
                listCustomerWallets.forEach(it -> {
                    InvoiceItems itms = new InvoiceItems();
                    if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                        itms.setInvoiceItem(it.getSourceType());
                    } else if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                        itms.setInvoiceItem(it.getSourceType());
                    } else {
                        itms.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                        itms.setOtherItem(it.getSourceType());
                    }
                    itms.setAmount(it.getAmount());
                    itms.setInvoice(invoicesV1);

                    invoicesItems.add(itms);
                });
            }

            invoicesV1.setInvoiceItems(invoicesItems);

            invoicesV1Repository.save(invoicesV1);

            CustomerWallet updateWallet = customers.getWallet();
            if (updateWallet != null) {
                updateWallet.setAmount(0.0);
                customers.setWallet(updateWallet);
            }

            customersService.updateCustomersFromRecurring(customers);


            if (!listCustomerWallets.isEmpty()) {
                List<CustomerWalletHistory> whu = listCustomerWallets
                        .stream()
                        .map(im -> {
                            im.setBillingStatus(WalletBillingStatus.INVOICE_GENERATED.name());
                            return im;
                        })
                        .toList();

                customerWalletHistoryService.saveAll(whu);
            }

        }
    }
}
