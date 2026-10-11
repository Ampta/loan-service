package com.ampta.service;

public interface EmiCalculationService {

    double calculateEmi(Double principal,double annualInterestRate, Integer tenureMonths);
}
