package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoiceDrafts;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.responses.invoiceDraft.CustomerInfo;
import com.smartstay.smartstay.responses.invoiceDraft.InvoiceInfo;
import com.smartstay.smartstay.responses.invoiceDraft.InvoiceItems;
import com.smartstay.smartstay.responses.invoiceDraft.StayInfo;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DraftListMapper implements Function<InvoiceDrafts, InvoiceInfo> {
    List<BedDetails> listBedDetails = null;
    List<Customers> listCustomers = null;
    List<BookingsV1> listBookings = null;

    public DraftListMapper(List<BedDetails> listBedDetails, List<Customers> listCustomers, List<BookingsV1> listBookings) {
        this.listBedDetails = listBedDetails;
        this.listCustomers = listCustomers;
        this.listBookings = listBookings;
    }

    @Override
    public InvoiceInfo apply(InvoiceDrafts invoiceDrafts) {
        Customers customers;
        StayInfo stayInfo = null;
        CustomerInfo customerInfo = null;
        List<InvoiceItems> invoiceItems = new ArrayList<>();
        if (listCustomers != null) {
            customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoiceDrafts.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (customers != null) {
                if (listBookings != null) {
                    BookingsV1 bookingsV1 = listBookings
                            .stream()
                            .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                            .findFirst()
                            .orElse(null);
                    if (bookingsV1 != null) {
                        if (listBedDetails != null) {
                            BedDetails bedDetails = listBedDetails
                                    .stream()
                                    .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                                    .findFirst()
                                    .orElse(null);
                            if (bedDetails != null) {
                                stayInfo = new StayInfo(bedDetails.getBedName(),
                                        bedDetails.getRoomName(),
                                        bedDetails.getFloorName());
                            }
                        }
                    }
                }
                customerInfo = new CustomerInfo(customers.getFirstName(),
                        customers.getLastName(),
                        NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                        CustomerUtils.getProfilePic(customers),
                        NameUtils.getInitials(customers.getFirstName(), customers.getLastName()),
                        customers.getCustomerId(),
                        customers.getMobile(),
                        "91");

            }

        } else {
            customers = null;
        }

        if (invoiceDrafts.getListItems() != null) {
           invoiceItems = invoiceDrafts
                   .getListItems()
                   .stream()
                   .map(i -> {
                       String itemName = null;
                       if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name())) {
                           itemName = i.getOtherItem();
                       }
                       else {
                           itemName = i.getInvoiceItem();
                       }
                       return new InvoiceItems(itemName, Utils.roundOffWithTwoDigit(i.getAmount()), i.getDraftItemId());
                   })
                   .toList();
        }
        return new InvoiceInfo(invoiceDrafts.getDraftId(),
                Utils.roundOfDouble( invoiceDrafts.getTotalAmount()),
                Utils.dateToString(invoiceDrafts.getInvoiceStartDate()),
                Utils.dateToString(invoiceDrafts.getInvoiceEndDate()),
                invoiceDrafts.isEdited(),
                invoiceItems,
                customerInfo,
                stayInfo);
    }
}
