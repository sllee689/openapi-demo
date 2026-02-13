package com.example.openapi.test.future.position;

import com.example.openapi.client.ApiClient;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 单接口测试：close-all
 */
public class CloseAllPositionTest {

    private static final Logger log = LoggerFactory.getLogger(CloseAllPositionTest.class);

    private final PositionOperationSupport support;

    private CloseAllPositionTest(ApiClient apiClient) {
        this.support = new PositionOperationSupport(apiClient, log);
    }

    /**
     * 单接口测试：一键平仓（示例使用无持仓交易对，避免实际平仓）
     */
    private void testCloseAllPosition() {
        log.info("===== 开始测试 close-all 接口 =====");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", "eth_usdt");
        params.put("contractType", "PERPETUAL");
        if (support.invokePost("/fut/v1/position/close-all", params, "一键平仓(close-all)")) {
            log.info("✅ close-all 请求已发送，请根据返回内容确认业务结果");
        }
    }

    public static void main(String[] args) {
        try (ApiClient client = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY)) {
            CloseAllPositionTest test = new CloseAllPositionTest(client);
            test.testCloseAllPosition();
        } catch (Exception e) {
            log.error("执行 close-all 测试发生异常: {}", e.getMessage(), e);
        }
    }
}
