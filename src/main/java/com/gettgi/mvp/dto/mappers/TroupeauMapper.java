package com.gettgi.mvp.dto.mappers;

import com.gettgi.mvp.dto.request.TroupeauCreateRequestDto;
import com.gettgi.mvp.dto.request.TroupeauUpdateRequestDto;
import com.gettgi.mvp.dto.response.FindAllTroupeauResponseDto;
import com.gettgi.mvp.dto.response.TroupeauResponseDto;
import com.gettgi.mvp.entity.Troupeau;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TroupeauMapper {

    FindAllTroupeauResponseDto toDto(Troupeau entity);

    TroupeauResponseDto toDto2(Troupeau entity);

    Troupeau toEntity(TroupeauCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "animals", ignore = true)
    void updateEntityFromDto(TroupeauUpdateRequestDto dto, @MappingTarget Troupeau entity);
}
