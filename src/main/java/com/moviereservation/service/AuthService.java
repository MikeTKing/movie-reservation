package com.moviereservation.service;

import com.moviereservation.dto.request.AuthRequests.LoginRequest;
import com.moviereservation.dto.request.AuthRequests.SignupRequest;
import com.moviereservation.dto.response.Responses.AuthResponse;
import com.moviereservation.dto.response.Responses.UserResponse;
import com.moviereservation.entity.Role;
import com.moviereservation.entity.User;
import com.moviereservation.exception.Exceptions.ConflictException;
import com.moviereservation.exception.Exceptions.ResourceNotFoundException;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.of(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.of(token, user);
    }

    @Transactional
    public UserResponse promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setRole(Role.ADMIN);
        return UserResponse.from(userRepository.save(user));
    }
}
