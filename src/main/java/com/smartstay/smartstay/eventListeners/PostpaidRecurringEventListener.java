package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.events.PostpaidRecurringEvents;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PostpaidRecurringEventListener {
    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomersConfigService customersConfigService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private CustomerEbHistoryService customerEbHistoryService;
    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private TemplatesService templatesService;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private RecurringTrackerService recurringTrackerService;
    @Autowired
    private NotificationService notificationService;

    @Async
    @EventListener
    public void generateInvoiceForPostpaid(PostpaidRecurringEvents postpaidRecurringEvents) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(postpaidRecurringEvents.getHostelId());
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(postpaidRecurringEvents.getHostelId());
//        List<CustomersConfig> listCustomerConfig = customersConfigService.getAllActiveAndEnabledRecurringCustomers(postpaidRecurringEvents.getHostelId());

//        List<String> tempCusIds = listCustomerConfig.stream()
//                .map(CustomersConfig::getCustomerId)
//                .toList();

        AtomicReference<Date> invoiceStartDate = new AtomicReference<>();
        Date invoiceEndDate = null;

        List<BookingsV1> customersList = bookingsService.findCheckedInCustomers(postpaidRecurringEvents.getHostelId());
        List<String> customerIds = customersList
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();

        List<CustomersBedHistory> listCustomerBedHistory = customersBedHistoryService.findBedHistoriesByListOfCustomersAndDates(customerIds, billingDates.currentBillStartDate(), billingDates.currentBillEndDate());

        List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService.getWalletListForRecurring(customerIds);

        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        List<ElectricityReadings> listElectricityForAHostel = electricityService.getAllElectricityReadingForRecurring(postpaidRecurringEvents.getHostelId());

        customersList
                .forEach(item -> {
                    Double rentAmount = item.getRentAmount();
                    List<CustomersBedHistory> currentHistory = listCustomerBedHistory.stream()
                            .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
                            .toList();
                    //No bed change happens
                    if (currentHistory.size() < 2) {
                        if (Utils.compareWithTwoDates(item.getJoiningDate(), billingDates.currentBillStartDate()) <= 0) {
                            rentAmount = item.getRentAmount();
                            invoiceStartDate.set(billingDates.currentBillStartDate());
                        }
                        else {
                            invoiceStartDate.set(item.getJoiningDate());
                            if (billingDates.hasGracePeriod()) {
                                Date dateAfterGracePeriod = Utils.addDaysToDate(billingDates.currentBillStartDate(), billingDates.gracePeriodDays());
                                if (Utils.compareWithTwoDates(item.getJoiningDate(), dateAfterGracePeriod) <= 0) {
                                    rentAmount = item.getRentAmount();
                                }
                                else {
                                    rentAmount = calculateRentAmount(item.getJoiningDate(), item.getRentAmount(), billingDates);
                                }
                            }
                            else {
                                rentAmount = calculateRentAmount(item.getJoiningDate(), item.getRentAmount(), billingDates);
                            }
                        }
                    }
                    else {
                        if (Utils.compareWithTwoDates(item.getJoiningDate(), billingDates.currentBillStartDate()) <= 0) {
                            invoiceStartDate.set(billingDates.currentBillStartDate());
                        }
                        else {
                            invoiceStartDate.set(item.getJoiningDate());
                        }
                        //executes when bed change happens
                        List<CustomersBedHistory> oldBedHistories = currentHistory
                                .stream()
                                .filter(i -> i.getEndDate() != null)
                                .toList();

                        AtomicReference<Double> oldRentAmounts = new AtomicReference<>(0.0);
                        if (oldBedHistories != null && !oldBedHistories.isEmpty()) {
                            oldBedHistories.forEach(oldItems -> {
                                double fullRentAmount = oldItems.getRentAmount();
                                long totalNoOfDaysInTheMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                                Date startDate = oldItems.getStartDate();
                                if (Utils.compareWithTwoDates(oldItems.getStartDate(), billingDates.currentBillStartDate()) < 0) {
                                    startDate = billingDates.currentBillStartDate();
                                }
                                long noOfDaysStayed = Utils.findNumberOfDays(startDate, oldItems.getEndDate());
                                double rentPerDay = fullRentAmount / totalNoOfDaysInTheMonth;
                                double rentForStayedDays = rentPerDay * noOfDaysStayed;
                                oldRentAmounts.set(rentForStayedDays + oldRentAmounts.get());
                            });
                        }

                        CustomersBedHistory currentBed = currentHistory
                                .stream()
                                .filter(i -> i.getEndDate() == null)
                                .findFirst()
                                .orElse(null);
                        if (currentBed != null) {
                            double fullRentAmount = currentBed.getRentAmount();
                            long totalNoOfDaysInTheMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                            Date startDate = currentBed.getStartDate();
                            if (Utils.compareWithTwoDates(currentBed.getStartDate(), billingDates.currentBillStartDate()) < 0) {
                                startDate = billingDates.currentBillStartDate();
                            }
                            long noOfDaysStayed = Utils.findNumberOfDays(startDate, currentBed.getEndDate());
                            double rentPerDay = fullRentAmount / totalNoOfDaysInTheMonth;
                            double rentForStayedDays = rentPerDay * noOfDaysStayed;
                            oldRentAmounts.set(rentForStayedDays + oldRentAmounts.get());

                        }
                    }



                    List<Integer> ebReadingsId = listElectricityForAHostel
                            .stream()
                            .map(ElectricityReadings::getId)
                            .toList();
                    List<CustomersEbHistory> listCustomerEb = customerEbHistoryService.getAllByCustomerIdAndReadingId(item.getCustomerId(), ebReadingsId);

                    Double ebAmount = listCustomerEb
                            .stream()
                            .mapToDouble(CustomersEbHistory::getAmount)
                            .sum();
                    if (ebAmount > 0) {
                        ebAmount = Utils.roundOfDouble(ebAmount);
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
                    double finalAmount = Utils.roundOfDouble(rentEbAndAmenity);

                    Customers customers = listCustomers
                            .stream()
                            .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
                            .findFirst()
                            .orElse(null);
                    if (customers != null) {
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
                                }
                                else if (suffix < 100) {
                                    prefixSuffix.append("0");
                                    prefixSuffix.append(suffix);
                                }
                                else {
                                    prefixSuffix.append(suffix);
                                }
                            }
                        }
                        else {
                            //this is going to be the first invoice
                            prefixSuffix.append("-");
                            prefixSuffix.append("001");
                        }


                        InvoicesV1 invoicesV1 = new InvoicesV1();
                        invoicesV1.setCancelled(false);
                        invoicesV1.setCustomerId(item.getCustomerId());
                        invoicesV1.setCustomerMailId(customers.getEmailId());
                        invoicesV1.setCustomerMobile(customers.getMobile());
                        invoicesV1.setHostelId(postpaidRecurringEvents.getHostelId());
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
                        invoicesV1.setInvoiceDueDate(billingDates.dueDate());
                        invoicesV1.setInvoiceStartDate(invoiceStartDate.get());
                        invoicesV1.setInvoiceEndDate(billingDates.currentBillEndDate());
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

                        List<CustomerWalletHistory> wh = listCustomerWallets
                                .stream()
                                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                                .toList();
                        if (!wh.isEmpty()) {
                            wh.forEach(it -> {
                                InvoiceItems itms = new InvoiceItems();
                                if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                                    itms.setInvoiceItem(it.getSourceType());
                                }
                                else if (it.getSourceType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                                    itms.setInvoiceItem(it.getSourceType());
                                }
                                else {
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



                });

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
        recurringTrackerService.markAsInvoiceGenerated(hostelV1.getHostelId());
        notificationService.addAdminNotificationsForRecurringInvoice(hostelV1.getHostelId());
    }

    public Double calculateRentAmount(Date joiningDate, Double totalRent, BillingDates billingDates) {
        long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
        long noOfDaysStayed = Utils.findNumberOfDays(joiningDate, billingDates.currentBillEndDate());

        double rentPerday = totalRent / noOfDaysInCurrentMonth;

        return noOfDaysStayed * rentPerday;
    }
}
