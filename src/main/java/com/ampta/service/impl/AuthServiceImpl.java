package com.ampta.service.impl;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;
import com.ampta.entity.User;
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

        User user = modelMapper.map(request, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with email: " + email));
        return modelMapper.map(user, LoginResponse.class);
    }
}
