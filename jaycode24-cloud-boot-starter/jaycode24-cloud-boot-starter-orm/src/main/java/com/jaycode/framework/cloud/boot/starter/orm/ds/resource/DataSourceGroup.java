package com.jaycode.framework.cloud.boot.starter.orm.ds.resource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.xa.DruidXADataSource;
import lombok.Data;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * 数据源架构，主备
 * @author cheng.wang
 * @date 2022/5/16
 */
@Data
public class DataSourceGroup {

    private List<DataSource> masters = new ArrayList<>();
    private List<DataSource> salves = new ArrayList<>();

    //sql方言
    private String dialect;

    public void add(String key, DataSource dataSource){
        if (key.startsWith("sds")){
            getSalves().add(dataSource);
        }else {
            getMasters().add(dataSource);
        }
    }


    public void close(){
        masters.addAll(salves);
        for (DataSource dataSource : masters) {
            try {
                if (dataSource instanceof DruidXADataSource){
                    ((DruidXADataSource) dataSource).close();
                }
                if (dataSource instanceof DruidDataSource){
                    ((DruidDataSource) dataSource).close();
                }
            }catch (Exception e){
                //do nothing

            }
        }
    }
}

