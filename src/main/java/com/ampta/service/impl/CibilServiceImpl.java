package com.ampta.service.impl;

import com.ampta.config.ModelMapperConfig;
import com.ampta.dto.response.CibilScoreResponse;
import com.ampta.entity.CibilReport;
import com.ampta.entity.Customer;
import com.ampta.entity.User;
import com.ampta.entity.enums.CibilStatus;
import com.ampta.entity.enums.EmployeeType;
import com.ampta.entity.enums.Role;
import com.ampta.exception.CibilReportNotFoundException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.CibilService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CibilServiceImpl implements CibilService {

    private final CibilReportRepository cibilReportRepository;
    private final UserRepository userRepository;
    private final ModelMapperConfig modelMapperConfig;

    private final CustomerRepository customerRepository;

    private static final Pattern PAN_PATTERN =
            Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

    //    fetch the customer from database
    private Customer fetchCustomer(Long customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId
                ));
    }

    // Calculate score based on monthly income
    private int calculateIncomeScore(Double monthlyEarning) {

        if (monthlyEarning < 25000) {
            return 100;
        }

        if (monthlyEarning <= 50000) {
            return 200;
        }

        if (monthlyEarning > 50000 && monthlyEarning < 100000) {
            return 300;
        }

        if (monthlyEarning >= 100000) {
            return 400;
        }

        throw new IllegalArgumentException(
                "Income range does not have a defined score"
        );
    }

    // Calculate score based on customer age
    private int calculateAgeScore(Integer age) {

        if (age >= 21 && age <= 24) {
            return 50;
        }

        if (age >= 25 && age <= 45) {
            return 150;
        }

        if (age >= 46 && age <= 60) {
            return 100;
        }

        throw new IllegalArgumentException(
                "Age must be between 21 and 60"
        );
    }

    // Calculate score based on employment type
    private int calculateEmploymentScore(EmployeeType employeeType) {

        if (employeeType == null) {
            throw new IllegalArgumentException(
                    "Employment type is required"
            );
        }

        switch (employeeType) {

            case GOVERNMENT:
                return 200;

            case PRIVATE:
                return 150;

            case SELF_EMPLOYED:
                return 100;

            default:
                throw new IllegalArgumentException(
                        "Unsupported employment type: " + employeeType
                );
        }
    }

    // Calculate FOIR percentage
    private double calculateFoir(
            Double monthlySpending,
            Double monthlyEarning) {

        if (monthlySpending == null
                || !Double.isFinite(monthlySpending)
                || monthlySpending < 0) {

            throw new IllegalArgumentException(
                    "Monthly spending cannot be negative or invalid"
            );
        }

        if (monthlyEarning == null
                || !Double.isFinite(monthlyEarning)
                || monthlyEarning <= 0) {

            throw new IllegalArgumentException(
                    "Monthly earning must be greater than zero"
            );
        }

        return (monthlySpending / monthlyEarning) * 100;
    }


    // Calculate score based on FOIR percentage
    private int calculateFoirScore(double foir) {

        if (!Double.isFinite(foir) || foir < 0) {
            throw new IllegalArgumentException(
                    "FOIR must be a valid non-negative percentage"
            );
        }

        if (foir < 30) {
            return 250;
        }

        if (foir < 50) {
            return 150;
        }

        if (foir <= 60) {
            return 75;
        }

        return 0;
    }


    @Override
    public CibilScoreResponse calculateCibilScore(Long customerId) {

        log.info("Starting CIBIL calculation for customerId={}", customerId);

        // 1. Fetch customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId
                ));

        // 2. Validate customer data
        validateCibilData(customer);

        // 3. Calculate individual scores
        int incomeScore =
                calculateIncomeScore(customer.getMonthlyEarning());

        int employmentScore =
                calculateEmploymentScore(customer.getEmployeeType());

        int ageScore =
                calculateAgeScore(customer.getAge());

        double foir = calculateFoir(
                customer.getMonthlySpending(),
                customer.getMonthlyEarning()
        );

        int foirScore = calculateFoirScore(foir);

        // 4. Calculate final CIBIL score
        int finalScore =
                incomeScore
                        + employmentScore
                        + ageScore
                        + foirScore;

        // 5. Determine CIBIL status
        CibilStatus status = getCibilStatus(finalScore);

        log.info(
                "CIBIL calculation completed: customerId={}, " +
                        "incomeScore={}, employmentScore={}, ageScore={}, " +
                        "foir={}, foirScore={}, finalScore={}, status={}",
                customerId,
                incomeScore,
                employmentScore,
                ageScore,
                foir,
                foirScore,
                finalScore,
                status
        );

        // 6. Fetch existing latest report or create a new one
        CibilReport report = cibilReportRepository
                .findTopByCustomer_CustomerIdOrderByCheckDateDesc(customerId)
                .orElseGet(CibilReport::new);

        // 7. Set report details
        report.setCustomer(customer);
        report.setPanNumber(customer.getPanNumber());
        report.setCibilScore(finalScore);
        report.setCheckDate(Instant.now());

        // 8. Save report
        CibilReport savedReport =
                cibilReportRepository.save(report);

        // 9. Map entity to response DTO using ModelMapperConfig
        return mapToResponse(savedReport);
    }

    private CibilScoreResponse mapToResponse(CibilReport report) {

        CibilScoreResponse response =
                modelMapperConfig.modelMapper()
                        .map(report, CibilScoreResponse.class);

        response.setCustomerId(
                report.getCustomer().getCustomerId()
        );

        response.setCustomerName(
                getCustomerName(report.getCustomer())
        );

        response.setPanNumber(
                maskPan(report.getPanNumber())
        );

        response.setCibilScore(
                String.valueOf(report.getCibilScore())
        );

        response.setCibilStatus(
                getCibilStatus(report.getCibilScore())
        );

        response.setLastCheckedDate(
                report.getCheckDate()
        );

        return response;
    }


    // Validate customer data before calculation
    private void validateCibilData(Customer customer) {

        String pan = customer.getPanNumber();

        if (pan == null || pan.isBlank()) {
            throw new IllegalArgumentException(
                    "PAN number is required"
            );
        }

        pan = pan.trim().toUpperCase(Locale.ROOT);

        if (!PAN_PATTERN.matcher(pan).matches()) {
            throw new IllegalArgumentException(
                    "Invalid PAN number format"
            );
        }

        customer.setPanNumber(pan);

        if (customer.getAge() == null
                || customer.getAge() < 21
                || customer.getAge() > 60) {

            throw new IllegalArgumentException(
                    "Age must be between 21 and 60"
            );
        }

        Double income = customer.getMonthlyEarning();

        if (income == null
                || !Double.isFinite(income)
                || income <= 0) {

            throw new IllegalArgumentException(
                    "Monthly earning must be greater than zero"
            );
        }

        Double spending = customer.getMonthlySpending();

        if (spending == null
                || !Double.isFinite(spending)
                || spending < 0) {

            throw new IllegalArgumentException(
                    "Monthly spending cannot be negative"
            );
        }

        if (customer.getEmployeeType() == null) {
            throw new IllegalArgumentException(
                    "Employment type is required"
            );
        }
    }

    // Determine CIBIL status from calculated score
    private CibilStatus getCibilStatus(Integer score) {

        if (score >= 900) {
            return CibilStatus.EXCELLENT;
        }

        if (score >= 800) {
            return CibilStatus.VERY_GOOD;
        }

        if (score >= 750) {
            return CibilStatus.GOOD;
        }

        if (score >= 700) {
            return CibilStatus.AVERAGE;
        }

        if (score >= 650) {
            return CibilStatus.RISKY;
        }

        return CibilStatus.REJECT;
    }


    // Combine first name and last name
    private String getCustomerName(Customer customer) {

        String firstName = customer.getFirstName() == null
                ? ""
                : customer.getFirstName().trim();

        String lastName = customer.getLastName() == null
                ? ""
                : customer.getLastName().trim();

        return (firstName + " " + lastName).trim();
    }

    // Mask PAN before returning it in the API response
    private String maskPan(String panNumber) {

        if (panNumber == null || panNumber.isBlank()) {
            return null;
        }

        String pan = panNumber.trim();

        if (pan.length() != 10) {
            return "**********";
        }

        return pan.substring(0, 2)
                + "******"
                + pan.substring(8);
    }

    @Override
    @Transactional(readOnly = true)
    public CibilScoreResponse getLatestCibilScore(Long customerId) {

        log.info(
                "Fetching latest CIBIL score for customerId={}",
                customerId
        );

        // Verify customer exists
        fetchCustomer(customerId);

        // Fetch latest report
        CibilReport report = cibilReportRepository
                .findTopByCustomer_CustomerIdOrderByCheckDateDesc(
                        customerId
                )
                .orElseThrow(() -> new CibilReportNotFoundException(
                        "CIBIL report not found for customerId: "
                                + customerId
                ));

        // Map entity to response DTO
        return mapToResponse(report);
    }


















