package com.gettgi.mvp.controller;


import com.gettgi.mvp.dto.request.AnimalCreateRequestDto;
import com.gettgi.mvp.dto.response.AnimalCreateResponseDto;
import com.gettgi.mvp.dto.response.FindAllAnimalResponseDto;
import com.gettgi.mvp.dto.response.AnimalDetailResponseDto;
import com.gettgi.mvp.dto.request.AnimalUpdateRequestDto;
import com.gettgi.mvp.controller.validation.PaginationValidator;
import com.gettgi.mvp.dto.request.AnimalTroupeauPatchRequestDto;
import com.gettgi.mvp.service.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalController {


    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<Page<FindAllAnimalResponseDto>> findAllAnimalsByUserId(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserDetails principal) {

        int[] validated = PaginationValidator.validateAndNormalize(page, size);
        String telephone = principal.getUsername();
        Pageable pageable = PageRequest.of(validated[0], validated[1]);

        Page<FindAllAnimalResponseDto> animals = animalService.FindAllAnimalsByUserTelephone(pageable, telephone);

        return ResponseEntity.ok(animals);
    }


    @GetMapping("/{animalId}")
    public ResponseEntity<AnimalDetailResponseDto> getAnimalDetail(
            @PathVariable UUID animalId,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        var detail = animalService.GetAnimalDetail(animalId, telephone);
        return ResponseEntity.ok(detail);
    }



    @PostMapping
    public ResponseEntity<AnimalCreateResponseDto> createAnimal(
            @Valid @RequestBody AnimalCreateRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        AnimalCreateResponseDto created = animalService.CreateAnimal(dto, telephone);

        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{animalId}")
    public ResponseEntity<AnimalCreateResponseDto> updateAnimal(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalUpdateRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        AnimalCreateResponseDto updated = animalService.UpdateAnimal(animalId, dto, telephone);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{animalId}")
    public ResponseEntity<Void> deleteAnimal(
            @PathVariable UUID animalId,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        animalService.DeleteAnimal(animalId, telephone);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{animalId}/troupeau")
    public ResponseEntity<AnimalCreateResponseDto> patchAnimalTroupeau(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalTroupeauPatchRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String telephone = principal.getUsername();
        var updated = animalService.PatchAnimalTroupeau(animalId, dto.troupeauId(), telephone);
        return ResponseEntity.ok(updated);
    }









}
