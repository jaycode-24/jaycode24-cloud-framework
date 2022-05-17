package com.jaycode.framework.cloud.boot.core.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
@Slf4j
public class YamlReader {

    private final Map<String,Object> content;
    private final String originalContent;

    public YamlReader(String str) {
        this.originalContent = str;
        content = new Yaml().load(str);

    }

    @Nullable
    public <T> T get(String keyPath) {
        try {
            if (!StrUtil.contains(keyPath,".")){
                return (T) content.get(keyPath);
            }else {
                Map<String,Object> source = content;
                String[] keys = keyPath.split("\\.");
                for (int i = 0; i < keys.length-1; i++) {
                    source = (Map<String, Object>)source.get(keys[i]);
                }

                if (source == null){
                    return null;
                }
                return (T) source.get(keys[keys.length - 1]);
            }
        }catch (Exception e){
            log.debug("配置项:{}读取失败;", keyPath);
            return null;
        }
    }
}
