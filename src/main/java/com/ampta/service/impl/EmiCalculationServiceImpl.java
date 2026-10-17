package com.ampta.service.impl;

import com.ampta.service.EmiCalculationService;
import org.springframework.stereotype.Service;

@Service
public class EmiCalculationServiceImpl implements EmiCalculationService {
    @Override
    public double calculateEmi(Double principal, double annualInterestRate, Integer tenureMonths) {

        double monthlyRate= annualInterestRate / 12 / 100;

        double power=Math.pow( 1 + monthlyRate,tenureMonths);

        return (principal * monthlyRate * power)
                / (power - 1);
    }
}
