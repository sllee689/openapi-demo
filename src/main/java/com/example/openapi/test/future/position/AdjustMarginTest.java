package com.example.openapi.test.future.position;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 单接口测试：margin（逐仓保证金调整）
 */
public class AdjustMarginTest {

    private static final Logger log = LoggerFactory.getLogger(AdjustMarginTest.class);

    private final PositionOperationSupport support;
    private PositionListTest.PositionVO activePosition;

    private AdjustMarginTest(ApiClient apiClient) {
        this.support = new PositionOperationSupport(apiClient, log);
    }

    /**
     * 单接口测试：调整逐仓保证金（先增加后回滚）
     */
    private void testAdjustMargin() {
        if (activePosition == null) {
            log.warn("当前账户无持仓，无法测试 margin");
            return;
        }
        if (!"ISOLATED".equalsIgnoreCase(activePosition.getPositionType())) {
            log.info("当前仓位类型为{}，仅逐仓仓位支持保证金调整，跳过测试", activePosition.getPositionType());
            return;
        }
        if (activePosition.getPositionId() == null || activePosition.getPositionId().isEmpty()) {
            log.warn("持仓缺少 positionId 信息，无法测试逐仓保证金");
            return;
        }

        String side = activePosition.getPositionSide();
        if (side == null || side.isEmpty()) {
            log.warn("持仓缺少 positionSide 信息，无法测试逐仓保证金");
            return;
        }

        TreeMap<String, String> params = new TreeMap<>();
        params.put("symbol", activePosition.getSymbol());
        params.put("positionSide", side);
        params.put("positionId", activePosition.getPositionId());
        params.put("margin", "1");
        params.put("type", "ADD");

        log.info("===== 开始测试 margin 接口 =====");
        if (support.invokePost("/fut/v1/position/margin", params, "增加逐仓保证金(margin)")) {
            log.info("✅ margin-ADD 请求已发送");
            try {
                TreeMap<String, String> revertParams = new TreeMap<>(params);
                revertParams.put("type", "SUB");
                if (support.invokePost("/fut/v1/position/margin", revertParams, "回滚逐仓保证金(margin)")) {
                    log.info("✅ margin-SUB 回滚请求已发送");
                }
            } catch (Exception ex) {
                log.warn("回滚逐仓保证金请求失败: {}", ex.getMessage(), ex);
            }
        }
    }

    public static void main(String[] args) {
        try (ApiClient client = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY)) {
            AdjustMarginTest test = new AdjustMarginTest(client);

            try {
                test.activePosition = test.support.loadActivePosition();
                if (test.activePosition == null) {
                    log.warn("当前账户没有可用持仓，测试无法进行");
                }
            } catch (HashExApiException e) {
                log.error("加载持仓信息失败: {}", e.getMessage(), e);
            }

            test.testAdjustMargin();
        } catch (Exception e) {
            log.error("执行 margin 测试发生异常: {}", e.getMessage(), e);
        }
    }
}
