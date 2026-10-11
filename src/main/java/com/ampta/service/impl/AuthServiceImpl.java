package com.ampta.service.impl;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.enums.Role;
import com.ampta.entity.User;
import com.ampta.exception.ResourceAlreadyExistsException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.AuthService;
import com.ampta.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Override
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail().trim();
        String phoneNumber = request.getPhoneNumber().trim();
        String panNumber = request.getPanNumber().trim();
        String aadhaarNumber = request.getAadhaarNumber().trim();

        if(userRepository.existsByEmail(email)){
            throw new ResourceAlreadyExistsException("Email already exists: " + email);
        }

        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new ResourceAlreadyExistsException("Phone number already exists: " + phoneNumber);
        }

        if(customerRepository.existsByPanNumber(panNumber)){
            throw new ResourceAlreadyExistsException("Pan number already exists: " + panNumber);
        }

        if(customerRepository.existsByAadhaarNumber(aadhaarNumber)){
            throw new ResourceAlreadyExistsException("Aadhaar number already exists: " + aadhaarNumber);
        }

        String verificationToken = UUID.randomUUID().toString();

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPassword(request.getPassword());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAadhaarNumber(request.getAadhaarNumber());
        customer.setPanNumber(request.getPanNumber());
        customer.setEmployeeType(request.getEmployeeType());
        customer.setMonthlyEarning(request.getMonthlyEarning());
        customer.setMonthlySpending(request.getMonthlySpending());
        customer.setAge(request.getAge());
        customer.setAddress(request.getAddress());
        customer.setIsEmailVerified(false);
        customer.setVerificationToken(verificationToken);
        customer.setAccountStatus("ACTIVE");

        Customer savedCustomer = customerRepository.save(customer);

        // Todo: create wallet here

        User user = new User();
        user.setRole(Role.CUSTOMER);
        user.setCustomer(savedCustomer);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword());
        User savedUser = userRepository.save(user);

        emailService.sendRegistrationSuccessEmail(
                savedCustomer.getEmail(),
                savedCustomer.getFirstName() + (savedCustomer.getLastName() != null ? " " + savedCustomer.getLastName() : ""),
                request.getPassword(),
                verificationToken
        );

        return UserResponse.builder()
                .userId(savedUser.getUserId())
                .role(savedUser.getRole())
                .customerId(savedCustomer.getCustomerId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build();
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with email: " + email));

        String token = "jwt-token-" + UUID.randomUUID();
        String refreshToken = "refresh-token-" + UUID.randomUUID();

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .customerId(user.getCustomer() != null ? user.getCustomer().getCustomerId() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
