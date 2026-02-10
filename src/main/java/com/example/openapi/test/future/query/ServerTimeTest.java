package com.example.openapi.test.future.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class ServerTimeTest {

    private static final Logger log = LoggerFactory.getLogger(ServerTimeTest.class);
    private static ApiClient apiClient;

    /**
     * 获取服务器时间
     *
     * @return 服务器时间（毫秒时间戳）
     * @throws HashExApiException 如果API调用失败
     */
    public Long getServerTime() throws HashExApiException {
        try {
            // 创建空的查询参数Map（此接口不需要参数）
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/public/time", queryParams, false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<Long> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Long>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取服务器时间失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取服务器时间时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取服务器时间
     */
    private void testGetServerTime() throws HashExApiException {
        log.info("===== 获取服务器时间测试 =====");

        // 获取服务器时间
        Long serverTime = getServerTime();

        // 转换为可读的日期时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = dateFormat.format(new Date(serverTime));

        log.info("服务器时间戳: {}", serverTime);
        log.info("格式化时间: {}", formattedDate);

        // 计算与本地时间的时差
        long localTime = System.currentTimeMillis();
        long timeDiff = localTime - serverTime;

        log.info("本地时间戳: {}", localTime);
        log.info("本地与服务器时差: {}毫秒", timeDiff);
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        ServerTimeTest timeTest = new ServerTimeTest();

        timeTest.testGetServerTime();
    }
}