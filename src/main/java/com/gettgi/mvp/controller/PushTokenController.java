package com.gettgi.mvp.controller;

import com.gettgi.mvp.dto.request.PushTokenUpsertRequestDto;
import com.gettgi.mvp.entity.PushToken;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.PushPlatform;
import com.gettgi.mvp.repository.PushTokenRepository;
import com.gettgi.mvp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/push/tokens")
@RequiredArgsConstructor
public class PushTokenController {

    private final UserRepository userRepository;
    private final PushTokenRepository pushTokenRepository;

    @PostMapping
    public ResponseEntity<Void> upsertToken(
            @Valid @RequestBody PushTokenUpsertRequestDto request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String telephone = principal.getUsername();
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = request.token().trim();
        PushToken pushToken = pushTokenRepository.findByToken(token).orElseGet(PushToken::new);
        pushToken.setToken(token);
        pushToken.setPlatform(request.platform() != null ? request.platform() : PushPlatform.UNKNOWN);
        pushToken.setUser(user);
        pushTokenRepository.save(pushToken);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteToken(
            @RequestParam("token") String token,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String telephone = principal.getUsername();
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String normalized = token != null ? token.trim() : "";
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is required");
        }

        pushTokenRepository.deleteByUser_IdAndToken(user.getId(), normalized);
        return ResponseEntity.noContent().build();
    }
}
