package org.example.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.dto.ChangePasswordRequest;
import org.example.project.dto.ForgotPasswordRequest;
import org.example.project.dto.LoginRequest;
import org.example.project.dto.RefreshTokenRequest;
import org.example.project.dto.ResetPasswordRequest;
import org.example.project.dto.TokenResponse;
import org.example.project.entity.PasswordResetToken;
import org.example.project.entity.RefreshToken;
import org.example.project.entity.TokenBlacklist;
import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.example.project.repository.PasswordResetTokenRepository;
import org.example.project.repository.RefreshTokenRepository;
import org.example.project.repository.TokenBlacklistRepository;
import org.example.project.repository.UserRepository;
import org.example.project.security.CustomUserDetails;
import org.example.project.security.JwtService;
import org.example.project.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.password-reset.expiration-ms}")
    private long passwordResetExpirationMs;

    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new ApiException(
                    "Sai tài khoản hoặc mật khẩu",
                    HttpStatus.UNAUTHORIZED
            );
        } catch (LockedException | DisabledException ex) {
            throw new ApiException(
                    "Tài khoản đã bị khóa",
                    HttpStatus.FORBIDDEN
            );
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new ApiException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenValue, user.getId(), user.getRole());
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ApiException("Refresh token đã hết hạn hoặc bị thu hồi", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new ApiException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateRefreshToken(user);

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setUser(user);
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        refreshTokenRepository.save(newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshTokenValue, user.getId(), user.getRole());
    }

    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new ApiException("Token không hợp lệ", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtService.isTokenValid(accessToken)) {
            throw new ApiException("Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        if (tokenBlacklistRepository.existsByToken(accessToken)) {
            throw new ApiException("Token đã bị thu hồi", HttpStatus.UNAUTHORIZED);
        }

        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setToken(accessToken);
        blacklist.setExpiryDate(jwtService.extractExpiration(accessToken));
        tokenBlacklistRepository.save(blacklist);

        String email = jwtService.extractUsername(accessToken);
        userRepository.findByEmail(email).ifPresent(refreshTokenRepository::deleteByUser);
    }

    public void changePassword(ChangePasswordRequest request) {
        CustomUserDetails currentUser = SecurityUtils.getCurrentUserDetails();
        User user = currentUser.getUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException("Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Email không tồn tại trong hệ thống", HttpStatus.NOT_FOUND));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(Instant.now().plusMillis(passwordResetExpirationMs));
        passwordResetTokenRepository.save(resetToken);

        sendResetEmail(user.getEmail(), token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException("Token đặt lại mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ApiException("Token đặt lại mật khẩu đã hết hạn hoặc đã sử dụng", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private void sendResetEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Đặt lại mật khẩu - Hệ thống Quản lý Khóa học");
            message.setText("Mã token đặt lại mật khẩu của bạn: " + token
                    + "\nToken có hiệu lực trong 1 giờ.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.info("Không gửi được email. Reset token cho {}: {}", email, token);
        }
    }
}
