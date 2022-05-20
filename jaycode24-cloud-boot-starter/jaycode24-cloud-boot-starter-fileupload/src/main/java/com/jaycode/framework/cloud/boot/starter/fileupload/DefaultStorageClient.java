package com.jaycode.framework.cloud.boot.starter.fileupload;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.starter.fileupload.request.ImageRequest;
import com.jaycode.framework.cloud.boot.starter.fileupload.request.ReadRequest;
import com.jaycode.framework.cloud.boot.core.data.Response;
import com.jaycode.framework.cloud.boot.core.support.UrlPath;
import com.jaycode.framework.cloud.boot.core.support.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
@Slf4j
public class DefaultStorageClient implements StorageClient{

    private CloseableHttpClient httpclient;
    private PoolingHttpClientConnectionManager cm;
    private final ServiceInstanceChooser serviceInstanceChooser;

    private static final ContentType DEFAULT_TEXT_PLAIN = ContentType.TEXT_PLAIN.withCharset(Charset.forName("utf-8"));

    public DefaultStorageClient(ServiceInstanceChooser loadBalancerClient) {
        this.serviceInstanceChooser = loadBalancerClient;
    }


    @Override
    public void init() throws Exception {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal((Runtime.getRuntime().availableProcessors() * 2 + 1) * 3);
        cm.setDefaultMaxPerRoute(Runtime.getRuntime().availableProcessors() * 2 + 1);
        httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Override
    public void deleteFile(String id) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httppost = new HttpPost(getStorageHost() + "/storage/delete?id=" + id);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new StorageException("删除失败，存储服务返回状态码:" + response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            //log.error(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private String getStorageHost() {
        ServiceInstance serviceInstance = serviceInstanceChooser.choose("file");
        if (serviceInstance == null) {
            throw new IllegalArgumentException("文件服务尚未启动，请稍后再试");
        }
        return "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort();
    }

    @Override
    public void downloadFile(ReadRequest fileReadRequest, OutputStream outputStream) {
        CloseableHttpResponse response = null;
        try {
            UrlPath urlPath = new UrlPath(getStorageHost() + "/storage/read?id=" + fileReadRequest.getFileId());
            if(fileReadRequest instanceof ImageRequest){
                ImageRequest imageRequest=(ImageRequest) fileReadRequest;
                if (imageRequest.getHeight() != null && imageRequest.getHeight() > 0) {
                    urlPath.addParameter("width", imageRequest.getHeight().toString());
                }
                if (imageRequest.getWidth() != null && imageRequest.getWidth() > 0) {
                    urlPath.addParameter("width", imageRequest.getWidth().toString());
                }
            }

            HttpPost httppost = new HttpPost(urlPath.toString());
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            response = httpclient.execute(httppost);
            //检验服务器返回消息
            Header contentType = response.getFirstHeader("Content-Type");
            if (contentType != null && contentType.getValue().contains("application/json")) {
                Response ret = Json.parse(IOUtils.toString(response.getEntity().getContent()), Response.class);
                //log.error(() -> "不符合预期的文件上传结果:" + Json.toString(ret));
                throw new StorageException("文件下载失败，" + ret.getMessage() + "[状态码:" + ret.getStatus() + "]");
            }
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                IOUtils.copy(resEntity.getContent(), outputStream);
                EntityUtils.consume(resEntity);
            }

        } catch (Exception e) {
            //log.error(e);
            if (e instanceof StorageException) {
                throw (StorageException) e;
            } else {
                throw new StorageException("文件下载失败");
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public Map<String, Object> downloadMetadata(String id) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httppost = new HttpPost(getStorageHost() + "/storage/readMetadata?id=" + id);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                return Json.parse(IOUtils.toString(resEntity.getContent())).getItem("data");
            }
        } catch (Exception e) {
            //log.error(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return new HashMap<>();
    }

    @Override
    public String upload(String originalName, InputStream inputStream, Map<String, String> metadata, String group) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httppost = new HttpPost(getStorageHost() + "/storage/write");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            InputStreamBody bin = new InputStreamBody(inputStream, originalName);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().addPart("file", bin)
                    .addPart("filename", new StringBody(originalName, DEFAULT_TEXT_PLAIN))
                    .addPart("metadata", new StringBody(Json.toString(metadata), DEFAULT_TEXT_PLAIN));
            if (!StrUtil.isEmpty(group)) {
                builder.addPart("group", new StringBody(group, DEFAULT_TEXT_PLAIN));
            }
            httppost.setEntity(builder.build());
            response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String responseEntityStr = EntityUtils.toString(response.getEntity());
                Response uploadResponse = Json.parse(responseEntityStr, Response.class);
                if (uploadResponse.isSuccess()) {
                    return ((Map<String, String>) uploadResponse.getData()).get("id");
                }
            }
            EntityUtils.consume(resEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        throw new UploadFileException("上传失败，存储服务状态错误");
    }

    @Override
    public void destroy() throws Exception {
        HttpClientUtils.closeQuietly(httpclient);
        try {
            cm.close();
        } catch (Exception e) {
            //ignore
        }

    }
}
