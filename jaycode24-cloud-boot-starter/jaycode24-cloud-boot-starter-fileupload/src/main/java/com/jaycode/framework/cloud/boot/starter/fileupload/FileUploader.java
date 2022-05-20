package com.jaycode.framework.cloud.boot.starter.fileupload;

import com.jaycode.framework.cloud.boot.starter.fileupload.request.ImageRequest;
import com.jaycode.framework.cloud.boot.starter.fileupload.request.UploadRequest;
import com.google.common.collect.ImmutableMap;
import com.jaycode.framework.cloud.boot.core.support.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
public class FileUploader {

    private final StorageClient storageClient;

    public FileUploader(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    public String upload(String originalName, InputStream inputStream) {
        return upload(UploadRequest.builder()
                .content(inputStream)
                .originalName(originalName)
                .metadata(ImmutableMap.<String, String>builder().put("name", originalName).build()
                ).build());
    }

    public String upload(UploadRequest uploadRequest) {
        checkFileExtension(uploadRequest.getOriginalName());
        return storageClient.upload(uploadRequest.getOriginalName(),
                uploadRequest.getContent(),
                uploadRequest.getMetadata(),
                uploadRequest.getGroup());
    }


    /**
     * 下载图片，支持缩略图策略
     *
     * @param imageRequest 图片下载请求
     * @param outputStream 输出流
     */
    public void download(ImageRequest imageRequest, OutputStream outputStream) {
        storageClient.downloadFile(imageRequest, outputStream);
    }

    public void download(String fileId, OutputStream outputStream) {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.setFileId(fileId);
        storageClient.downloadFile(imageRequest, outputStream);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    public void delete(String fileId) {
        storageClient.deleteFile(fileId);
    }


    /**
     * 获取原始文件名，通过文件元数据获取
     *
     * @param fileId 文件ID
     * @return 文件名称
     */
    public String getOriginFileName(String fileId) {
        Map<String, Object> metadata = storageClient.downloadMetadata(fileId);
        return (String) metadata.get("name");
    }

    /**
     * 创建文件名
     * 1 先读取原始文件名
     * 2 如果原始文件名为空，则新创建文件名
     *
     * @param fileId 文件ID
     * @return 文件名
     */
    public String generateFileName(String fileId) {
        String originFileName = getOriginFileName(fileId);
        if (StringUtils.isEmpty(originFileName)) {
            String ext = FilenameUtils.getExtension(fileId);
            originFileName = UUID.randomUUID().toString() + "." + ext;
        }
        return originFileName;
    }

    private void checkFileExtension(String fileName) {
        Assert.hasText(fileName, "文件名不能为空");
        String ext = FileUtils.getFileExtension(fileName);
        if (StringUtils.isEmpty(ext)) {
            throw new UploadFileException("文件扩展名不能为空");
        }
    }
}
