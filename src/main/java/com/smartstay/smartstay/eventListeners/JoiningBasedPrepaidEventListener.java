package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.events.JoiningBasedPrepaidEvents;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.services.CustomerRecurringTracker;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JoiningBasedPrepaidEventListener {

    private static final String DEFAULT_INVOICE_PREFIX = "INV";

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
    @Autowired
    private CustomerRecurringTracker customerRecurringTrackerService;
    @Autowired
    private RecurringConfigService recurringConfigService;
    @Autowired
    private InvoiceDraftsService invoiceDraftsService;

    @Async
    @EventListener
    public void RecurringSetupForJoiningBasedPrepaid(JoiningBasedPrepaidEvents jbpe) {
        String customerId = jbpe.getCustomerId();

        // Check the booking first so we skip the remaining lookups for customers who are not checked in.
        BookingsV1 bookingsV1 = bookingsService.getBookingInfoByCustomerId(customerId);
        if (bookingsV1 == null || !isActiveStay(bookingsV1.getCurrentStatus())) {
            return;
        }

        Date now = new Date();
        HostelV1 hostelV1 = hostelService.getHostelInfo(jbpe.getHostelId());
        Customers customers = customersService.getCustomerInformation(customerId);
        BillingDates billingDates = hostelService.getBillingRuleOnDate(jbpe.getHostelId(), now);

        List<AmenitiesV1> listAmenities = amenitiesService.getAllAmenitiesByHostelId(hostelV1.getHostelId());
        List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService.getAllInvoiceNotGeneratedWallets(customerId);
        List<CustomersAmenity> listCustomersAmenity = amenitiesService.getAllCustomerAmenitiesForRecurring(customerId, now);

        double rentAmount = bookingsV1.getRentAmount();
        double amenityAmount = listCustomersAmenity == null ? 0.0 : listCustomersAmenity
                .stream()
                .mapToDouble(CustomersAmenity::getAmenityPrice)
                .sum();
        double ebAmount = flatRateEbAmount(hostelV1.getElectricityConfig());

        RecurringConfiguration recurringConfiguration = recurringConfigService.getRecurringConfig(hostelV1.getHostelId());
        boolean shouldVerify = recurringConfiguration != null && Boolean.TRUE.equals(recurringConfiguration.getShouldVerify());

        if (!shouldVerify) {
            double finalAmount = rentAmount + ebAmount + amenityAmount + walletAmount(customers);
            generateInvoice(customers, hostelV1, billingDates, now, finalAmount, rentAmount, ebAmount,
                    listCustomersAmenity, listAmenities, listCustomerWallets);
        } else {
            generateInvoiceForVerification(customers, billingDates.currentBillStartDate(),
                    billingDates.currentBillEndDate(),
                    hostelV1.getCreatedBy(),
                    billingDates.dueDays(),
                    rentAmount,
                    ebAmount,
                    amenityAmount,
                    listCustomersAmenity,
                    listAmenities,
                    listCustomerWallets);
        }

        customerRecurringTrackerService.addToTracker(customerId, hostelV1.getHostelId(), now);
    }

    private void generateInvoice(Customers customers,
                                 HostelV1 hostelV1,
                                 BillingDates billingDates,
                                 Date now,
                                 double finalAmount,
                                 double rentAmount,
                                 double ebAmount,
                                 List<CustomersAmenity> listCustomersAmenity,
                                 List<AmenitiesV1> listAmenities,
                                 List<CustomerWalletHistory> listCustomerWallets) {
        Date dueDate = Utils.addDaysToDate(now, billingDates.dueDays() - 1);
        Date invoiceEndDate = Utils.findLastDate(Utils.dateToDate(now), now);

        InvoicesV1 invoicesV1 = new InvoicesV1();
        invoicesV1.setCancelled(false);
        invoicesV1.setCustomerId(customers.getCustomerId());
        invoicesV1.setCustomerMailId(customers.getEmailId());
        invoicesV1.setCustomerMobile(customers.getMobile());
        invoicesV1.setHostelId(hostelV1.getHostelId());
        invoicesV1.setInvoiceNumber(nextInvoiceNumber(customers.getHostelId(), hostelV1.getHostelId()));
        invoicesV1.setInvoiceType(InvoiceType.RENT.name());
        invoicesV1.setBasePrice(finalAmount);
        invoicesV1.setTotalAmount(Utils.roundOfDouble(finalAmount));
        invoicesV1.setInvoiceAmount(Utils.roundOffWithTwoDigit(rentAmount));
        invoicesV1.setPayableAmount(Utils.roundOfDouble(ebAmount));
        invoicesV1.setPaidAmount(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGst(0.0);
        invoicesV1.setGstPercentile(0.0);
        invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
        invoicesV1.setOthersDescription(null);
        invoicesV1.setInvoiceMode(InvoiceMode.RECURRING.name());
        invoicesV1.setCreatedBy(hostelV1.getCreatedBy());
        invoicesV1.setInvoiceGeneratedDate(now);
        invoicesV1.setInvoiceDate(now);
        invoicesV1.setInvoiceDueDate(dueDate);
        invoicesV1.setInvoiceStartDate(now);
        invoicesV1.setInvoiceEndDate(invoiceEndDate);
        invoicesV1.setCreatedAt(now);

        List<InvoiceItems> invoicesItems = new ArrayList<>();
        if (rentAmount > 0) {
            invoicesItems.add(invoiceItem(invoicesV1, com.smartstay.smartstay.ennum.InvoiceItems.RENT.name(), null, rentAmount));
        }
        if (ebAmount > 0) {
            invoicesItems.add(invoiceItem(invoicesV1, com.smartstay.smartstay.ennum.InvoiceItems.EB.name(), null, ebAmount));
        }
        if (listCustomersAmenity != null) {
            Map<String, AmenitiesV1> amenitiesById = indexAmenities(listAmenities);
            listCustomersAmenity.forEach(amenity -> {
                AmenitiesV1 amenitiesV1 = findAmenity(amenitiesById, amenity.getAmenityId());
                if (amenitiesV1 != null) {
                    invoicesItems.add(invoiceItem(invoicesV1,
                            com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name(),
                            amenitiesV1.getAmenityName(),
                            Utils.roundOffWithTwoDigit(amenity.getAmenityPrice())));
                }
            });
        }
        listCustomerWallets.forEach(it -> invoicesItems.add(invoiceItem(invoicesV1,
                walletItemType(it.getSourceType()),
                walletOtherItem(it.getSourceType()),
                it.getAmount())));

        invoicesV1.setInvoiceItems(invoicesItems);
        invoicesV1Repository.save(invoicesV1);

        clearWalletAndMarkInvoiced(customers, listCustomerWallets);
    }

    private void generateInvoiceForVerification(Customers customers,
                                                Date invoiceStartDate,
                                                Date invoiceEndDate,
                                                String createdBy,
                                                Integer dueDays,
                                                Double rentAmount,
                                                Double ebAmount,
                                                Double amenityAmount,
                                                List<CustomersAmenity> listCustomerAmenities,
                                                List<AmenitiesV1> listAmenities,
                                                List<CustomerWalletHistory> listCustomerWallets) {
        double finalAmount = walletAmount(customers) + rentAmount + ebAmount + amenityAmount;
        int dDays = dueDays != null ? dueDays : 0;
        Date dueDate = Utils.addDaysToDate(invoiceStartDate, dDays - 1);
        Date now = new Date();

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
        invoiceDrafts.setInvoiceGeneratedDate(now);
        invoiceDrafts.setCancelledDate(null);
        invoiceDrafts.setInvoiceDueDate(dueDate);
        invoiceDrafts.setInvoiceDate(now);
        invoiceDrafts.setInvoiceStartDate(invoiceStartDate);
        invoiceDrafts.setInvoiceEndDate(invoiceEndDate);
        invoiceDrafts.setCreatedAt(now);
        invoiceDrafts.setUpdatedAt(now);

        List<DraftItems> listDraftedItem = new ArrayList<>();
        if (rentAmount > 0) {
            listDraftedItem.add(draftItem(invoiceDrafts, com.smartstay.smartstay.ennum.InvoiceItems.RENT.name(), null, rentAmount));
        }
        if (ebAmount > 0) {
            listDraftedItem.add(draftItem(invoiceDrafts, com.smartstay.smartstay.ennum.InvoiceItems.EB.name(), null, ebAmount));
        }
        if (listCustomerAmenities != null) {
            Map<String, AmenitiesV1> amenitiesById = indexAmenities(listAmenities);
            listCustomerAmenities.forEach(amenity -> {
                AmenitiesV1 amenitiesV1 = findAmenity(amenitiesById, amenity.getAmenityId());
                if (amenitiesV1 != null) {
                    listDraftedItem.add(draftItem(invoiceDrafts,
                            com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name(),
                            amenitiesV1.getAmenityName(),
                            Utils.roundOffWithTwoDigit(amenity.getAmenityPrice())));
                }
            });
        }
        // listCustomerWallets is already scoped to this customer by the repository query
        listCustomerWallets.forEach(it -> listDraftedItem.add(draftItem(invoiceDrafts,
                walletItemType(it.getSourceType()),
                walletOtherItem(it.getSourceType()),
                Utils.roundOffWithTwoDigit(it.getAmount()))));

        invoiceDrafts.setListItems(listDraftedItem);
        invoiceDraftsService.saveFromRecurring(invoiceDrafts);

        clearWalletAndMarkInvoiced(customers, listCustomerWallets);
        // Tracker entry is added once by the caller for both paths.
    }

    private static boolean isActiveStay(String bookingStatus) {
        return BookingStatus.CHECKIN.name().equalsIgnoreCase(bookingStatus)
                || BookingStatus.NOTICE.name().equalsIgnoreCase(bookingStatus);
    }

    private static double flatRateEbAmount(ElectricityConfig ebConfig) {
        if (ebConfig != null
                && EBReadingType.FLAT_RATE.name().equalsIgnoreCase(ebConfig.getTypeOfReading())
                && !ebConfig.isShouldIncludeInRent()) {
            return ebConfig.getFlatCharge();
        }
        return 0.0;
    }

    private static double walletAmount(Customers customers) {
        CustomerWallet wallet = customers.getWallet();
        return wallet != null && wallet.getAmount() != null ? wallet.getAmount() : 0.0;
    }

    private String nextInvoiceNumber(String customerHostelId, String hostelId) {
        String prefix = DEFAULT_INVOICE_PREFIX;
        BillTemplates templates = templatesService.getTemplateByHostelId(customerHostelId);
        if (templates != null && templates.getTemplateTypes() != null) {
            prefix = templates.getTemplateTypes()
                    .stream()
                    .filter(i -> BillConfigTypes.RENTAL.name().equalsIgnoreCase(i.getInvoiceType()))
                    .map(BillTemplateType::getInvoicePrefix)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(DEFAULT_INVOICE_PREFIX);
        }

        InvoicesV1 latest = invoicesV1Repository.findLatestInvoiceByPrefix(prefix, hostelId);
        if (latest == null) {
            return prefix + "-001";
        }
        String[] prefArr = latest.getInvoiceNumber().split("-");
        if (prefArr.length > 1) {
            int suffix = Integer.parseInt(prefArr[prefArr.length - 1]) + 1;
            return prefix + "-" + String.format("%03d", suffix);
        }
        return prefix;
    }

    private void clearWalletAndMarkInvoiced(Customers customers, List<CustomerWalletHistory> wallets) {
        CustomerWallet wallet = customers.getWallet();
        if (wallet != null) {
            wallet.setAmount(0.0);
        }
        customersService.updateCustomersFromRecurring(customers);

        if (!wallets.isEmpty()) {
            wallets.forEach(w -> w.setBillingStatus(WalletBillingStatus.INVOICE_GENERATED.name()));
            customerWalletHistoryService.saveAll(wallets);
        }
    }

    private static Map<String, AmenitiesV1> indexAmenities(List<AmenitiesV1> listAmenities) {
        if (listAmenities == null) {
            return Map.of();
        }
        return listAmenities
                .stream()
                .filter(a -> a.getAmenityId() != null)
                .collect(Collectors.toMap(a -> a.getAmenityId().toLowerCase(), Function.identity(), (a, b) -> a));
    }

    private static AmenitiesV1 findAmenity(Map<String, AmenitiesV1> amenitiesById, String amenityId) {
        return amenityId == null ? null : amenitiesById.get(amenityId.toLowerCase());
    }

    private static String walletItemType(String sourceType) {
        if (com.smartstay.smartstay.ennum.InvoiceItems.EB.name().equalsIgnoreCase(sourceType)
                || com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name().equalsIgnoreCase(sourceType)) {
            return sourceType;
        }
        return com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name();
    }

    private static String walletOtherItem(String sourceType) {
        return com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name().equals(walletItemType(sourceType)) ? sourceType : null;
    }

    private static InvoiceItems invoiceItem(InvoicesV1 invoice, String type, String otherItem, Double amount) {
        InvoiceItems item = new InvoiceItems();
        item.setInvoiceItem(type);
        item.setOtherItem(otherItem);
        item.setAmount(amount);
        item.setInvoice(invoice);
        return item;
    }

    private static DraftItems draftItem(InvoiceDrafts drafts, String type, String otherItem, Double amount) {
        DraftItems item = new DraftItems();
        item.setInvoiceItem(type);
        item.setOtherItem(otherItem);
        item.setAmount(amount);
        item.setInvoiceDrafts(drafts);
        return item;
    }
}
