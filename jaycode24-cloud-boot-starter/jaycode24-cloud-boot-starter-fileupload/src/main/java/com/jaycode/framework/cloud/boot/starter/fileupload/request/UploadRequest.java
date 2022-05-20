package com.jaycode.framework.cloud.boot.starter.fileupload.request;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.Map;

@Data
@Builder
public class UploadRequest {
    private String originalName;
    private String group;
    private InputStream content;
    private Map<String, String> metadata;
}
