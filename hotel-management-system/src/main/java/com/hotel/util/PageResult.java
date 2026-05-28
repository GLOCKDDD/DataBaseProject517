package com.hotel.util;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {
    private long total;
    private List<T> records;
    private int pageNum;
    private int pageSize;

    public PageResult() {}

    public PageResult(long total, List<T> records, int pageNum, int pageSize) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
