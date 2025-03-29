package com.example.openapi;

import com.example.openapi.utils.HashexApiUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

public class HashexApiClientDemo {
    private static final Logger logger = LoggerFactory.getLogger(HashexApiClientDemo.class);
    private final String baseUrl;
    private final String accessKey;
    private final String secretKey;
    private final CloseableHttpClient httpClient;

    public HashexApiClientDemo(String baseUrl, String accessKey, String secretKey) {
        this.baseUrl = baseUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.httpClient = HttpClients.createDefault();
    }


    public String sendGetRequest(String endpoint, TreeMap<String, String> queryParams) throws Exception {
        String endPointUrl = baseUrl + endpoint;

        URIBuilder uriBuilder = new URIBuilder(endPointUrl);
        // 添加查询参数到URI
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        // 构建最终URL
        URI uri = uriBuilder.build();

        // 创建GET请求
        HttpGet httpGet = new HttpGet(uri);

        // 使用工具类添加认证头
        HashexApiUtils.addAuthHeaders(httpGet, accessKey, secretKey, queryParams);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode >= 200 && statusCode < 300) {
                logger.info("请求成功，responseBody: " + responseBody);
                return responseBody;
            } else {
                throw new RuntimeException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        }
    }

    public String sendPostRequest(String endpoint, TreeMap<String, String> queryParams) throws Exception {
        String endPointUrl = baseUrl + endpoint;

        URIBuilder uriBuilder = new URIBuilder(endPointUrl);
        // 添加查询参数到URI
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        // 构建最终URL
        URI uri = uriBuilder.build();

        // 创建POST请求
        HttpPost httpPost = new HttpPost(uri);

        // 使用工具类添加认证头
        HashexApiUtils.addAuthHeaders(httpPost, accessKey, secretKey, queryParams);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode >= 200 && statusCode < 300) {
                logger.info("请求成功，responseBody: " + responseBody);
                return responseBody;
            } else {
                throw new RuntimeException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        }
    }

    // 释放资源
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("关闭HTTP客户端时出错", e);
        }
    }

    // 其他API方法保持不变
    // ...


    public String getSpotBalance(String coin) throws Exception {
        TreeMap<String, String> queryParams = new TreeMap<>();
        if (coin != null && !coin.isEmpty()) {
            queryParams.put("coin", coin);
        }
        return sendGetRequest("/spot/v1/u/balance/spot", queryParams);
    }

    public static void main(String[] args) {
        HashexApiClientDemo apiClient = new HashexApiClientDemo("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");

        try {
            apiClient.getSpotBalance(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}