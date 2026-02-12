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
 * 合约仓位操作相关接口演示测试
 */
public class PositionOperationTest {

    private static final Logger log = LoggerFactory.getLogger(PositionOperationTest.class);

    private final ApiClient apiClient;
    private PositionListTest.PositionVO activePosition;

    private PositionOperationTest(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private PositionListTest.PositionVO loadActivePosition() throws HashExApiException {
        String responseJson = apiClient.sendGetRequest("/fut/v1/position/list", new TreeMap<>(), true);
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
        return positions.get(0);
    }

    /**
     * 修改仓位类型
     */
    private void testChangePositionType() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，跳过修改仓位类型测试");
            return;
        }
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
        invokePost("/fut/v1/position/change-type", params, "修改仓位类型");
    }

    /**
     * 调整杠杆倍数
     */
    private void testAdjustLeverage() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，跳过调整杠杆测试");
            return;
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", activePosition.getSymbol());
        if (activePosition.getPositionSide() != null && !activePosition.getPositionSide().isEmpty()) {
            params.put("positionSide", activePosition.getPositionSide());
        }
        // leverage 为必填参数
        int leverage = activePosition.getLeverage() != null ? activePosition.getLeverage() : 20;
        params.put("leverage", String.valueOf(leverage));
        invokePost("/fut/v1/position/adjust-leverage", params, "调整杠杆倍数");
    }

    /**
     * 调整逐仓保证金（先增加后回滚）
     */
    private void testAdjustMargin() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，跳过逐仓保证金测试");
            return;
        }
        if (!"ISOLATED".equalsIgnoreCase(activePosition.getPositionType())) {
            log.info("当前仓位类型为{}，仅逐仓仓位支持保证金调整，跳过测试", activePosition.getPositionType());
            return;
        }
        if (activePosition.getPositionId() == null || activePosition.getPositionId().isEmpty()) {
            log.warn("持仓缺少positionId信息，无法测试逐仓保证金");
            return;
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", activePosition.getSymbol());
        // positionSide 为必填参数
        String side = activePosition.getPositionSide();
        if (side == null || side.isEmpty()) {
            log.warn("持仓缺少 positionSide 信息，无法测试逐仓保证金");
            return;
        }
        params.put("positionSide", side);
        params.put("positionId", activePosition.getPositionId());
        params.put("margin", "1");
        params.put("type", "ADD");
        String tag = "增加逐仓保证金";
        if (invokePost("/fut/v1/position/margin", params, tag)) {
            try {
                TreeMap<String, String> revertParams = new TreeMap<>(params);
                revertParams.put("type", "SUB");
                invokePost("/fut/v1/position/margin", revertParams, "回滚逐仓保证金");
            } catch (Exception ex) {
                log.warn("回滚逐仓保证金请求失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 一键平仓（示例使用无持仓交易对，避免实际平仓）
     */
    private void testCloseAllPosition() {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", "eth_usdt");
        params.put("contractType", "PERPETUAL");
        invokePost("/fut/v1/position/close-all", params, "一键平仓");
    }

    private boolean invokePost(String endpoint, TreeMap<String, String> params, String action) {
        try {
            String response = apiClient.sendPostRequest(endpoint, params);
            log.info("{}响应: {}", action, response);
            return true;
        } catch (HashExApiException e) {
            log.error("{}失败: {}", action, e.getMessage(), e);
            return false;
        }
    }

    private void runAll() {
        try {
            activePosition = loadActivePosition();
            if (activePosition == null) {
                log.warn("当前账户没有可用持仓，部分测试将被跳过");
            } else {
                log.info("使用持仓进行测试: symbol={}, positionId={}, type={}, side={}",
                        activePosition.getSymbol(), activePosition.getPositionId(),
                        activePosition.getPositionType(), activePosition.getPositionSide());
            }
        } catch (HashExApiException e) {
            log.error("加载持仓信息失败: {}", e.getMessage(), e);
        }

        testChangePositionType();
        testAdjustLeverage();
        testAdjustMargin();
        testCloseAllPosition();
    }

    public static void main(String[] args) {
        try (ApiClient client = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY)) {
            PositionOperationTest test = new PositionOperationTest(client);
            test.runAll();
        } catch (Exception e) {
            log.error("执行仓位操作测试发生异常: {}", e.getMessage(), e);
        }
    }
}
