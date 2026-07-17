package com.whatsoeversky.minder.rn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetPipRespDto {
    private String id;
    private String pipId;
    private String pipVersion;
    private String documentType;
    private String description;
}
