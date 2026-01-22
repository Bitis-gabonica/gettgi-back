package com.gettgi.mvp.controller;

import com.gettgi.mvp.dto.response.TroupeauDetailResponseDto;
import com.gettgi.mvp.dto.response.TroupeauResponseDto;
import com.gettgi.mvp.dto.request.TroupeauCreateRequestDto;
import com.gettgi.mvp.dto.request.TroupeauUpdateRequestDto;
import com.gettgi.mvp.controller.validation.PaginationValidator;
import com.gettgi.mvp.service.TroupeauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/troupeaux")
@RequiredArgsConstructor
public class TroupeauController {

    private final TroupeauService troupeauService;

    @GetMapping
    public ResponseEntity<Page<TroupeauResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails principal) {
        int[] validated = PaginationValidator.validateAndNormalize(page, size);
        String telephone = principal.getUsername();
        Pageable pageable = PageRequest.of(validated[0], validated[1]);
        Page<TroupeauResponseDto> items = troupeauService.FindAllByUserTelephone(pageable, telephone);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<TroupeauResponseDto> create(
            @Valid @RequestBody TroupeauCreateRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        var created = troupeauService.CreateTroupeau(dto, telephone);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{troupeauId}")
    public ResponseEntity<TroupeauResponseDto> update(
            @PathVariable UUID troupeauId,
            @Valid @RequestBody TroupeauUpdateRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        var updated = troupeauService.UpdateTroupeau(troupeauId, dto, telephone);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{troupeauId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID troupeauId,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        troupeauService.DeleteTroupeau(troupeauId, telephone);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{troupeauId}")
    public ResponseEntity<TroupeauDetailResponseDto> getTroupeauDetail(
            @PathVariable UUID troupeauId,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        var detail = troupeauService.GetTroupeauDetail(troupeauId, telephone);
        return ResponseEntity.ok(detail);
    }
}
