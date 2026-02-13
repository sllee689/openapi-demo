package com.example.openapi.test.future.position;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 单接口测试：adjust-leverage
 */
public class AdjustLeverageTest {

    private static final Logger log = LoggerFactory.getLogger(AdjustLeverageTest.class);

    private final PositionOperationSupport support;
    private PositionListTest.PositionVO activePosition;

    private AdjustLeverageTest(ApiClient apiClient) {
        this.support = new PositionOperationSupport(apiClient, log);
    }

    /**
     * 单接口测试：调整杠杆倍数
     */
    private void testAdjustLeverage() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，无法测试 adjust-leverage");
            return;
        }

        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", activePosition.getSymbol());
        if (activePosition.getPositionSide() != null && !activePosition.getPositionSide().isEmpty()) {
            params.put("positionSide", activePosition.getPositionSide());
        }

        int leverage = activePosition.getLeverage() != null ? activePosition.getLeverage() : 20;
        params.put("leverage", String.valueOf(leverage));

        log.info("===== 开始测试 adjust-leverage 接口 =====");
        if (support.invokePost("/fut/v1/position/adjust-leverage", params, "调整杠杆倍数(adjust-leverage)")) {
            log.info("✅ adjust-leverage 请求已发送，请根据返回内容确认业务结果");
        }
    }

    public static void main(String[] args) {
        try (ApiClient client = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY)) {
            AdjustLeverageTest test = new AdjustLeverageTest(client);

            try {
                test.activePosition = test.support.loadActivePosition();
                if (test.activePosition == null) {
                    log.warn("当前账户没有可用持仓，测试无法进行");
                }
            } catch (HashExApiException e) {
                log.error("加载持仓信息失败: {}", e.getMessage(), e);
            }

            test.testAdjustLeverage();
        } catch (Exception e) {
            log.error("执行 adjust-leverage 测试发生异常: {}", e.getMessage(), e);
        }
    }
}
