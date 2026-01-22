package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.enums.PushPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "push_token", indexes = {
        @Index(name = "idx_push_token_user", columnList = "user_id"),
        @Index(name = "idx_push_token_token", columnList = "token", unique = true)
})
@Getter
@Setter
@ToString(exclude = {"user"})
public class PushToken extends AuditableUuidEntity {

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 16)
    private PushPlatform platform = PushPlatform.UNKNOWN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

