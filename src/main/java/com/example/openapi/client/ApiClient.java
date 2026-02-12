package com.example.openapi.client;

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

/**
 * HashEx API客户端
 */
public class ApiClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final String baseUrl;
    private final String accessKey;
    private final String secretKey;
    private final CloseableHttpClient httpClient;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.accessKey = null;
        this.secretKey = null;
        this.httpClient = HttpClients.createDefault();
    }


    /**
     * 构造函数
     *
     * @param baseUrl API基础URL
     * @param accessKey 访问密钥
     * @param secretKey 秘密密钥
     */
    public ApiClient(String baseUrl, String accessKey, String secretKey) {
        this.baseUrl = baseUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 发送GET请求
     *
     * @param endpoint API端点
     * @param queryParams 查询参数
     * @return 响应字符串
     * @throws HashExApiException 如果API调用失败
     */
    public String sendGetRequest(String endpoint, TreeMap<String, String> queryParams,boolean needAuth) throws HashExApiException {
        try {
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

            // 添加认证头
            if (needAuth) {
                HashexApiUtils.addAuthHeaders(httpGet, accessKey, secretKey, queryParams);
            }

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("GET请求成功，endpoint: {}, 响应: {}", endpoint, responseBody);
                    return responseBody;
                } else {
                    throw new HashExApiException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new HashExApiException("执行GET请求时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 发送POST请求
     *
     * @param endpoint API端点
     * @param queryParams 查询参数
     * @return 响应字符串
     * @throws HashExApiException 如果API调用失败
     */
    public String sendPostRequest(String endpoint, TreeMap<String, String> queryParams) throws HashExApiException {
        try {
            String endPointUrl = baseUrl + endpoint;

            URIBuilder uriBuilder = new URIBuilder(endPointUrl);
            // 添加查询参数到URI
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                logger.info("添加查询参数: {} = {}", entry.getKey(), entry.getValue());
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }

            // 构建最终URL
            URI uri = uriBuilder.build();

            // 创建POST请求
            HttpPost httpPost = new HttpPost(uri);

            // 添加认证头
            HashexApiUtils.addAuthHeaders(httpPost, accessKey, secretKey, queryParams);

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("POST请求成功，endpoint: {}, 响应: {}", endpoint, responseBody);
                    return responseBody;
                } else {
                    throw new HashExApiException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new HashExApiException("执行POST请求时出错: " + e.getMessage(), e);
        }
    }


    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
