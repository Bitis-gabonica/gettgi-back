package com.gettgi.mvp.service.Impl;

import com.gettgi.mvp.dto.mappers.AnimalMapper;
import com.gettgi.mvp.dto.response.FindAllAnimalResponseDto;
import com.gettgi.mvp.dto.response.TroupeauDetailResponseDto;
import com.gettgi.mvp.dto.response.TroupeauResponseDto;
import com.gettgi.mvp.dto.request.TroupeauCreateRequestDto;
import com.gettgi.mvp.dto.request.TroupeauUpdateRequestDto;
import com.gettgi.mvp.entity.Troupeau;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.repository.TroupeauRepository;
import com.gettgi.mvp.repository.UserRepository;
import com.gettgi.mvp.service.TroupeauService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TroupeauServiceImpl implements TroupeauService {

    private final TroupeauRepository troupeauRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final AnimalMapper animalMapper;

    @Override
    public TroupeauDetailResponseDto GetTroupeauDetail(UUID troupeauId, String telephone) {
        Troupeau troupeau = troupeauRepository.findByIdAndUserTelephone(troupeauId, telephone)
                .orElseThrow(() -> new IllegalStateException("Troupeau introuvable ou non autorisé"));

        List<FindAllAnimalResponseDto> animals = animalRepository.findAllByTroupeauId(troupeauId)
                .stream()
                .map(animalMapper::toDto)
                .toList();

        return new TroupeauDetailResponseDto(troupeau.getId(), troupeau.getNom(), animals);
    }

    @Override
    public org.springframework.data.domain.Page<TroupeauResponseDto> FindAllByUserTelephone(org.springframework.data.domain.Pageable pageable, String telephone) {
        return troupeauRepository.findAllByUserTelephone(telephone, pageable)
                .map(t -> new TroupeauResponseDto(t.getId(), t.getNom()));
    }

    @Override
    @Transactional
    public TroupeauResponseDto CreateTroupeau(TroupeauCreateRequestDto dto, String telephone) {
        User user = userRepository.findByTelephone(telephone).orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
        if (troupeauRepository.existsByUserIdAndNomIgnoreCase(user.getId(), dto.nom())) {
            throw new IllegalStateException("Un troupeau avec ce nom existe déjà");
        }
        Troupeau t = new Troupeau();
        t.setNom(dto.nom());
        t.setUser(user);
        Troupeau saved = troupeauRepository.save(t);
        return new TroupeauResponseDto(saved.getId(), saved.getNom());
    }

    @Override
    @Transactional
    public TroupeauResponseDto UpdateTroupeau(UUID troupeauId, TroupeauUpdateRequestDto dto, String telephone) {
        Troupeau troupeau = troupeauRepository.findByIdAndUserTelephone(troupeauId, telephone)
                .orElseThrow(() -> new IllegalStateException("Troupeau introuvable ou non autorisé"));
        // check uniqueness on user
        if (troupeau.getUser() != null && troupeauRepository.existsByUserIdAndNomIgnoreCase(troupeau.getUser().getId(), dto.nom())) {
            // allow same name if it's the same entity
            if (!dto.nom().equalsIgnoreCase(troupeau.getNom())) {
                throw new IllegalStateException("Un troupeau avec ce nom existe déjà");
            }
        }
        troupeau.setNom(dto.nom());
        Troupeau saved = troupeauRepository.save(troupeau);
        return new TroupeauResponseDto(saved.getId(), saved.getNom());
    }

    @Override
    @Transactional
    public void DeleteTroupeau(UUID troupeauId, String telephone) {
        Troupeau troupeau = troupeauRepository.findByIdAndUserTelephone(troupeauId, telephone)
                .orElseThrow(() -> new IllegalStateException("Troupeau introuvable ou non autorisé"));

        // détacher les animaux avant suppression
        var animals = animalRepository.findAllByTroupeauId(troupeauId);
        if (animals != null && !animals.isEmpty()) {
            for (var a : animals) {
                a.setTroupeau(null);
            }
            animalRepository.saveAll(animals);
        }
        troupeauRepository.delete(troupeau);
    }
}
