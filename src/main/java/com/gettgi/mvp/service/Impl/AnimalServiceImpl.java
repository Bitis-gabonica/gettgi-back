package com.gettgi.mvp.service.Impl;

import com.gettgi.mvp.dto.mappers.AnimalMapper;
import com.gettgi.mvp.dto.request.AnimalCreateRequestDto;
import com.gettgi.mvp.dto.request.AnimalUpdateRequestDto;
import com.gettgi.mvp.dto.response.AnimalCreateResponseDto;
import com.gettgi.mvp.dto.response.FindAllAnimalResponseDto;
import com.gettgi.mvp.dto.response.AnimalDetailResponseDto;
import com.gettgi.mvp.dto.response.DeviceDetailResponseDto;
import com.gettgi.mvp.dto.response.VaccinDetailResponseDto;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Troupeau;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.Vaccin;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.repository.DeviceRepository;
import com.gettgi.mvp.repository.TroupeauRepository;
import com.gettgi.mvp.repository.UserRepository;
import com.gettgi.mvp.service.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final TroupeauRepository troupeauRepository;
    private final AnimalMapper animalMapper;
    @Override
    @Transactional(readOnly = true)
    public Page<FindAllAnimalResponseDto> FindAllAnimalsByUserTelephone(Pageable pageable, String telephone) {
        return animalRepository.findAllByUserTelephone(telephone, pageable)
                .map(animalMapper::toDto);
    }

    @Override
    public AnimalCreateResponseDto CreateAnimal(AnimalCreateRequestDto dto, String telephone) {

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));

        if (dto.device() != null && StringUtils.hasText(dto.device().imei())) {
            if (deviceRepository.existsByImei(dto.device().imei())) {
                throw new IllegalStateException("Un collier avec cet IMEI est déjà associé à un autre animal");
            }
        }

        // 2) mapper DTO -> entité
        Animal animal = animalMapper.toEntity(dto);
        animal.setUser(user);
        animal.setTroupeau(null); // règle: pas de troupeau à la création

        // back-reference vaccins
        if (animal.getVaccins() != null) {
            for (Vaccin v : animal.getVaccins()) {
                if (v != null) v.setAnimal(animal);
            }
        }

        // 3) sauvegarder l’animal (et vaccins via cascade)
        Animal saved = animalRepository.save(animal);

        // 4) gérer le device si fourni (owner side)
        Device device = animal.getDevice();
        if (device != null) {
            device.setAnimal(saved);
            deviceRepository.save(device);
        }

        // 5) mapper entité -> DTO réponse
        return animalMapper.toDto2(saved);
    }

    @Override
    public AnimalCreateResponseDto UpdateAnimal(UUID animalId, AnimalUpdateRequestDto dto, String telephone) {

        Animal animal = animalRepository.findByIdAndUserTelephone(animalId, telephone)
                .orElseThrow(() -> new IllegalStateException("Animal introuvable ou non autorisé"));

        if (dto.device() != null && StringUtils.hasText(dto.device().imei())) {
            deviceRepository.findByImei(dto.device().imei()).ifPresent(existingDevice -> {
                Device current = animal.getDevice();
                if (current == null || !existingDevice.getId().equals(current.getId())) {
                    throw new IllegalStateException("Un collier avec cet IMEI est déjà associé à un autre animal");
                }
            });
        }

        // update scalars via MapStruct
        animalMapper.updateEntityFromDto(dto, animal);

        // replace vaccins if provided
        if (dto.vaccins() != null) {
            List<Vaccin> managedVaccins = animal.getVaccins();
            if (managedVaccins == null) {
                managedVaccins = new ArrayList<>();
                animal.setVaccins(managedVaccins);
            } else {
                managedVaccins.clear();
            }

            for (var vDto : dto.vaccins()) {
                if (vDto == null) continue;
                Vaccin v = animalMapper.toEntity(vDto);
                v.setAnimal(animal);
                managedVaccins.add(v);
            }
        }

        // handle device
        if (dto.device() != null) {
            Device existing = animal.getDevice();
            if (existing == null) {
                Device device = animalMapper.toEntity(dto.device());
                device.setAnimal(animal);
                deviceRepository.save(device);
                animal.setDevice(device);
            } else {
                // update fields
                existing.setImei(dto.device().imei());
                existing.setFirmwareVersion(dto.device().firmwareVersion());
                existing.setActivationDate(dto.device().activationDate());
                existing.setStatusCollar(dto.device().statusCollar());
                deviceRepository.save(existing);
            }
        }

        Animal saved = animalRepository.save(animal);
        return animalMapper.toDto2(saved);
    }

    @Override
    public void DeleteAnimal(UUID animalId, String telephone) {
        Animal animal = animalRepository.findByIdAndUserTelephone(animalId, telephone)
                .orElseThrow(() -> new IllegalStateException("Animal introuvable ou non autorisé"));

        Device device = animal.getDevice();
        if (device != null) {
            device.setAnimal(null);
            deviceRepository.save(device);
        }

        animalRepository.delete(animal);
    }

    @Override
    public AnimalCreateResponseDto PatchAnimalTroupeau(UUID animalId, UUID troupeauId, String telephone) {
        Animal animal = animalRepository.findByIdAndUserTelephone(animalId, telephone)
                .orElseThrow(() -> new IllegalStateException("Animal introuvable ou non autorisé"));

        if (troupeauId == null) {
            animal.setTroupeau(null);
        } else {
            Troupeau troupeau = troupeauRepository.findByIdAndUserTelephone(troupeauId, telephone)
                    .orElseThrow(() -> new IllegalStateException("Troupeau introuvable ou non autorisé"));
            animal.setTroupeau(troupeau);
        }

        Animal saved = animalRepository.save(animal);
        return animalMapper.toDto2(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AnimalDetailResponseDto GetAnimalDetail(UUID animalId, String telephone) {
        Animal animal = animalRepository.findByIdAndUserTelephone(animalId, telephone)
                .orElseThrow(() -> new IllegalStateException("Animal introuvable ou non autorisé"));

        Device device = animal.getDevice();
        DeviceDetailResponseDto deviceDto = null;
        if (device != null) {
            deviceDto = new DeviceDetailResponseDto(
                    device.getImei(),
                    device.getFirmwareVersion(),
                    device.getActivationDate(),
                    device.getStatusCollar()
            );
        }

        List<VaccinDetailResponseDto> vaccinsDto = null;
        if (animal.getVaccins() != null) {
            vaccinsDto = new ArrayList<>();
            for (Vaccin v : animal.getVaccins()) {
                if (v != null) {
                    vaccinsDto.add(new VaccinDetailResponseDto(v.getTypeVaccin(), v.getDate()));
                }
            }
        }

        return new AnimalDetailResponseDto(
                animal.getId(),
                animal.getAge(),
                animal.getSexe(),
                animal.getTaille(),
                animal.getPoids(),
                animal.getStatut(),
                animal.getRole(),
                animal.getEspece(),
                animal.getNom(),
                animal.getUser() != null ? animal.getUser().getId() : null,
                deviceDto,
                vaccinsDto
        );
    }
}
