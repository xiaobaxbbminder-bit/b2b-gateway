package com.whatsoeversky.minder.agent.dto;

import lombok.Data;

@Data
public class ChatReqDto {
    private String message;
    private String conversationId;
}
