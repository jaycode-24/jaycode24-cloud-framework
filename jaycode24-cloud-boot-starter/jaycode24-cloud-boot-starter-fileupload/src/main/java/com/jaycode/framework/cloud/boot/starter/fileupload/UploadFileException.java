package com.jaycode.framework.cloud.boot.starter.fileupload;

public class UploadFileException extends StorageException {
    public UploadFileException() {
    }

    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadFileException(String message) {
        super(message);
    }
}
