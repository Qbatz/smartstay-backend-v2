package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.events.RecurringEvents;
import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class RecurringEventListener {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private CustomerEbHistoryService customerEbHistoryService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private TemplatesService templatesService;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CustomersConfigService customersConfigService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private RecurringTrackerService recurringTrackerService;
    @Autowired
    private RecurringConfigService recurringConfigService;
    @Autowired
    private InvoiceDraftsService invoiceDraftsService;

    @Async
    @EventListener
    public void OnRecurringSetup(RecurringEvents recurringEvents) {

        HostelV1 hostelV1 = hostelService.getHostelInfo(recurringEvents.getHostelId());
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(recurringEvents.getHostelId());
        ElectricityConfig ebConfig = hostelService.getElectricityConfig(hostelV1.getHostelId());
        boolean shouldIncludeEb;
        double flatEbAmount;
        boolean isFlatRate;
        if (ebConfig != null) {
            if (ebConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.FLAT_RATE.name())) {
                if (!ebConfig.isShouldIncludeInRent()) {
                    flatEbAmount = ebConfig.getFlatCharge();
                    isFlatRate = true;
                    shouldIncludeEb = false;
                }
                else {
                    shouldIncludeEb = false;
                    isFlatRate = false;
                    flatEbAmount = 0.0;
                }
            }
            else {
                flatEbAmount = 0.0;
                isFlatRate = false;
                shouldIncludeEb = true;
            }
        } else {
            flatEbAmount = 0.0;
            isFlatRate = false;
            shouldIncludeEb = true;
        }


        List<CustomersConfig> listCustomerConfig = customersConfigService.getAllActiveAndEnabledRecurringCustomers(recurringEvents.getHostelId());

        List<String> tempCusIds = listCustomerConfig.stream()
                .map(CustomersConfig::getCustomerId)
                .toList();
        List<AmenitiesV1> listAmenities = amenitiesService.getAllAmenitiesByHostelId(hostelV1.getHostelId());
        List<BookingsV1> customersList = bookingsService.getAllCheckedInCustomersByListOfCustomerIdsAndHostelId(tempCusIds, recurringEvents.getHostelId());
        List<String> customerIds = customersList
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();

        List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService.getWalletListForRecurring(customerIds);

        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);

        List<ElectricityReadings> listElectricityForAHostel = new ArrayList<>();
        if (shouldIncludeEb) {
            listElectricityForAHostel = electricityService.getAllElectricityReadingForRecurring(recurringEvents.getHostelId());
        } else {
            listElectricityForAHostel = new ArrayList<>();
        }

        List<ElectricityReadings> finalListElectricityForAHostel = listElectricityForAHostel;
        customersList
                .forEach(item -> {
                    Double rentAmount = item.getRentAmount();
                    Double ebAmount = 0.0;
                    if (shouldIncludeEb) {
                        List<Integer> ebReadingsId = finalListElectricityForAHostel
                                .stream()
                                .map(ElectricityReadings::getId)
                                .toList();
                        List<CustomersEbHistory> listCustomerEb = customerEbHistoryService.getAllByCustomerIdAndReadingId(item.getCustomerId(), ebReadingsId);

                        ebAmount = listCustomerEb
                                .stream()
                                .mapToDouble(CustomersEbHistory::getAmount)
                                .sum();
                        if (ebAmount > 0) {
                            ebAmount = Utils.roundOfDouble(ebAmount);
                        }
                    }
                    else if (isFlatRate) {
                        ebAmount = flatEbAmount;
                    }

//            List<CustomersAmenity> listCustomersAmenity = amenitiesService.getAllAmenitiesByCustomerId(item.getCustomerId());
                    List<CustomersAmenity> listCustomersAmenity = amenitiesService.getAllCustomerAmenitiesForRecurring(item.getCustomerId(), billingDates.currentBillStartDate());
                    Double amenityAmount = listCustomersAmenity
                            .stream()
                            .mapToDouble(CustomersAmenity::getAmenityPrice)
                            .sum();

                    double rentEbAmount = rentAmount + ebAmount;
                    double rentEbAndAmenity = rentEbAmount + amenityAmount;
                    double walletAmount = 0.0;
                    double finalAmount = rentEbAndAmenity;

                    Customers customers = listCustomers
                            .stream()
                            .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
                            .findFirst()
                            .orElse(null);
                    if (customers != null) {
                        boolean shouldVerify = false;
                        RecurringConfiguration recurringConfiguration = recurringConfigService.getRecurringConfig(hostelV1.getHostelId());
                        if (recurringConfiguration != null) {
                            if (recurringConfiguration.getShouldVerify() != null) {
                                shouldVerify = recurringConfiguration.getShouldVerify();
                            }
                        }

                        if (!shouldVerify) {
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


                            InvoicesV1 invoicesV1 = new InvoicesV1();
                            invoicesV1.setCancelled(false);
                            invoicesV1.setCustomerId(item.getCustomerId());
                            invoicesV1.setCustomerMailId(customers.getEmailId());
                            invoicesV1.setCustomerMobile(customers.getMobile());
                            invoicesV1.setHostelId(recurringEvents.getHostelId());
                            invoicesV1.setInvoiceNumber(prefixSuffix.toString());
                            invoicesV1.setInvoiceType(InvoiceType.RENT.name());
                            invoicesV1.setBasePrice(Utils.roundOfDouble(finalAmount));
                            invoicesV1.setTotalAmount(Utils.roundOfDouble(finalAmount));
                            invoicesV1.setPaidAmount(0.0);
                            invoicesV1.setCgst(0.0);
                            invoicesV1.setSgst(0.0);
                            invoicesV1.setGst(0.0);
                            invoicesV1.setGstPercentile(0.0);
                            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
                            invoicesV1.setOthersDescription(null);
                            invoicesV1.setInvoiceMode(InvoiceMode.RECURRING.name());
                            invoicesV1.setCreatedBy(hostelV1.getCreatedBy());
                            invoicesV1.setInvoiceGeneratedDate(new Date());
                            invoicesV1.setInvoiceDate(new Date());
                            invoicesV1.setInvoiceDueDate(billingDates.dueDate());
                            invoicesV1.setInvoiceStartDate(billingDates.currentBillStartDate());
                            invoicesV1.setInvoiceEndDate(billingDates.currentBillEndDate());
                            invoicesV1.setCreatedAt(new Date());

                            List<InvoiceItems> invoicesItems = new ArrayList<>();
                            if (rentAmount > 0) {
                                InvoiceItems item1 = new InvoiceItems();
                                item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
                                item1.setAmount(Utils.roundOffWithTwoDigit(rentAmount));
                                item1.setInvoice(invoicesV1);
                                invoicesItems.add(item1);
                            }

                            if (ebAmount > 0) {
                                InvoiceItems item1 = new InvoiceItems();
                                item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                                item1.setAmount(Utils.roundOffWithTwoDigit(ebAmount));
                                item1.setInvoice(invoicesV1);
                                invoicesItems.add(item1);
                            }

                            if (listCustomersAmenity != null) {
                                listCustomersAmenity.forEach(amenity -> {
                                    AmenitiesV1 amenitiesV1 = listAmenities
                                            .stream()
                                            .filter(amty -> amty.getAmenityId().equalsIgnoreCase(amenity.getAmenityId()))
                                            .findFirst()
                                            .orElse(null);
                                    if (amenitiesV1 != null) {
                                        InvoiceItems item1 = new InvoiceItems();
                                        item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                                        item1.setOtherItem(amenitiesV1.getAmenityName());
                                        item1.setAmount(Utils.roundOffWithTwoDigit(amenity.getAmenityPrice()));
                                        item1.setInvoice(invoicesV1);
                                        invoicesItems.add(item1);
                                    }

                                });
                            }

                            List<CustomerWalletHistory> wh = listCustomerWallets
                                    .stream()
                                    .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                                    .toList();
                            if (!wh.isEmpty()) {
                                wh.forEach(it -> {
                                    InvoiceItems itms = new InvoiceItems();
                                    if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                                        itms.setInvoiceItem(it.getSourceType());
                                    } else if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                                        itms.setInvoiceItem(it.getSourceType());
                                    } else {
                                        itms.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                                        itms.setOtherItem(it.getSourceType());
                                    }
                                    itms.setAmount(Utils.roundOffWithTwoDigit(it.getAmount()));
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

                            if (!wh.isEmpty()) {
                                List<CustomerWalletHistory> whu = wh
                                        .stream()
                                        .map(im -> {
                                            im.setBillingStatus(WalletBillingStatus.INVOICE_GENERATED.name());
                                            return im;
                                        })
                                        .toList();

                                customerWalletHistoryService.saveAll(whu);
                            }
                        }
                        else {
                            generateInvoiceForVerification(customers, billingDates.currentBillStartDate(),
                                    billingDates.currentBillEndDate(),
                                    hostelV1.getCreatedBy(),
                                    billingDates.dueDays(),
                                    rentAmount,
                                    ebAmount,
                                    listCustomersAmenity,
                                    listAmenities,
                                    listCustomerWallets);
                        }

//                        if (amenityAmount > 0) {
//                            InvoiceItems item1 = new InvoiceItems();
//                            item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
//                            item1.setAmount(Utils.roundOffWithTwoDigit(amenityAmount));
//                            item1.setInvoice(invoicesV1);
//                            invoicesItems.add(item1);
//                        }



                    }

                });
        if (shouldIncludeEb) {
            List<ElectricityReadings> listReadingForMakingInvoiceGenerated = listElectricityForAHostel
                    .stream()
                    .map(i -> {
                        i.setBillStatus(ElectricityBillStatus.INVOICE_GENERATED.name());
                        i.setUpdatedAt(new Date());
                        i.setUpdatedBy(hostelV1.getCreatedBy());
                        return i;
                    })
                    .toList();
            electricityService.markAsInvoiceGenerated(listReadingForMakingInvoiceGenerated);
        }
        recurringTrackerService.markAsInvoiceGenerated(hostelV1.getHostelId());
        notificationService.addAdminNotificationsForRecurringInvoice(hostelV1.getHostelId());

    }

    private void generateInvoiceForVerification(Customers customers,
                                                Date invoiceStartDate,
                                                Date invoiceEndDate,
                                                String createdBy,
                                                Integer dueDays,
                                                Double rentAmount,
                                                Double ebAmount,
                                                List<CustomersAmenity> listCustomerAmenities,
                                                List<AmenitiesV1> listAmenities,
                                                List<CustomerWalletHistory> listCustomerWallets) {
        double walletAmount = 0.0;
        double finalAmount = 0.0;
        CustomerWallet customerWallet = customers.getWallet();
        if (customerWallet != null) {
            if (customerWallet.getAmount() != null) {
                walletAmount = customerWallet.getAmount();
                finalAmount = finalAmount + walletAmount;
            }
        }

        int dDays = 0;
        if (dueDays != null) {
            dDays = dueDays;
        }

        Date dueDate = Utils.addDaysToDate(invoiceStartDate, dDays-1);

        InvoiceDrafts invoiceDrafts = new InvoiceDrafts();
        invoiceDrafts.setCustomerId(customers.getCustomerId());
        invoiceDrafts.setHostelId(customers.getHostelId());
        invoiceDrafts.setCustomerMailId(customers.getEmailId());
        invoiceDrafts.setCustomerMobile(customers.getMobile());
        invoiceDrafts.setInvoiceType(InvoiceType.RENT.name());
        invoiceDrafts.setBasePrice(Utils.roundOfDouble(finalAmount));
        invoiceDrafts.setTotalAmount(Utils.roundOfDouble(finalAmount));
        invoiceDrafts.setPaidAmount(0.0);
        invoiceDrafts.setBalanceAmount(0.0);
        invoiceDrafts.setSubTotal(0.0);
        invoiceDrafts.setGst(0.0);
        invoiceDrafts.setCgst(0.0);
        invoiceDrafts.setSgst(0.0);
        invoiceDrafts.setGstPercentile(0.0);
        invoiceDrafts.setPaymentStatus(PaymentStatus.PENDING.name());
        invoiceDrafts.setDeductionAmount(0.0);
        invoiceDrafts.setOthersDescription(null);
        invoiceDrafts.setInvoiceMode(InvoiceMode.RECURRING.name());
        invoiceDrafts.setCancelled(false);
        invoiceDrafts.setDiscounted(false);
        invoiceDrafts.setDiscountAmount(0.0);
        invoiceDrafts.setCreatedBy(createdBy);
        invoiceDrafts.setInvoiceGeneratedDate(new Date());
        invoiceDrafts.setCancelledDate(null);
        invoiceDrafts.setInvoiceDueDate(dueDate);
        invoiceDrafts.setInvoiceDate(new Date());
        invoiceDrafts.setInvoiceStartDate(invoiceStartDate);
        invoiceDrafts.setInvoiceEndDate(invoiceEndDate);
        invoiceDrafts.setCreatedAt(new Date());
        invoiceDrafts.setUpdatedAt(new Date());

        List<DraftItems> listDraftedItem = new ArrayList<>();
        if (rentAmount > 0) {
            DraftItems di = new DraftItems();
            di.setAmount(rentAmount);
            di.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
            di.setInvoiceDrafts(invoiceDrafts);
            listDraftedItem.add(di);
        }

        if (ebAmount > 0) {
            DraftItems di = new DraftItems();
            di.setAmount(ebAmount);
            di.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
            di.setInvoiceDrafts(invoiceDrafts);
            listDraftedItem.add(di);
        }

        if (listCustomerAmenities != null) {
            listCustomerAmenities.forEach(amenity -> {
                AmenitiesV1 amenitiesV1 = listAmenities
                        .stream()
                        .filter(amty -> amty.getAmenityId().equalsIgnoreCase(amenity.getAmenityId()))
                        .findFirst()
                        .orElse(null);
                if (amenitiesV1 != null) {
                    DraftItems item1 = new DraftItems();
                    item1.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                    item1.setOtherItem(amenitiesV1.getAmenityName());
                    item1.setAmount(Utils.roundOffWithTwoDigit(amenity.getAmenityPrice()));
                    item1.setInvoiceDrafts(invoiceDrafts);
                    listDraftedItem.add(item1);
                }

            });
        }

        List<CustomerWalletHistory> wh = listCustomerWallets
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .toList();
        if (!wh.isEmpty()) {
            wh.forEach(it -> {
                DraftItems itms = new DraftItems();
                if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                    itms.setInvoiceItem(it.getSourceType());
                } else if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                    itms.setInvoiceItem(it.getSourceType());
                } else {
                    itms.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                    itms.setOtherItem(it.getSourceType());
                }
                itms.setAmount(Utils.roundOffWithTwoDigit(it.getAmount()));
                itms.setInvoiceDrafts(invoiceDrafts);

                listDraftedItem.add(itms);
            });
        }

        invoiceDrafts.setListItems(listDraftedItem);
        invoiceDraftsService.saveFromRecurring(invoiceDrafts);

        CustomerWallet updateWallet = customers.getWallet();
        if (updateWallet != null) {
            updateWallet.setAmount(0.0);
            customers.setWallet(updateWallet);
        }

        customersService.updateCustomersFromRecurring(customers);

        if (!wh.isEmpty()) {
            List<CustomerWalletHistory> whu = wh
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
