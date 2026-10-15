package com.ampta.repository;
import com.ampta.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

}
