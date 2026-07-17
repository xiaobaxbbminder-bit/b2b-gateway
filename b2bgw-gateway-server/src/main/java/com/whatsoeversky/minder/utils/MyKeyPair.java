package com.whatsoeversky.minder.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyKeyPair {
    private String privateKey;
    private String publicKey;
}
