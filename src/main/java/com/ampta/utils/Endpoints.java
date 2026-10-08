package com.ampta.utils;

public class Endpoints {

    // Prevent instantiation
    private Endpoints() {}

    // SHIVAM: Auth, Score card, Eligibility, Notification, Ticket
    public static final String V1_AUTH = "/api/v1/auth";
    public static final String V1_SCORE_CARD = "/api/v1/score-cards";
    public static final String V1_ELIGIBILITY = "/api/v1/eligibility";
    public static final String V1_NOTIFICATION = "/api/v1/notifications";
    public static final String V1_TICKET = "/api/v1/tickets";

    // PRINCE: Wallet, Payment, Bounce, Penalty, Scheduler
    public static final String V1_WALLET = "/api/v1/wallets";
    public static final String V1_PAYMENT = "/api/v1/payments";
    public static final String V1_BOUNCE = "/api/v1/bounces";
    public static final String V1_PENALTY = "/api/v1/penalties";
    public static final String V1_SCHEDULER = "/api/v1/schedulers";

    // ANUPAMA: KYC, Document, Cibil
    public static final String V1_KYC = "/api/v1/kyc";
    public static final String V1_DOCUMENT = "/api/v1/documents";
    public static final String V1_CIBIL = "/api/v1/cibils";

    // MANISH: Loan creation, Sanction letter, Disbursement
    public static final String V1_LOAN = "/api/v1/loans";
    public static final String V1_SANCTION_LETTER = "/api/v1/sanction-letters";
    public static final String V1_DISBURSEMENT = "/api/v1/disbursements";

    // FARAZ: Foreclosure, Closure
    public static final String V1_FORECLOSURE = "/api/v1/foreclosures";
    public static final String V1_CLOSURE = "/api/v1/closures";
}
