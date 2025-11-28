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

    @Async
    @EventListener
    public void OnRecurringSetup(RecurringEvents recurringEvents) {

        HostelV1 hostelV1 = hostelService.getHostelInfo(recurringEvents.getHostelId());
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(recurringEvents.getHostelId());
        Calendar calendarPreviousBillsStartDate = Calendar.getInstance();
        calendarPreviousBillsStartDate.setTime(billingDates.currentBillStartDate());
        calendarPreviousBillsStartDate.add(Calendar.MONTH, -1);

        Date calendarPreviousBillsEndDate = Utils.findLastDate(calendarPreviousBillsStartDate.get(Calendar.DAY_OF_MONTH), calendarPreviousBillsStartDate.getTime());


        List<BookingsV1> customersList = bookingsService.getAllCheckedInCustomer(recurringEvents.getHostelId());
        List<String> customerIds = customersList
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();

        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        List<ElectricityReadings> listElectricityForAHostel = electricityService.getAllElectricityReadingForRecurring(recurringEvents.getHostelId(),
                calendarPreviousBillsStartDate.getTime(),
                calendarPreviousBillsEndDate);

        customersList
                .stream()
                .forEach(item -> {
            Double rentAmount = item.getRentAmount();
            List<Integer> ebReadingsId = listElectricityForAHostel
                    .stream()
                    .filter(i -> i.getRoomId() == item.getRoomId())
                    .map(ElectricityReadings::getId)
                    .toList();
            List<CustomersEbHistory> listCustomerEb = customerEbHistoryService.getAllByCustomerIdAndReadingId(item.getCustomerId(), ebReadingsId);

            Double ebAmount = listCustomerEb
                    .stream()
                    .mapToDouble(CustomersEbHistory::getAmount)
                    .sum();

            List<CustomersAmenity> listCustomersAmenity = amenitiesService.getAllAmenitiesByCustomerId(item.getCustomerId());

            Double amenityAmount = listCustomersAmenity
                    .stream()
                    .mapToDouble(CustomersAmenity::getAmenityPrice)
                    .sum();

            double rentEbAmount = rentAmount + ebAmount;
            double rentEbAndAmenity = rentEbAmount + amenityAmount;
            System.out.println(amenityAmount);

            Customers customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
                    .findFirst().get();

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
                        prefixSuffix.append("-");
                        prefixSuffix.append(prefix);
                    }
                    InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(prefix, hostelV1.getHostelId());

                    if (inv != null) {
                        String[] prefArr = inv.getInvoiceNumber().split("-");
                        if (prefArr.length > 1) {
                            int suffix = Integer.parseInt(prefArr[1]) + 1;
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
            invoicesV1.setBasePrice(rentEbAndAmenity);
            invoicesV1.setTotalAmount(rentEbAndAmenity);
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
            invoicesV1.setInvoiceDueDate(billingDates.dueDate());
            invoicesV1.setInvoiceStartDate(billingDates.currentBillStartDate());
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


            invoicesV1.setInvoiceItems(invoicesItems);

            invoicesV1Repository.save(invoicesV1);

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

    }
}
