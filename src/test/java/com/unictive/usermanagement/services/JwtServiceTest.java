package com.unictive.usermanagement.services;

import com.unictive.usermanagement.configs.AppPropertiesConfig;
import com.unictive.usermanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    private static final String SECRET           = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long   EXPIRATION         = 86_400_000L; // 24 jam
    private static final long   REFRESH_EXPIRATION = 604_800_000L; // 7 hari

    @Mock
    private AppPropertiesConfig appProperties;

    @Mock
    private AppPropertiesConfig.JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        when(appProperties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getSecret()).thenReturn(SECRET);

        // ✅ FIX #1: Stub expiration values.
        // Tanpa ini, Mockito return 0L (default long) sehingga token
        // langsung expired pada saat dibuat.
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION);
        when(jwtProperties.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        userDetails = new User("testuser", "password", Collections.emptyList());
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {

        @Test
        @DisplayName("should generate a non-null access token")
        void shouldGenerateAccessToken() {
            String token = jwtService.generateToken(userDetails);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate a non-null refresh token")
        void shouldGenerateRefreshToken() {
            String token = jwtService.generateRefreshToken(userDetails);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("access and refresh tokens should differ")
        void accessAndRefreshTokensShouldDiffer() {
            // ✅ FIX #2: Karena expiration berbeda (24h vs 7d),
            // payload JWT berbeda sehingga token pasti tidak sama.
            String access  = jwtService.generateToken(userDetails);
            String refresh = jwtService.generateRefreshToken(userDetails);
            assertThat(access).isNotEqualTo(refresh);
        }
    }

    @Nested
    @DisplayName("Token Parsing")
    class TokenParsing {

        @Test
        @DisplayName("should extract correct username from token")
        void shouldExtractUsername() {
            String token    = jwtService.generateToken(userDetails);
            String username = jwtService.extractUsername(token);
            assertThat(username).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should extract expiration date in the future")
        void shouldExtractExpiration() {
            String token      = jwtService.generateToken(userDetails);
            Date   expiration = jwtService.extractExpiration(token);
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("valid token should pass validation")
        void validTokenShouldPass() {
            String token = jwtService.generateToken(userDetails);
            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("token for different user should fail validation")
        void tokenForDifferentUserShouldFail() {
            String token = jwtService.generateToken(userDetails);
            UserDetails other = new User("other_user", "pass", Collections.emptyList());
            assertThat(jwtService.isTokenValid(token, other)).isFalse();
        }

        @Test
        @DisplayName("fresh token should not be expired")
        void freshTokenShouldNotBeExpired() {
            String token = jwtService.generateToken(userDetails);
            assertThat(jwtService.isTokenExpired(token)).isFalse();
        }
    }
}