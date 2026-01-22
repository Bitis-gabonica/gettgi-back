package com.gettgi.mvp.service;

import com.gettgi.mvp.dto.request.AnimalCreateRequestDto;
import com.gettgi.mvp.dto.request.AnimalUpdateRequestDto;
import com.gettgi.mvp.dto.request.AnimalTroupeauPatchRequestDto;
import com.gettgi.mvp.dto.response.AnimalCreateResponseDto;
import com.gettgi.mvp.dto.response.FindAllAnimalResponseDto;
import com.gettgi.mvp.entity.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AnimalService {

    Page<FindAllAnimalResponseDto> FindAllAnimalsByUserTelephone(Pageable pageable, String telephone);

    AnimalCreateResponseDto CreateAnimal(AnimalCreateRequestDto dto, String telephone);

    AnimalCreateResponseDto UpdateAnimal(java.util.UUID animalId, AnimalUpdateRequestDto dto, String telephone);

    void DeleteAnimal(java.util.UUID animalId, String telephone);

    AnimalCreateResponseDto PatchAnimalTroupeau(java.util.UUID animalId, java.util.UUID troupeauId, String telephone);

    // Detail d'un animal (avec vaccins et device)
    com.gettgi.mvp.dto.response.AnimalDetailResponseDto GetAnimalDetail(java.util.UUID animalId, String telephone);

}
