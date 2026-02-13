package com.example.openapi.test.future.position;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;

import java.util.List;
import java.util.TreeMap;

/**
 * 仓位操作测试公共支持类
 */
class PositionOperationSupport {

    private final ApiClient apiClient;
    private final Logger log;

    PositionOperationSupport(ApiClient apiClient, Logger log) {
        this.apiClient = apiClient;
        this.log = log;
    }

    PositionListTest.PositionVO loadActivePosition() throws HashExApiException {
        long startTime = System.currentTimeMillis();
        String responseJson = apiClient.sendGetRequest("/fut/v1/position/list", new TreeMap<>(), true);
        long endTime = System.currentTimeMillis();
        log.info("获取持仓列表耗时: {} ms", endTime - startTime);

        JSONObject jsonObject = JSONUtil.parseObj(responseJson);
        ApiResponse<List<PositionListTest.PositionVO>> apiResponse = JSONUtil.toBean(jsonObject,
                new TypeReference<ApiResponse<List<PositionListTest.PositionVO>>>() {}, false);

        if (apiResponse.getCode() != 0) {
            throw new HashExApiException("获取持仓失败: " + apiResponse.getMsg());
        }

        List<PositionListTest.PositionVO> positions = apiResponse.getData();
        if (positions == null || positions.isEmpty()) {
            return null;
        }

        PositionListTest.PositionVO position = positions.get(0);
        log.info("获取持仓成功: symbol={}, positionId={}, positionType={}, positionSide={}, contractType={}",
                position.getSymbol(), position.getPositionId(), position.getPositionType(),
                position.getPositionSide(), position.getContractType());
        return position;
    }

    boolean invokePost(String endpoint, TreeMap<String, String> params, String action) {
        log.info("{}请求参数: {}", action, params);
        try {
            long startTime = System.currentTimeMillis();
            String response = apiClient.sendPostRequest(endpoint, params);
            long endTime = System.currentTimeMillis();

            log.info("{}响应耗时: {} ms", action, endTime - startTime);
            log.info("{}响应: {}", action, response);
            return true;
        } catch (HashExApiException e) {
            log.error("{}失败: {}", action, e.getMessage(), e);
            return false;
        }
    }
}
