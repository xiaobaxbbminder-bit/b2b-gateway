package com.whatsoeversky.minder.sftp.mapper;

import com.whatsoeversky.minder.sftp.dto.SftpServiceCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceUpdateReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpService;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SftpServiceMapper {

    SftpService toEntity(SftpServiceCreateReqDto dto);

    SftpServiceRespDto toRespDto(SftpService entity);

    void updateEntity(SftpServiceUpdateReqDto dto, @MappingTarget SftpService entity);
}
