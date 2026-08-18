package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

    /**
     * need a production push
     *
     * @param filterOptionsRepositories
     * @return
     */
//    @Bean
//    public CommandLineRunner updateAdvanceInvoiceHeaders(FilterOptionsRepositories filterOptionsRepositories) {
//        return args -> {
//            FilterOptions bookingFilterOptions = filterOptionsRepositories.findBookingsFilterOptions();
//            List<ColumnFilters> listColumnFilters = bookingFilterOptions
//                    .getFilterOptions();
//            ColumnFilters columnFilters = new ColumnFilters();
//            columnFilters.setFieldName("Invoice Date");
//            columnFilters.setSelected(true);
//            columnFilters.setOrder(13);
//
//            listColumnFilters.add(columnFilters);
//
//            filterOptionsRepositories.save(bookingFilterOptions);
//        };
//    }

    /**
     * need production push
     *
     * @param tableColumnsRepositories
     * @return
     */
//    @Bean
//    public CommandLineRunner updateInvoiceDateOnTableCOlumns(TableColumnsRepositories tableColumnsRepositories) {
//        return args -> {
//            List<TableColumns> lisTableColumns = tableColumnsRepositories.findByModuleName(FilterOptionsModule.MODULE_BOOKINGS.name());
//            if (lisTableColumns != null) {
//                List<TableColumns> listNewTableColumns = lisTableColumns
//                        .stream()
//                        .map(i -> {
//                            if (i.getColumns() != null) {
//                                ColumnFilters columnFilters = new ColumnFilters();
//                                columnFilters.setFieldName("Invoice Date");
//                                columnFilters.setSelected(true);
//                                columnFilters.setOrder(13);
//                                List<ColumnFilters> columnFiltersList = i.getColumns();
//                                columnFiltersList.add(columnFilters);
//
//                                i.setColumns(columnFiltersList);
//                            }
//                            return i;
//                        })
//                        .toList();
//
//                tableColumnsRepositories.saveAll(listNewTableColumns);
//            }
//        };
//    }

    /**
     *
     * required priduction run
     *
     * @param invoicesV1Repository
     * @return
     */
//    @Bean
//    public CommandLineRunner mapOldCancelledInvoicesToNewCancelledInvoice(InvoicesV1Repository invoicesV1Repository) {
//        return args -> {
//            List<InvoicesV1> listCancelledInvoices = invoicesV1Repository.findAllSettlementInvoices();
//            if (listCancelledInvoices != null) {
//                List<String> invoiceIds = new ArrayList<>();
//
//                listCancelledInvoices
//                        .stream()
//                        .filter(i -> i.getCancelledInvoices() != null && !i.getCancelledInvoices().isEmpty())
//                        .forEach(item -> {
//                            List<String> currentCancelledInvoiceIds = item
//                                    .getCancelledInvoices()
//                                    .stream().toList();
//
//                           List<InvoicesV1> listInvoices = invoicesV1Repository.findAllById(currentCancelledInvoiceIds);
//                           List<CancelledInvoice> listCancelledInvoicesWithUpdatedInfo = listInvoices
//                                   .stream()
//                                   .map(i -> {
//                                       CancelledInvoice cancelledInvoice = new CancelledInvoice();
//                                       cancelledInvoice.setInvoiceId(i.getInvoiceId());
//                                       cancelledInvoice.setPaymentStatus(i.getPaymentStatus());
//
//                                       return cancelledInvoice;
//                                   })
//                                   .toList();
//                           item.setNewCancelledInvoices(listCancelledInvoicesWithUpdatedInfo);
//
//                           invoicesV1Repository.save(item);
//
//                           List<InvoicesV1> listInvoiceWithNewUpdateCancelled = listInvoices
//                                   .stream()
//                                   .map(i -> {
//                                       i.setPaymentStatus(PaymentStatus.CANCELLED.name());
//                                       return i;
//                                   })
//                                   .toList();
//
//                           invoicesV1Repository.saveAll(listInvoiceWithNewUpdateCancelled);
//
//                        });
//
//            }
//        };
//    }

}