
package com.ampta.dto.response;

import lombok.Data;

@Data
public class EmiPaymentSummaryResponse {

        private Long loanAccountId;
        private Long emiScheduleId;

        private Double emiAmount;

        // Penalty breakdown
        private Double bounceCharge;
        private Double dailyPenalty;
        private Integer overdueDays;

        // Total penalty and payable amount
        private Double penaltyAmount;
        private Double totalPayable;

        private String emiStatus;
        private String penaltyStatus;
}
