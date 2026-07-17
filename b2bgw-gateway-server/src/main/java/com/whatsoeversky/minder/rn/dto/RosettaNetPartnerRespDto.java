package com.whatsoeversky.minder.rn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetPartnerRespDto {
    private String id;
    private String name;
    private String duns;
    private String url;
    private String signCert;
    private String encryptCert;
}
