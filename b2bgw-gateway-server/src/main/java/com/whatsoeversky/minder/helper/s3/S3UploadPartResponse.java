package com.whatsoeversky.minder.helper.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3UploadPartResponse {
    private int partNumber;
    private String etag;
}
