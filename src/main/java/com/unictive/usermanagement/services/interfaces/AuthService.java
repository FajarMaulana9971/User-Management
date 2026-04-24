package com.unictive.usermanagement.services.interfaces;

import com.unictive.usermanagement.dto.requests.LoginRequest;
import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.responses.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request, MultipartFile profilePicture);

    void logout(String token);

    AuthResponse refreshToken(String refreshToken);
}