package com.whatsoeversky.minder.rn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetCommConfigReqDto {
    private String personalityId;
    private String partnerId;
    private String pipDefinitionId;
    private String direction;
    private Boolean signingEnabled;
    private String signAlgorithm;
    private Boolean encryptionEnabled;
    private String encryptAlgorithm;
}
