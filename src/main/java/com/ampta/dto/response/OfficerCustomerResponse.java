package com.ampta.dto.response;

import lombok.Data;


@Data
public class OfficerCustomerResponse {

    private Long customerId;
    private String customerName;
    private String email;
    private String phoneNumber;
}