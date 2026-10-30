package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.auth.AuthResponse;
import com.mycompany.projectmanagementapp.dto.auth.LoginRequest;
import com.mycompany.projectmanagementapp.dto.auth.RegisterRequest;
import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import com.mycompany.projectmanagementapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.saveAndFlush(user);

        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        // Sai mật khẩu -> AuthenticationException -> GlobalExceptionHandler trả 401.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));
        return UserResponse.from(user);
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponse.from(user));
    }
}
