package com.example.openapi.test.future.position;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

/**
 * 单独测试 change-type 接口
 */
public class ChangeTypeTest {

    private static final Logger log = LoggerFactory.getLogger(ChangeTypeTest.class);

    private final ApiClient apiClient;
    private PositionListTest.PositionVO activePosition;

    private ChangeTypeTest(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private PositionListTest.PositionVO loadActivePosition() throws HashExApiException {
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

    /**
     * 修改仓位类型 - 单独测试
     */
    private void testChangePositionType() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，无法测试 change-type");
            return;
        }
        
        log.info("===== 开始测试 change-type 接口 =====");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", activePosition.getSymbol());
        params.put("positionType", activePosition.getPositionType());
        String positionModel = activePosition.getPositionModel();
        params.put("positionModel", positionModel == null || positionModel.isEmpty() ? "AGGREGATION" : positionModel);
        
        if (activePosition.getContractType() != null && !activePosition.getContractType().isEmpty()) {
            params.put("contractType", activePosition.getContractType());
        }
        
        if (activePosition.getPositionSide() != null && !activePosition.getPositionSide().isEmpty()) {
            params.put("positionSide", activePosition.getPositionSide());
        }
        
        log.info("请求参数: {}", params);
        
        try {
            long startTime = System.currentTimeMillis();
            String response = apiClient.sendPostRequest("/fut/v1/position/change-type", params);
            long endTime = System.currentTimeMillis();
            
            log.info("修改仓位类型响应耗时: {} ms", endTime - startTime);
            log.info("修改仓位类型响应: {}", response);
            
            // 解析响应
            JSONObject jsonObject = JSONUtil.parseObj(response);
            ApiResponse<?> apiResponse = JSONUtil.toBean(jsonObject,
                    new TypeReference<ApiResponse<?>>() {}, false);
            
            if (apiResponse.getCode() == 0) {
                log.info("✅ change-type 测试成功");
            } else {
                log.error("❌ change-type 测试失败, code={}, msg={}", apiResponse.getCode(), apiResponse.getMsg());
            }
        } catch (HashExApiException e) {
            log.error("❌ change-type 测试异常: {}", e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try (ApiClient client = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY)) {
            ChangeTypeTest test = new ChangeTypeTest(client);
            
            try {
                test.activePosition = test.loadActivePosition();
                if (test.activePosition == null) {
                    log.warn("当前账户没有可用持仓，测试无法进行");
                }
            } catch (HashExApiException e) {
                log.error("加载持仓信息失败: {}", e.getMessage(), e);
            }
            
            test.testChangePositionType();
        } catch (Exception e) {
            log.error("执行 change-type 测试发生异常: {}", e.getMessage(), e);
        }
    }
}
