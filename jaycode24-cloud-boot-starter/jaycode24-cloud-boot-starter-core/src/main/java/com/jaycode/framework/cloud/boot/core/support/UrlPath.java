package com.jaycode.framework.cloud.boot.core.support;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * URL处理工具类
 *
 * @author jinlong.wang
 */
public class UrlPath {
    private static final String URL_QUERY_SEPARATOR = "?";
    private static final String PATH_KEYWORD = "/";
    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String PROTOCOL_SUB_SEPARATOR = ":";
    private static final String URL_PARAMETER_SEPARATOR = "&";
    private static final String URL_PARAMETER_EQ = "=";
    private static final int LEFT_PART = 0;
    private static final int RIGHT_PART = 1;
    /**
     * URL 资源部分，从url[0]-url[indexOf(?)]
     */
    private String uri;
    /**
     * URL中的主机名
     */
    private String host;
    /**
     * URL参数列表
     */
    private List<Parameter> parameterList;
    private boolean parameterValueUrlEncode;
    /**
     * URL 协议
     */
    private String protocol;
    /**
     * URL 中的端口号
     */
    private int port;


    public UrlPath(String url) {
        this(url, false);
    }

    public UrlPath(String url, boolean parameterValueUrlEncode) {
        this.parameterList = new ArrayList<>();
        this.parameterValueUrlEncode = parameterValueUrlEncode;
        String[] leftRight;//如果url包含资源与参数则进行分离
        String resource = url;
        String query = "";
        if (resource.contains(URL_QUERY_SEPARATOR) && !resource.endsWith(URL_QUERY_SEPARATOR)) {
            String[] uriParts = resource.split("\\" + URL_QUERY_SEPARATOR);
            resource = uriParts[LEFT_PART];
            query = uriParts[RIGHT_PART];
        }
        if (resource.contains(PROTOCOL_SEPARATOR)) {
            leftRight = resource.split(PROTOCOL_SEPARATOR);
            this.protocol = leftRight[LEFT_PART];
            int firstPathDirIndex;
            if ((firstPathDirIndex = leftRight[RIGHT_PART].indexOf(PATH_KEYWORD)) > -1) {
                this.host = leftRight[RIGHT_PART].substring(0, firstPathDirIndex);
                this.uri = leftRight[RIGHT_PART].substring(firstPathDirIndex);
            } else {
                this.host = leftRight[RIGHT_PART];
            }
            String[] pats = this.getHost().split(PROTOCOL_SUB_SEPARATOR);
            if (pats.length > 1) {
                this.host = pats[LEFT_PART];
                this.port = Integer.valueOf(pats[RIGHT_PART]);
            }


        } else if (resource.contains(PATH_KEYWORD)) {
            this.uri = resource;
        } else if (resource.contains(URL_PARAMETER_SEPARATOR)) {
            query = resource;
        } else {
            this.uri = resource;
        }
        this.addQueryString(query);

    }

    public UrlPath removeParameter(String key) {
        parameterList.removeIf(p -> p.getName().equals(key));
        return this;
    }

    public UrlPath clearParameters() {
        parameterList.clear();
        return this;
    }

    /**
     * 新增参数
     *
     * @param key   参数名
     * @param value 参数值
     * @return URL构建器
     */
    public UrlPath addParameter(String key, String value) {
        this.parameterList.add(new Parameter(key, value));
        return this;
    }

    /**
     * 新增参数对
     *
     * @param map 参数名
     * @return URL构建器
     */
    public UrlPath addParameters(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return this;
        }
        map.forEach((k, v) -> {
            this.parameterList.add(new Parameter(k, v));
        });

        return this;
    }

    /**
     * 设置参数值
     * @param key 参数名
     * @param value 参数值
     * @return  URL构建器
     */
    public UrlPath setParameterValue(String key,String value){
        removeParameter(key);
        addParameter(key,value);
        return this;
    }

    /**
     * 按照a=1&b=2 的字符串形式添加参数
     *
     * @param queryString URL参数字符串
     * @return URL构建器
     */
    public UrlPath addQueryString(String queryString) {
        if (!StringUtils.isEmpty(queryString)) {
            if (queryString.startsWith(URL_PARAMETER_SEPARATOR)) {
                throw new IllegalArgumentException("query string should not start with &");
            }
            if (queryString.contains(URL_PARAMETER_EQ)) {
                String[] urlParameterArray = queryString.split(URL_PARAMETER_SEPARATOR);
                for (String urlParameter : urlParameterArray) {
                    String[] urlParameterParts = urlParameter.split(URL_PARAMETER_EQ);
                    if (urlParameterParts.length == 2) {
                        this.parameterList.add(new Parameter(urlParameterParts[0], urlParameterParts[1]));
                    }
                }
            }
        }

        return this;
    }

    public String getParameterValue(String key) {
        for (Parameter parameter : parameterList) {
            if (parameter.getName().equals(key)) {
                return parameter.getValue();
            }
        }
        return null;
    }

    public String getQueryString() {
        if (CollectionUtils.isEmpty(parameterList)) {
            return "";
        }
        StringBuilder fullUrlBuilder = new StringBuilder();
        parameterList.forEach((parameter -> {
            fullUrlBuilder.append(parameter.getName()).append("=").append(this.parameterValueUrlEncode ? DigestUtils.urlEncode(parameter.getValue()).toLowerCase() : parameter.getValue()).append("&");

        }));
        if (fullUrlBuilder.charAt(fullUrlBuilder.length() - 1) == '&') {
            fullUrlBuilder.deleteCharAt(fullUrlBuilder.length() - 1);
        }

        return fullUrlBuilder.toString();
    }


    public void copyParams(HttpServletRequest request) {
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (request.getParameter(param) != null && request.getParameter(param).length() > 0) {
                addParameter(param, request.getParameter(param));
            }
        }
    }

    public static class Parameter {
        private String name;
        private String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public String toString() {
        StringBuilder fullUrlBuilder = new StringBuilder();
        if (StringUtils.hasLength(protocol)) {
            fullUrlBuilder.append(protocol).append(PROTOCOL_SEPARATOR);
        }
        if (StringUtils.hasLength(host)) {
            fullUrlBuilder.append(host);
        }
        if (fullUrlBuilder.length() > 0) {
            fullUrlBuilder.append((port > 0 ? ":" + port : ""));
        }
        if (StringUtils.hasLength(uri)) {
            fullUrlBuilder.append(uri);
        }
        if (this.parameterList.size() > 0) {
            if (fullUrlBuilder.length() > 0) {
                return fullUrlBuilder.append("?").append(this.getQueryString()).toString();
            } else {
                return this.getQueryString();
            }
        } else {
            return fullUrlBuilder.toString();
        }
    }
}
