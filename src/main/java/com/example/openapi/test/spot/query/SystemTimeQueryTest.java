package com.example.openapi.test.spot.query;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;

public class SystemTimeQueryTest {

    private static final Logger log = LoggerFactory.getLogger(SystemTimeQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 获取系统时间
     *
     * @return 系统时间戳
     * @throws HashExApiException 如果API调用失败
     */
    public Long getSystemTime() throws HashExApiException {
        try {
            // 创建空的查询参数Map，因为该API不需要参数
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 调用API，添加/spot/v1前缀
            String responseJson = apiClient.sendGetRequest("/spot/v1/p/time", queryParams,false);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用适合实际返回结构的映射
            ApiResponse<Long> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Long>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("获取系统时间失败: " + apiResponse.getMsg());
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("获取系统时间时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试获取系统时间
     */
    private void testGetSystemTime() throws HashExApiException {
        log.info("===== 获取系统时间测试 =====");

        // 获取系统时间
        Long serverTime = getSystemTime();

        // 格式化显示时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedTime = sdf.format(new Date(serverTime));

        log.info("系统时间戳: {}", serverTime);
        log.info("格式化后的时间: {}", formattedTime);

        // 计算与本地时间的差异
        long localTime = System.currentTimeMillis();
        long timeDiff = serverTime - localTime;

        log.info("本地时间戳: {}", localTime);
        log.info("与服务器时间差异: {} 毫秒", timeDiff);
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.mgbx.com");
        SystemTimeQueryTest systemTimeTest = new SystemTimeQueryTest();

        systemTimeTest.testGetSystemTime();
    }
}