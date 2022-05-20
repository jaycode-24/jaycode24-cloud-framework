package com.jaycode.framework.cloud.boot.starter.fileupload.request;

import lombok.Data;

@Data
public class ImageRequest extends ReadRequest {
    //图片宽度(缩略图)
    private Integer width;
    //图片高度(缩略图)
    private Integer height;

}
