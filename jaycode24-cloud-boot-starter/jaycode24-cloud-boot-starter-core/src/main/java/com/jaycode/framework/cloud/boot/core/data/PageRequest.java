package com.jaycode.framework.cloud.boot.core.data;


import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @author jinlong.wang
 */
@Data
@NoArgsConstructor
@Slf4j
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -3549182464193036563L;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private static Integer maxPageSize = null;


    public PageRequest(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public final boolean isAvailable() {
        if (maxPageSize == null) {
            synchronized (this) {
                if (maxPageSize == null) {
                    try {
                        PageRequest.maxPageSize = ConfigConst.getMaxPageSize();
                        Assert.isTrue(maxPageSize > 0 && maxPageSize < 10000, "不合理的单页大小限制，请酌情考虑");
                    } catch (Exception e) {
                        PageRequest.maxPageSize = 100;
                    }
                    log.info("当前应用分页参数设置：单页最大条数限制为:{}条", PageRequest.maxPageSize);
                }

            }
        }
        //限制分页查询上限为500条记录
        return pageNo != null && pageSize != null && pageSize < maxPageSize;
    }

    public int getStartRow() {
        return pageNo > 0 ? (pageNo - 1) * pageSize : 0;
    }

    public int getEndRow() {
        return this.getStartRow() + pageSize * (pageNo > 0 ? 1 : 0);
    }

}
