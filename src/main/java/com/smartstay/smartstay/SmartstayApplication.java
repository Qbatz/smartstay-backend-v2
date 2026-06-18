package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
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

    private static final Logger log = LoggerFactory.getLogger(SmartstayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

    /**
     * One-time, idempotent backfill of the denormalized vendor financial summary
     * ({@code total_expense}, {@code total_paid}, {@code balance}, {@code payment_status}) for
     * vendors created before these columns existed. Runs on startup/deploy.
     *
     * <p>Gated by {@code vendor.financials.backfill.enabled} (default {@code true}); set it to
     * {@code false} once the backfill has run to skip the cost on subsequent boots. Failures are
     * logged and swallowed so they can never block startup.
     */
    @Bean
    public CommandLineRunner vendorFinancialsBackfill(JdbcTemplate jdbcTemplate,
            @Value("${vendor.financials.backfill.enabled:true}") boolean enabled) {
        return args -> {
            if (!enabled) {
                log.info("Vendor financials backfill skipped (vendor.financials.backfill.enabled=false).");
                return;
            }
            try {
                int rows = jdbcTemplate.update("""
                        UPDATE vendorv1 v SET
                          total_expense = COALESCE((SELECT SUM(e.total_price) FROM expensesv1 e
                                                    WHERE e.vendor_id = CAST(v.vendor_id AS CHAR) AND e.is_active = true), 0),
                          total_paid    = COALESCE((SELECT SUM(p.paid_amount) FROM expense_payments p
                                                    WHERE p.vendor_id = CAST(v.vendor_id AS CHAR)), 0)
                        """);

                jdbcTemplate.update("UPDATE vendorv1 SET balance = total_expense - total_paid");

                jdbcTemplate.update("""
                        UPDATE vendorv1 SET payment_status =
                          CASE WHEN total_expense <= 0 THEN 'NO_TRANSACTION'
                               WHEN balance <= 0 AND total_paid > 0 THEN 'FULLY_SETTLED'
                               WHEN total_paid <= 0 THEN 'NOT_PAID'
                               ELSE 'PARTIALLY_PAID' END
                        """);

                log.info("Vendor financials backfill completed ({} vendor rows synced).", rows);
            } catch (Exception ex) {
                log.error("Vendor financials backfill failed; continuing startup.", ex);
            }
        };
    }

}