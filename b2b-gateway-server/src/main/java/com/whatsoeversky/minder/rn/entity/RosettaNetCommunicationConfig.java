package com.whatsoeversky.minder.rn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rn_comm_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetCommunicationConfig {
    @Id
    private String id;
    private String personalityId;
    private String partnerId;
    private String pipDefinitionId;
    private String direction;
    private Boolean signingEnabled;
    private String signAlgorithm;
    private Boolean encryptionEnabled;
    private String encryptAlgorithm;
}
