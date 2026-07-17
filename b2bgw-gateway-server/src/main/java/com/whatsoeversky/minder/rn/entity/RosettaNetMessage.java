package com.whatsoeversky.minder.rn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "rn_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RosettaNetMessage {
    @Id
    private String id;
    private String direction;
    private String personalityDuns;
    private String partnerDuns;
    private String pipId;
    private String pipVersion;
    private String messageId;
    private String status;
    private String payload;
    private String responsePayload;
    private LocalDateTime createdAt;
}
