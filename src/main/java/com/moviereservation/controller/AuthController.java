package com.moviereservation.controller;

import com.moviereservation.dto.request.AuthRequests.LoginRequest;
import com.moviereservation.dto.request.AuthRequests.SignupRequest;
import com.moviereservation.dto.response.Responses.AuthResponse;
import com.moviereservation.dto.response.Responses.UserResponse;
import com.moviereservation.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Sign up, log in, and user management")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user account")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @Operation(summary = "Log in and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Promote a user to ADMIN role (Admin only)")
    @PatchMapping("/users/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> promoteToAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.promoteToAdmin(userId));
    }
}
