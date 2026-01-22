package com.gettgi.mvp.controller;

import com.gettgi.mvp.config.JwtUtils;
import com.gettgi.mvp.dto.mappers.UserMapper;
import com.gettgi.mvp.dto.request.AuthLoginRequestDto;
import com.gettgi.mvp.dto.request.AuthRegisterRequestDto;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.UserRole;
import com.gettgi.mvp.security.LoginRateLimiter;
import com.gettgi.mvp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final LoginRateLimiter loginRateLimiter;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterRequestDto authRegisterRequestDto){
        if (userService.findByTelephone(authRegisterRequestDto.telephone())!=null){
            return ResponseEntity.badRequest().body("L'utilisateur existe deja");
        }
        if (StringUtils.hasText(authRegisterRequestDto.email()) && userService.existsByEmail(authRegisterRequestDto.email())) {
            return ResponseEntity.badRequest().body("L'email existe deja");
        }
        User user= userMapper.toEntity(authRegisterRequestDto);
        String rawPassword = StringUtils.hasText(authRegisterRequestDto.password())
                ? authRegisterRequestDto.password()
                : generateRandomPassword();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.ROLE_USER);
        User saved = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toRegisterResponse(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequestDto authLoginRequestDto,
                                   HttpServletRequest request){
        final String telephone = normalizeTelephone(authLoginRequestDto.telephone());
        final String password = authLoginRequestDto.password();

        String clientIp = resolveClientIp(request);
        String rateKey = clientIp + ":" + telephone;

        if (loginRateLimiter.isBlocked(rateKey)) {
            log.warn("Login attempt blocked (rate limit) for telephone={} ip={}", telephone, clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Trop de tentatives. Veuillez patienter ou completer une verification supplementaire.");
        }

        // MVP: allow phone-only login (no password / no OTP).
        if (!StringUtils.hasText(password)) {
            User user = userService.findByTelephone(telephone);
            if (user == null) {
                loginRateLimiter.recordFailure(rateKey);
                log.warn("Login failed (unknown user) for telephone={} ip={}", telephone, clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            loginRateLimiter.recordSuccess(rateKey);
            log.info("Login succeeded (phone-only) for telephone={} ip={}", telephone, clientIp);
            Map<String,Object> authData = new HashMap<>();
            authData.put("token",jwtUtils.generateToken(telephone));
            authData.put("type","Bearer");
            return ResponseEntity.ok(authData);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(telephone, password));
            if (authentication.isAuthenticated()){
                loginRateLimiter.recordSuccess(rateKey);
                log.info("Login succeeded for telephone={} ip={}", telephone, clientIp);
                Map<String,Object> authData = new HashMap<>();
                authData.put("token",jwtUtils.generateToken(telephone));
                authData.put("type","Bearer");
                return ResponseEntity.ok(authData);
            }
            loginRateLimiter.recordFailure(rateKey);
            log.warn("Login failed (bad credentials) for telephone={} ip={}", telephone, clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }catch (AuthenticationException e){
            loginRateLimiter.recordFailure(rateKey);
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }

    private String normalizeTelephone(String raw) {
        if (!StringUtils.hasText(raw)) return raw;
        return raw.replaceAll("\\s+", "");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
