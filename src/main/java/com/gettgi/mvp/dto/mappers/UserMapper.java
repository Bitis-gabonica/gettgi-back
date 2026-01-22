package com.gettgi.mvp.dto.mappers;

import com.gettgi.mvp.dto.request.AuthRegisterRequestDto;
import com.gettgi.mvp.dto.response.AuthRegisterResponseDto;
import com.gettgi.mvp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password")
    User toEntity(AuthRegisterRequestDto authRegisterRequestDto);

    AuthRegisterResponseDto toRegisterResponse(User user);
}
