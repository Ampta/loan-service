package com.ampta.repository;
import com.ampta.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

}
