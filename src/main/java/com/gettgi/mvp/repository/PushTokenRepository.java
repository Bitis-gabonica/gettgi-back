package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {

    Optional<PushToken> findByToken(String token);

    List<PushToken> findAllByUser_Id(UUID userId);

    long deleteByUser_IdAndToken(UUID userId, String token);
}

