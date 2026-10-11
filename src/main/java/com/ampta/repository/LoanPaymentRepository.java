package com.ampta.repository;
import com.ampta.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
}
