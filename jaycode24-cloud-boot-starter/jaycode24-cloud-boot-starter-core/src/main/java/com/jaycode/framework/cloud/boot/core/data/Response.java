package com.jaycode.framework.cloud.boot.core.data;

import com.jaycode.framework.cloud.boot.core.BusinessStatus;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 通用客户端返回对象,与Http状态码复用
 *
 * @author jinlong.wang
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1371327411447075035L;
    //状态码，正常为200，其他均为异常
    private Integer status = 200;
    //消息，正常为success
    private String message = "success";
    //数据扩展
    private Object data;
    //消息代码，标识消息
    private String code;

    public static final Response SUCCESS = new Response(200, "success");

    public static Response fail(String errorMessage){
        Response response = new Response();
        response.setStatus(400);
        response.setMessage(errorMessage);
        return response;
    }

    public static Response valueOfObject(Object data) {
        return Response.builder().status(SUCCESS.getStatus()).message(SUCCESS.getMessage()).object(data).build();
    }

    public static Response valueOfList(List list) {
        return Response.builder().status(SUCCESS.getStatus()).message(SUCCESS.getMessage()).list(list).build();
    }

    public static Response valueOfCode(String code) {
        return Response.builder().status(SUCCESS.getStatus()).code(code).build();
    }

    public static Response valueOfStatus(Integer status) {
        return Response.builder().status(status).build();
    }

    public static Response valueOfMsg(String msg) {
        return Response.builder().status(SUCCESS.getStatus()).message(msg).build();
    }

    public static <T, R> Response valueOfMap(Consumer<Map<String, Object>> action) {
        Map<String, Object> map = new HashMap<>();
        action.accept(map);
        return Response.builder().status(SUCCESS.getStatus()).message(SUCCESS.getMessage()).object(map).build();
    }

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public Integer getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status.equals(SUCCESS.getStatus());
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Response() {
    }

    public Response(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(Integer status, String msg, Object data) {
        this.status = status;
        this.message = msg;
        this.data = data;
    }

    public Response(Integer status, String msg, Object data, String code) {
        this.status = status;
        this.message = msg;
        this.data = data;
        this.code = code;
    }

    public static class ResponseBuilder {
        private Integer status = SUCCESS.status;
        private String message = SUCCESS.message;
        private Object data;
        private String code;
        private Map<String, Object> map = new HashMap<>();

        ResponseBuilder() {
        }

        public ResponseBuilder status(Integer code) {
            this.status = code;
            return this;
        }

        public ResponseBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ResponseBuilder status(BusinessStatus businessStatus) {
            this.status = businessStatus.getStatus();
            this.message = businessStatus.getMessage();
            return this;
        }

        public ResponseBuilder message(String msg) {
            this.message = msg;
            return this;
        }
        public ResponseBuilder errorMessage(String msg){
            this.message = msg;
            this.status= HttpStatus.BAD_REQUEST.value();
            return this;
        }

        public ResponseBuilder list(List list) {
            Map<String, Object> paged = new HashMap<>();
            paged.put("list", list);
            if (list instanceof PagedList) {
                paged.put("total", ((PagedList) list).getTotal());
            }
            data = paged;
            return this;
        }

        public ResponseBuilder object(Object data) {
            this.data = data;
            return this;
        }

        public ResponseBuilder map(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public ResponseBuilder map(Consumer<Map<String, Object>> action) {
            action.accept(map);
            this.data = map;
            return this;
        }

        public Response build() {
            if (this.data == null && (map != null && map.size() > 0)) {
                return new Response(this.status, this.message, this.map, this.code);
            }
            return new Response(this.status, this.message, this.data, this.code);
        }

        public String toString() {
            return "Response.ResponseBuilder(code=" + this.status + ", msg=" + this.message + ", data=" + this.data + ")";
        }
    }

}
