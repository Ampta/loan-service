package com.ampta.scheduler;

import com.ampta.entity.EmiSchedule;
import com.ampta.entity.PenaltyCharge;
import com.ampta.repository.EmiScheduleRepository;
import com.ampta.repository.PenaltyChargeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiPenaltyScheduler {

    private final EmiScheduleRepository emiScheduleRepository;
    private final PenaltyChargeRepository penaltyChargeRepository;

    @Value("${penalty.bounce-charge:600.00}")
    private Double bounceCharge;

    @Value("${penalty.daily-rate:1.00}")
    private Double dailyPenaltyRate;

    /*
     * Testing cron: every minute -> 0 * * * * *
     * Production cron: daily at 12:05 AM -> 0 5 0 * * *
     */
    @Scheduled(cron = "${penalty.scheduler.cron:0 5 0 * * *}")
    @Transactional
    public void calculateOverdueEmiPenalties() {

        LocalDate today = LocalDate.now();
        log.info("EMI penalty scheduler started. Date: {}", today);

        List<String> statuses = Arrays.asList("PENDING", "BOUNCED");

        List<EmiSchedule> overdueEmis =
                emiScheduleRepository
                        .findByPaymentStatusInAndDueDateBefore(statuses, today);

        log.info("Overdue EMIs found: {}", overdueEmis.size());

        if (overdueEmis.isEmpty()) {
            logDiagnostics(today);   // TEMPORARY - remove once the cause is fixed
            log.info("EMI penalty scheduler completed.");
            return;
        }

        for (EmiSchedule emi : overdueEmis) {
            try {
                LocalDate dueDate = emi.getDueDate();
                if (dueDate == null || !dueDate.isBefore(today)) {
                    continue;
                }

                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                if (overdueDays <= 0) {
                    continue;
                }

                String status = emi.getPaymentStatus() == null
                        ? "" : emi.getPaymentStatus().trim();

                boolean bounced = "BOUNCED".equalsIgnoreCase(status);

                Optional<PenaltyCharge> existingPenalty =
                        penaltyChargeRepository
                                .findByEmiSchedule_EmiScheduleId(emi.getEmiScheduleId());

                // Bounce charge applies only to bounced EMIs.
                // Due date cross hone par ₹600 charge apply hoga,
               // chahe EMI status PENDING ho ya BOUNCED.
                Double currentBounceCharge = bounceCharge;

                // Preserve a previously recorded bounce charge
                // (e.g. EMI was BOUNCED earlier, now back to PENDING).
                if (existingPenalty.isPresent()
                        && existingPenalty.get().getBounceCharge() != null
                        && existingPenalty.get().getBounceCharge() > 0.0) {
                    currentBounceCharge = existingPenalty.get().getBounceCharge();
                }

                // Daily penalty = overdue days * daily rate (recomputed from
                // scratch each run, so reruns never double-count).
                Double dailyPenalty = round2(dailyPenaltyRate * overdueDays);

                // Penalty stays separate from the EMI amount.
                Double totalPenalty = round2(currentBounceCharge + dailyPenalty);


                PenaltyCharge penalty;
                if (existingPenalty.isPresent()) {
                    penalty = existingPenalty.get();
                } else {
                    penalty = new PenaltyCharge();
                    penalty.setEmiSchedule(emi);
                    penalty.setLoanAccount(emi.getLoanAccount());
                }

                penalty.setBounceCharge(currentBounceCharge);
                penalty.setDailyPenalty(dailyPenalty);
                penalty.setOverdueDays((int) overdueDays);
                penalty.setPenaltyAmount(totalPenalty);
                penalty.setStatus("PENDING");

                penaltyChargeRepository.saveAndFlush(penalty);

                log.info(
                        "Penalty updated. EMI ID: {}, Status: {}, Overdue days: {}, "
                                + "Bounce charge: {}, Daily penalty: {}, Total penalty: {}",
                        emi.getEmiScheduleId(), status, overdueDays,
                        currentBounceCharge, dailyPenalty, totalPenalty);

            } catch (Exception exception) {
                log.error("Failed to calculate penalty for EMI ID: {}",
                        emi.getEmiScheduleId(), exception);
            }
        }

        log.info("EMI penalty scheduler completed.");
    }

    private Double round2(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // TEMPORARY: shows exactly what JPA sees. Brackets reveal hidden whitespace.
    private void logDiagnostics(LocalDate today) {
        List<EmiSchedule> all = emiScheduleRepository.findAll();
        log.warn("DIAGNOSTIC: today={}, EMI rows visible to JPA={}", today, all.size());
        for (EmiSchedule e : all) {
            log.warn("DIAGNOSTIC: EMI ID={}, dueDate={}, paymentStatus=[{}]",
                    e.getEmiScheduleId(), e.getDueDate(), e.getPaymentStatus());
        }
    }
}