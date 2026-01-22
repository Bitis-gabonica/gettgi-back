package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequestDto(
        @NotBlank
        @Size(min = 1, max = 20)
        String nom,
        @NotBlank
        @Size(min = 1, max = 30)
        String prenom,
        @Size(max = 200)
        String adresse,
        @NotBlank
        @Pattern(
                regexp = "^(?:\\+221|221)?(?:7[05678]|3[03])\\d{7}$",
                message = "Format attendu : 2217Xxxxxxx (9 chiffres, prefixe plus optionnel)"
        )
        String telephone,

        @Email
        @Size(max = 100)
        String email,
        UserRole role,
        // Optional: password can be generated server-side for MVP phone-only auth.
        @Size(min = 8, max = 100)
        String password
) {



}
