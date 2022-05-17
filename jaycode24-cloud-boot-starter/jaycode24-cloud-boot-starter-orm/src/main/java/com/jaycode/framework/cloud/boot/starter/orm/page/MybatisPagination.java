package com.jaycode.framework.cloud.boot.starter.orm.page;

import java.util.List;

/**
 * 分页信息对象，在Pagination对象上面，封装了Mybatis 相关分页所需特性
 * @author cheng.wang
 * @date 2022/5/17
 */
public class MybatisPagination {


    private static final ThreadLocal<Integer> PAGE_COUNTER_HOLDER = new ThreadLocal<Integer>();

    private final PageRequest pagination;

    private final Object parameterObject;

    @Deprecated
    public MybatisPagination(QueryRequest query) {
        pagination = query.getPageRequest();
        parameterObject = query;
    }

    @Deprecated
    public MybatisPagination(PageRequest query) {
        pagination = query;
        parameterObject = query;
    }

    public MybatisPagination(Object param, PageRequest pageRequest) {
        pagination = pageRequest;
        parameterObject = param;
    }

    public PageRequest getPagination() {
        return pagination;
    }

    public int getEndRow() {
        return pagination.getEndRow();
    }

    public static boolean isPagingResult() {
        return PAGE_COUNTER_HOLDER.get() != null;
    }

    public int getPageSize() {
        return pagination.getPageSize();
    }

    public int getStartRow() {
        return pagination.getStartRow();
    }


    public static <T> PagedList<T> toPagingList(List<T> result) {
        PagedList<T> pagedList = new PagedList<T>(result, PAGE_COUNTER_HOLDER.get());
        PAGE_COUNTER_HOLDER.remove();
        return pagedList;
    }

    public static void setTotalCount(int anInt) {
        PAGE_COUNTER_HOLDER.set(anInt);
    }


    public Object getParameterObject() {
        return parameterObject;
    }
}
