package com.ampta.dto.request;

import com.ampta.entity.Customer;
import com.ampta.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequestDto {
    private Customer customer;
    private String message;
    private Boolean read = false;
    private LocalDateTime createdAt;


}
