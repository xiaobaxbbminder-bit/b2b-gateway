package com.whatsoeversky.minder.rn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetCommConfigRespDto {
    private String id;
    private String personalityId;
    private String partnerId;
    private String pipDefinitionId;
    private String direction;
    private Boolean signingEnabled;
    private String signAlgorithm;
    private Boolean encryptionEnabled;
    private String encryptAlgorithm;

    private String personalityName;
    private String personalityDuns;
    private String partnerName;
    private String partnerDuns;
    private String pipId;
    private String pipVersion;
    private String documentType;
}
