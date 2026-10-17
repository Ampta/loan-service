
package com.ampta.repository;

import com.ampta.entity.LoanDeal;
import com.ampta.entity.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanDealRepository extends JpaRepository<LoanDeal, Long> {

    // Find all loan deals for a customer
    List<LoanDeal> findByCustomer_CustomerId(Long customerId);

    // Find loan deals by status
    List<LoanDeal> findByStatus(LoanStatus status);

    // Find loan deals for a customer with a specific status
    List<LoanDeal> findByCustomer_CustomerIdAndStatus(
            Long customerId,
            LoanStatus status
    );
}
