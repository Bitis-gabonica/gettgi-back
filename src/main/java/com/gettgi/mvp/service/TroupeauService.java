package com.gettgi.mvp.service;

import com.gettgi.mvp.dto.response.TroupeauDetailResponseDto;

import java.util.UUID;

public interface TroupeauService {

    TroupeauDetailResponseDto GetTroupeauDetail(UUID troupeauId, String telephone);

    org.springframework.data.domain.Page<com.gettgi.mvp.dto.response.TroupeauResponseDto> FindAllByUserTelephone(org.springframework.data.domain.Pageable pageable, String telephone);

    com.gettgi.mvp.dto.response.TroupeauResponseDto CreateTroupeau(com.gettgi.mvp.dto.request.TroupeauCreateRequestDto dto, String telephone);

    com.gettgi.mvp.dto.response.TroupeauResponseDto UpdateTroupeau(java.util.UUID troupeauId, com.gettgi.mvp.dto.request.TroupeauUpdateRequestDto dto, String telephone);

    void DeleteTroupeau(java.util.UUID troupeauId, String telephone);
}
