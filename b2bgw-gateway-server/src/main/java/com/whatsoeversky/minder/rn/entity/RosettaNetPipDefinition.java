package com.whatsoeversky.minder.rn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rn_pip_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetPipDefinition {
    @Id
    private String id;
    private String pipId;
    private String pipVersion;
    private String documentType;
    private String description;
}
