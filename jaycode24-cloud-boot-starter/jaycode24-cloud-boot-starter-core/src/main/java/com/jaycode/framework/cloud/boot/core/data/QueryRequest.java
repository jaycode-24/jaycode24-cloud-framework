package com.jaycode.framework.cloud.boot.core.data;

import java.io.Serializable;

/**
 * 通用查询request设计，允许分页参数可选设置
 *
 * @author jinlong.wang
 */
public abstract class QueryRequest implements PageableRequest, Serializable {
    private static final long serialVersionUID = -750158092620366738L;
    private PageRequest pageRequest;

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
