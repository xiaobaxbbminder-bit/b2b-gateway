package com.whatsoeversky.minder.sftp.mapper;

import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigRespDto;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SftpServiceConfigMapper {

    SftpServiceConfig toEntity(SftpServiceConfigReqDto dto);

    SftpServiceConfigRespDto toRespDto(SftpServiceConfig entity);

    void updateEntity(SftpServiceConfigReqDto dto, @MappingTarget SftpServiceConfig entity);
}
