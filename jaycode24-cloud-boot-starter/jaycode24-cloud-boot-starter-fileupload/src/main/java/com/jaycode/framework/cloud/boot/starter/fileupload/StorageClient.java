package com.jaycode.framework.cloud.boot.starter.fileupload;

import com.jaycode.framework.cloud.boot.starter.fileupload.request.ReadRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
public interface StorageClient {
    void init() throws Exception;

    void deleteFile(String id);

    void downloadFile(ReadRequest fileReadRequest, OutputStream outputStream);

    Map<String, Object> downloadMetadata(String id);

    String upload(String originalName, InputStream inputStream, Map<String, String> metadata, String group);

    void destroy() throws Exception;
}
