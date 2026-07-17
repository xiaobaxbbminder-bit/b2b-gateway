package com.whatsoeversky.minder.sftp.mapper;

import com.whatsoeversky.minder.sftp.dto.SftpUserCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserUpdateReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SftpUserMapper {

    SftpUser toEntity(SftpUserCreateReqDto dto);

    SftpUserRespDto toRespDto(SftpUser entity);

    void updateEntity(SftpUserUpdateReqDto dto, @MappingTarget SftpUser entity);
}
