package com.ampta.controller;


import com.ampta.dto.request.LoginRequest;
import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;
import com.ampta.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterRequest request){
        log.info("REST request to register user with email: {}", request);

        UserResponse response = authService.register(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "User register successfully", response), HttpStatus.CREATED);
    }



    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody LoginRequest request){
        log.info("REST request to login user with email: {}", request);

        UserResponse response = authService.login(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(new ApiResponse<>(true, "User login successfully", response), HttpStatus.CREATED);
    }

}
