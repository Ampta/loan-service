package com.ampta.service;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    UserResponse login(String email, String password);
}
