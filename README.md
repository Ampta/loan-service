# Ampta Loan Service

A Spring Boot microservice for comprehensive loan lifecycle management, including customer onboarding, KYC verification, CIBIL reporting, loan deals, EMI scheduling, repayments, penalties, and loan closure.

## Documentation Quick Links

- 📘 **[Master Team Development & Project Setup Guide](docs/TEAM_DEVELOPMENT_GUIDE.md)**
  - Building the project from scratch (Maven, Spring Boot, MySQL, Mail, JWT)
  - Security & Authentication architecture (JWT, RBAC, Dev Mode vs. Prod Mode)
  - Reference Module: **Loan Closure & Foreclosure** (Complete Blueprint)
  - Developer Guide for Team Members (Folder structure, Step-by-step recipe, Git workflow)
- 📗 **[REST API Endpoint Conventions & Standards](docs/API_ENDPOINT_CONVENTIONS.md)**
  - RESTful rules, URI naming conventions, HTTP methods, and module route audit

## Team Ownership Matrix

| Module | Owner | Base Route Constant |
| :--- | :--- | :--- |
| **Foreclosure & Closure (Demo/Reference)** | FARAZ | `Endpoints.V1_FORECLOSURE`, `Endpoints.V1_CLOSURE` |
| **Loan Deals, Sanction Letters, Disbursements** | MANISH | `Endpoints.V1_LOAN`, `Endpoints.V1_SANCTION_LETTER`, `Endpoints.V1_DISBURSEMENT` |
| **Wallets, Payments, Bounces, Penalties, EMI Scheduler** | PRINCE | `Endpoints.V1_WALLET`, `Endpoints.V1_PAYMENT`, `Endpoints.V1_BOUNCE`, `Endpoints.V1_PENALTY`, `Endpoints.V1_SCHEDULER` |
| **KYC Documents, Document Management, CIBIL** | ANUPAMA | `Endpoints.V1_KYC`, `Endpoints.V1_DOCUMENT`, `Endpoints.V1_CIBIL` |
| **Auth, 2FA, Score Cards, Eligibility, Notifications** | SHIVAM | `Endpoints.V1_AUTH`, `Endpoints.V1_SCORE_CARD`, `Endpoints.V1_ELIGIBILITY`, `Endpoints.V1_NOTIFICATION` |

## Quick Start

### 1. Backend (Spring Boot)
Ensure Java 21+ and MySQL are installed.
```bash
# Compile and build
./mvnw clean package -DskipTests

# Run application
./mvnw spring-boot:run
```
The server starts on port `8080`.

### 2. Frontend (`loan-closure-angular`)
Frontend Angular application for loan closure and foreclosure demo:
```bash
cd loan-closure-angular
npm ci
npm start
```
Access at `http://localhost:4200`. API calls are automatically proxied to `http://localhost:8080/api`.
