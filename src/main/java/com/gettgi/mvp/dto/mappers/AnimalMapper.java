package com.gettgi.mvp.dto.mappers;


import com.gettgi.mvp.dto.request.AnimalCreateRequestDto;
import com.gettgi.mvp.dto.request.DeviceCreateRequestDto;
import com.gettgi.mvp.dto.request.VaccinCreateRequestDto;
import com.gettgi.mvp.dto.request.AnimalUpdateRequestDto;
import com.gettgi.mvp.dto.response.AnimalCreateResponseDto;
import com.gettgi.mvp.dto.response.FindAllAnimalResponseDto;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Vaccin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AnimalMapper {

    @Mapping(target = "troupeauId", source = "troupeau.id")
    FindAllAnimalResponseDto toDto(Animal animal);

    @Mapping(target = "user", source = "user.id")
    AnimalCreateResponseDto toDto2(Animal animal);


    Animal toEntity(AnimalCreateRequestDto dto);

    // Nested mappings for create
    Vaccin toEntity(VaccinCreateRequestDto dto);

    Device toEntity(DeviceCreateRequestDto dto);

    // keep nested mapping handled in service for detail endpoint

    // Update scalars via MapStruct, handle relations manually in service
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "troupeau", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "vaccins", ignore = true)
    void updateEntityFromDto(AnimalUpdateRequestDto dto, @MappingTarget Animal entity);

}
