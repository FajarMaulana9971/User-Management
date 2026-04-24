package com.unictive.usermanagement.services.implementations;

import com.unictive.usermanagement.dto.requests.LoginRequest;
import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.responses.AuthResponse;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.entities.User;
import com.unictive.usermanagement.exceptions.types.AuthException;
import com.unictive.usermanagement.mappers.UserMapper;
import com.unictive.usermanagement.repositories.UserRepository;
import com.unictive.usermanagement.security.JwtBlacklistService;
import com.unictive.usermanagement.security.JwtService;
import com.unictive.usermanagement.services.interfaces.AuthService;
import com.unictive.usermanagement.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtBlacklistService blacklistService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new AuthException("Invalid username or password");
        }

        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(() -> new AuthException("User not found"));

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile profilePicture) {
        UserResponse userResponse = userService.createUser(request, profilePicture);

        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(() -> new AuthException("User not found after registration"));

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User registered: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            try {
                Date expiration = jwtService.extractExpiration(token);
                blacklistService.blacklist(token, expiration);
                log.info("Token blacklisted on logout");
            } catch (Exception e) {
                log.warn("Logout with invalid token: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);

            if (blacklistService.isBlacklisted(refreshToken)) {
                throw new AuthException("Refresh token has been revoked");
            }

            User user = userRepository.findByUsernameWithRole(username)
                    .orElseThrow(() -> new AuthException("User not found"));

            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new AuthException("Invalid refresh token");
            }

            String newAccessToken = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .user(userMapper.toResponse(user))
                    .build();

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Invalid refresh token");
        }
    }
}