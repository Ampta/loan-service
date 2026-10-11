
package com.ampta.utils;

import com.ampta.entity.LoanDeal;
import com.ampta.entity.SanctionLetter;
import com.ampta.entity.User;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class SanctionLetterPdfGenerator {

    @Value("${app.sanction-letter.directory:uploads/sanction-letters}")
    private String directory;

    public String generate(
            SanctionLetter letter,
            User customer) throws IOException {

        Path folder = Paths.get(directory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(folder);

        String fileName = letter.getSanctionLetterNo() + ".pdf";
        Path filePath = folder.resolve(fileName).normalize();

        if (!filePath.startsWith(folder)) {
            throw new IOException("Invalid sanction letter file path");
        }

        LoanDeal loan = letter.getLoanDeal();

        String html = buildHtml(letter, customer, loan);

        try (var outputStream = Files.newOutputStream(filePath)) {
            HtmlConverter.convertToPdf(html, outputStream);
        }

        return filePath.toString();
    }

    private String buildHtml(
            SanctionLetter letter,
            User customer,
            LoanDeal loan) {

        String customerName =
                safe(customer.getFirstName()) + " " +
                        safe(customer.getLastName());

        String accountNumber =
                maskAccountNumber(loan.getBankAccountNumber());

        String sanctionDate = letter.getSanctionDate() == null
                ? "N/A"
                : letter.getSanctionDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        String loanType = loan.getLoanType() == null
                ? "N/A"
                : loan.getLoanType().name();

        String status = letter.getStatus() == null
                ? "N/A"
                : letter.getStatus().name();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 35px 40px;
                        }

                        body {
                            font-family: Arial, sans-serif;
                            font-size: 10pt;
                            color: #263238;
                        }

                        .header {
                            text-align: center;
                            border-bottom: 3px solid #174a7e;
                            padding-bottom: 12px;
                            margin-bottom: 20px;
                        }

                        .header h1 {
                            color: #174a7e;
                            font-size: 21pt;
                            margin-bottom: 5px;
                        }

                        .subtitle {
                            color: #546e7a;
                            font-size: 9pt;
                        }

                        .date {
                            text-align: right;
                            margin-bottom: 20px;
                        }

                        h2 {
                            color: #174a7e;
                            font-size: 12pt;
                            margin-top: 20px;
                            margin-bottom: 8px;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-bottom: 14px;
                        }

                        td, th {
                            border: 1px solid #cbd5e1;
                            padding: 8px;
                            text-align: left;
                        }

                        td:first-child {
                            width: 38%%;
                            font-weight: bold;
                            background-color: #f1f5f9;
                        }

                        .amount {
                            color: #174a7e;
                            font-size: 12pt;
                            font-weight: bold;
                        }

                        .notice {
                            border: 1px solid #cbd5e1;
                            padding: 12px;
                            background-color: #f8fafc;
                        }

                        .footer {
                            margin-top: 25px;
                            border-top: 1px solid #cbd5e1;
                            padding-top: 10px;
                            font-size: 8pt;
                            color: #64748b;
                        }
                    </style>
                </head>
                <body>

                    <div class="header">
                        <h1>LOAN SANCTION LETTER</h1>
                        <div class="subtitle">
                            Loan Approval and Sanction Details
                        </div>
                    </div>

                    <div class="date">
                        <b>Sanction Date:</b> %s
                    </div>

                    <p>Dear %s,</p>

                    <p>
                        We are pleased to inform you that your loan
                        application has been sanctioned subject to the
                        applicable terms and conditions mentioned below.
                    </p>

                    <h2>1. Customer Information</h2>

                    <table>
                        <tr>
                            <td>Customer Name</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Customer Email</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Mobile Number</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Customer ID</td>
                            <td>%s</td>
                        </tr>
                    </table>

                    <h2>2. Loan Details</h2>

                    <table>
                        <tr>
                            <td>Sanction Letter Number</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Application / Deal ID</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Loan Type</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Loan Amount Requested</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Sanctioned Amount</td>
                            <td class="amount">%s</td>
                        </tr>
                        <tr>
                            <td>Interest Rate</td>
                            <td>%s%% per annum</td>
                        </tr>
                        <tr>
                            <td>Loan Tenure</td>
                            <td>%s months</td>
                        </tr>
                        <tr>
                            <td>Monthly EMI</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>EMI Due Day</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Sanction Status</td>
                            <td>%s</td>
                        </tr>
                    </table>

                    <h2>3. Bank Details</h2>

                    <table>
                        <tr>
                            <td>Bank Name</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>Account Number</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>IFSC Code</td>
                            <td>%s</td>
                        </tr>
                    </table>

                    <h2>4. Terms and Conditions</h2>

                    <div class="notice">
                        <p>
                            1. The loan is subject to the lender's applicable
                            terms, conditions and loan agreement.
                        </p>
                        <p>
                            2. Repayment must be made according to the
                            agreed repayment schedule.
                        </p>
                        <p>
                            3. Any applicable fees, charges and penalties
                            will be governed by the signed loan agreement.
                        </p>
                        <p>
                            4. Disbursement is subject to completion of
                            applicable verification and documentation.
                        </p>
                        <p>
                            <b>Remarks:</b> %s
                        </p>
                    </div>

                    <p>
                        Please retain this letter for your records.
                        Contact the loan department if you require
                        clarification.
                    </p>

                    <br/>

                    <p><b>Authorized Loan Department</b></p>
                    <p>Loan Management System</p>

                    <div class="footer">
                        This document is system-generated.
                        Please refer to the executed loan agreement
                        for the binding terms and conditions.
                    </div>

                </body>
                </html>
                """.formatted(
                escape(sanctionDate),
                escape(customerName),
                escape(customerName),
                escape(customer.getEmail()),
                escape(customer.getPhoneNumber()),
                escape(String.valueOf(customer.getUserId())),
                escape(letter.getSanctionLetterNo()),
                escape(String.valueOf(loan.getDealId())),
                escape(loanType),
                money(loan.getLoanAmount()),
                money(letter.getSanctionedAmount()),
                number(letter.getInterestRate()),
                escape(String.valueOf(letter.getTenureMonths())),
                money(letter.getEmiAmount()),
                escape(String.valueOf(loan.getEmiDay())),
                escape(status),
                escape(loan.getBankName()),
                escape(accountNumber),
                escape(loan.getIfscCode()),
                escape(letter.getRemarks())
        );
    }

    private String money(Double amount) {
        if (amount == null) {
            return "N/A";
        }

        NumberFormat format =
                NumberFormat.getNumberInstance(Locale.US);

        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        return "INR " + format.format(amount);
    }

    private String number(Double value) {
        return value == null
                ? "N/A"
                : String.format(Locale.US, "%.2f", value);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "N/A";
        }

        String value = accountNumber.trim();

        if (value.length() <= 4) {
            return "****";
        }

        return "****" + value.substring(value.length() - 4);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        if (value == null) {
            return "N/A";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
