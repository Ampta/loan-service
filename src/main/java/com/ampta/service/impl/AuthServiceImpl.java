package com.ampta.service.impl;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.UserResponse;
import com.ampta.entity.enums.Role;
import com.ampta.entity.User;
import com.ampta.exception.ResourceAlreadyExistsException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.UserRepository;
import com.ampta.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

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

        if(userRepository.existsByPanNumber(panNumber)){
            throw new ResourceAlreadyExistsException("Pan number already exists: " + panNumber);
        }

        if(userRepository.existsByAadhaarNumber(aadhaarNumber)){
            throw new ResourceAlreadyExistsException("Aadhaar number already exists: " + aadhaarNumber);
        }

        User user = modelMapper.map(request, User.class);

        // TODO: ENCODE PASSWORD

        user.setRole(Role.CUSTOMER);
        user.setMfaEnabled(false);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse login(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with email: " + email));
        return modelMapper.map(user, UserResponse.class);
    }
}