//    // 1. Calculate CIBIL score
//    @Override
//    public CibilScoreResponse calculateCibilScore(Long customerId) {
//
//        log.info("Starting CIBIL calculation for customerId={}", customerId);
//
//        User user = fetchCustomer(customerId);
//
//        validateCibilData(user);
//
//        int incomeScore =
//                calculateIncomeScore(user.getMonthlyEarning());
//
//        int employmentScore =
//                calculateEmploymentScore(user.getEmployeeType());
//
//        int ageScore =
//                calculateAgeScore(user.getAge());
//
//        double foir = calculateFoir(
//                user.getMonthlySpending(),
//                user.getMonthlyEarning()
//        );
//
//        int foirScore = calculateFoirScore(foir);
//
//        int finalScore =
//                incomeScore + employmentScore + ageScore + foirScore;
//
//        CibilStatus status = getCibilStatus(finalScore);
//
//        log.info(
//                "CIBIL calculated for customerId={}, incomeScore={}, " +
//                        "employmentScore={}, ageScore={}, foir={}, " +
//                        "foirScore={}, score={}, status={}",
//                customerId,
//                incomeScore,
//                employmentScore,
//                ageScore,
//                foir,
//                foirScore,
//                finalScore,
//                status
//        );
//
//        CibilReport report =
//                saveLatestCibilReport(user, finalScore);
//
//        return mapToResponse(report);
//    }
//
//
//    // 2. Get latest CIBIL score
//    @Override
//    @Transactional(readOnly = true)
//    public CibilScoreResponse getLatestCibilScore(Long customerId) {
//
//        log.info(
//                "Fetching latest CIBIL score for customerId={}",
//                customerId
//        );
//
//        fetchCustomer(customerId);
//
//        CibilReport report = cibilReportRepository
//                .findTopByUser_UserIdOrderByCheckDateDesc(customerId)
//                .orElseThrow(() -> new CibilReportNotFoundException(
//                        "CIBIL report not found for customerId: " + customerId
//                ));
//
//        return mapToResponse(report);
//    }
//
//
//    // 3. Fetch customer from users table
//    private User fetchCustomer(Long customerId) {
//
//        User user = userRepository.findById(customerId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User not found with id: " + customerId
//                ));
//
//        if (user.getRole() != Role.CUSTOMER) {
//            throw new IllegalArgumentException(
//                    "CIBIL score is available only for customer accounts"
//            );
//        }
//
//        return user;
//    }
//
//
//    // 4. Validate data used for calculation
//    private void validateCibilData(User user) {
//
//        String pan = user.getPanNumber();
//
//        if (!StringUtils.hasText(pan)) {
//            throw new IllegalArgumentException(
//                    "PAN number is required"
//            );
//        }
//
//        pan = pan.trim().toUpperCase(Locale.ROOT);
//
//        if (!PAN_PATTERN.matcher(pan).matches()) {
//            throw new IllegalArgumentException(
//                    "Invalid PAN number format"
//            );
//        }
//
//        if (user.getAge() == null
//                || user.getAge() < 21
//                || user.getAge() > 60) {
//
//            throw new IllegalArgumentException(
//                    "Age must be between 21 and 60"
//            );
//        }
//
//        Double income = user.getMonthlyEarning();
//
//        if (income == null
//                || !Double.isFinite(income)
//                || income <= 0) {
//
//            throw new IllegalArgumentException(
//                    "Monthly earning must be greater than zero"
//            );
//        }
//
//        Double spending = user.getMonthlySpending();
//
//        if (spending == null
//                || !Double.isFinite(spending)
//                || spending < 0) {
//
//            throw new IllegalArgumentException(
//                    "Monthly spending cannot be negative"
//            );
//        }
//
//        if (user.getEmployeeType() == null) {
//            throw new IllegalArgumentException(
//                    "Employee type is required"
//            );
//        }
//    }
//
//
//    // 5. Calculate income score
//    private int calculateIncomeScore(Double monthlyEarning) {
//
//        if (monthlyEarning < 25000) {
//            return 100;
//        }
//
//        if (monthlyEarning < 50000) {
//            return 200;
//        }
//
//        if (monthlyEarning < 100000) {
//            return 300;
//        }
//
//        return 400;
//    }
//
//
//    // 6. Calculate employment score
//    private int calculateEmploymentScore(EmployeeType employeeType) {
//
//        switch (employeeType) {
//
//            case GOVERNMENT:
//                return 200;
//
//            case PRIVATE:
//                return 150;
//
//            case SELF_EMPLOYED:
//                return 100;
//
//            default:
//                throw new IllegalArgumentException(
//                        "Unsupported employee type: " + employeeType
//                );
//        }
//    }
//
//
//    // 7. Calculate age score
//    private int calculateAgeScore(Integer age) {
//
//        if (age >= 21 && age <= 24) {
//            return 50;
//        }
//
//        if (age >= 25 && age <= 45) {
//            return 150;
//        }
//
//        if (age >= 46 && age <= 60) {
//            return 100;
//        }
//
//        throw new IllegalArgumentException(
//                "Age must be between 21 and 60"
//        );
//    }
//
//
//    // 8. Calculate FOIR
//    private double calculateFoir(
//            Double monthlySpending,
//            Double monthlyEarning) {
//
//        return (monthlySpending / monthlyEarning) * 100;
//    }
//
//
//    // 9. Calculate FOIR score
//    private int calculateFoirScore(double foir) {
//
//        if (foir < 30) {
//            return 250;
//        }
//
//        if (foir < 50) {
//            return 150;
//        }
//
//        if (foir <= 60) {
//            return 75;
//        }
//
//        return 0;
//    }
//
//
//    // 10. Convert score to CIBIL status
//    private CibilStatus getCibilStatus(Integer score) {
//
//        if (score >= 900) {
//            return CibilStatus.EXCELLENT;
//        }
//
//        if (score >= 800) {
//            return CibilStatus.VERY_GOOD;
//        }
//
//        if (score >= 750) {
//            return CibilStatus.GOOD;
//        }
//
//        if (score >= 700) {
//            return CibilStatus.AVERAGE;
//        }
//
//        if (score >= 650) {
//            return CibilStatus.RISKY;
//        }
//
//        return CibilStatus.REJECT;
//    }
//
//
//    // 11. Save or update latest report
//    private CibilReport saveLatestCibilReport(
//            User user,
//            Integer score) {
//
//        CibilReport report = cibilReportRepository
//                .findTopByUser_UserIdOrderByCheckDateDesc(
//                        user.getUserId()
//                )
//                .orElseGet(CibilReport::new);
//
//        report.setUser(user);
//
//        report.setPanNumber(
//                user.getPanNumber()
//                        .trim()
//                        .toUpperCase(Locale.ROOT)
//        );
//
//        report.setCibilScore(score);
//        report.setCheckDate(Instant.now());
//
//        CibilReport savedReport =
//                cibilReportRepository.save(report);
//
//        log.info(
//                "CIBIL report saved for customerId={}, reportId={}",
//                user.getUserId(),
//                savedReport.getCibilReportId()
//        );
//
//        return savedReport;
//    }
//
//
//    // 12. Map entity to response using your ModelMapperConfig
//    private CibilScoreResponse mapToResponse(CibilReport report) {
//
//        CibilScoreResponse response =
//                modelMapperConfig.modelMapper()
//                        .map(report, CibilScoreResponse.class);
//
//        response.setCustomerId(
//                report.getUser().getUserId()
//        );
//
//        response.setCustomerName(
//                getCustomerName(report.getUser())
//        );
//
//        response.setPanNumber(
//                maskPan(report.getPanNumber())
//        );
//
//        response.setCibilScore(
//                report.getCibilScore()
//        );
//
//        response.setCibilStatus(
//                CibilStatus.valueOf(getCibilStatus(report.getCibilScore()).name())
//        );
//
//        response.setLastCheckedDate(
//                report.getCheckDate()
//        );
//
//        return response;
//    }
//
//
//    // 13. Prepare full customer name
//    private String getCustomerName(User user) {
//
//        String firstName = user.getFirstName() == null
//                ? ""
//                : user.getFirstName().trim();
//
//        String lastName = user.getLastName() == null
//                ? ""
//                : user.getLastName().trim();
//
//        return (firstName + " " + lastName).trim();
//    }
//
//
//    // 14. Mask PAN before returning it
//    private String maskPan(String panNumber) {
//
//        if (!StringUtils.hasText(panNumber)) {
//            return null;
//        }
//
//        String pan = panNumber.trim();
//
//        if (pan.length() != 10) {
//            return "**********";
//        }
//
//        return pan.substring(0, 2)
//                + "******"
//                + pan.substring(8);
//    }
}