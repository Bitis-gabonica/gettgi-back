package com.gettgi.mvp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);
    private List<KeyHolder> keyRing = List.of();

    @PostConstruct
    void initKeyRing() {
        List<String> encodedKeys = jwtProperties.allKeys();
        if (CollectionUtils.isEmpty(encodedKeys)) {
            throw new IllegalStateException("At least one JWT signing key must be configured (app.jwt.secret-key)");
        }

        List<KeyHolder> holders = new ArrayList<>();
        int index = 0;
        for (String encoded : encodedKeys) {
            String trimmed = encoded != null ? encoded.trim() : "";
            if (trimmed.isEmpty()) {
                continue;
            }
            Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(trimmed));
            holders.add(new KeyHolder("k" + index, key));
            index++;
        }
        if (holders.isEmpty()) {
            throw new IllegalStateException("Configured JWT keys are empty after trimming");
        }
        this.keyRing = List.copyOf(holders);
        currentKeyIndex.set(0);
        log.info("Initialized JWT key ring with {} keys. Active kid={}", keyRing.size(), currentKey().id());
    }

    @Scheduled(fixedDelayString = "#{@jwtProperties.rotationInterval.toMillis()}")
    public void rotateSigningKey() {
        if (keyRing.size() <= 1) {
            return;
        }
        int next = currentKeyIndex.updateAndGet(i -> (i + 1) % keyRing.size());
        log.info("Rotated JWT signing key. Active kid={} (index {})", keyRing.get(next).id(), next);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        KeyHolder holder = currentKey();
        return Jwts.builder()
                .setHeaderParam("kid", holder.id())
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationTime()))
                .signWith(holder.key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String telephone = extractTelephone(token);
        return (telephone.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTelephone(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        JwtException lastException = null;
        for (KeyHolder holder : keyRing) {
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(holder.key())
                        .setAllowedClockSkewSeconds(60)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (JwtException ex) {
                lastException = ex;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new JwtException("Unable to parse JWT token with configured keys");
    }

    private KeyHolder currentKey() {
        return keyRing.get(currentKeyIndex.get());
    }

    private record KeyHolder(String id, Key key) {
    }
}
