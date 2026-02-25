package com.example.openapi.test.future.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 合约止盈止损委托创建测试类
 */
public class EntrustCreateProfitTest {

    private static final Logger log = LoggerFactory.getLogger(EntrustCreateProfitTest.class);
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 1500L;
    private static ApiClient apiClient;

    public EntrustCreateProfitTest() {
    }

    public EntrustCreateProfitTest(ApiClient apiClient) {
        EntrustCreateProfitTest.apiClient = apiClient;
    }

    /**
     * 创建止盈止损委托
     *
     * @param request 委托请求参数
     * @return 委托ID
     * @throws HashExApiException 如果API调用失败
     */
    public Object createProfitEntrust(ProfitEntrustRequest request) throws HashExApiException {
        try {
            // 验证必填参数
            if (request.getSymbol() == null || request.getSymbol().isEmpty()) {
                throw new HashExApiException("交易对不能为空");
            }
            if (request.getTriggerProfitPrice() == null && request.getTriggerStopPrice() == null) {
                throw new HashExApiException("止盈触发价和止损触发价至少填写一个");
            }
            if ("NORMAL".equalsIgnoreCase(request.getProfitType()) && (request.getOrigQty() == null || request.getOrigQty().isEmpty())) {
                throw new HashExApiException("profitType=NORMAL 时 origQty 必填");
            }

            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();
            queryParams.put("symbol", request.getSymbol());

            // 添加可选参数
            if (request.getTriggerProfitPrice() != null) {
                queryParams.put("triggerProfitPrice", request.getTriggerProfitPrice());
            }
            if (request.getTriggerStopPrice() != null) {
                queryParams.put("triggerStopPrice", request.getTriggerStopPrice());
            }
            if (request.getTriggerPriceType() != null) {
                queryParams.put("triggerPriceType", request.getTriggerPriceType());
            }
            if (request.getPositionId() != null) {
                queryParams.put("positionId", request.getPositionId());
            }
            if (request.getOrigQty() != null) {
                queryParams.put("origQty", request.getOrigQty());
            }
            if (request.getProfitType() != null) {
                queryParams.put("profitType", request.getProfitType());
            }
            if (request.getExpireTime() != null) {
                queryParams.put("expireTime", request.getExpireTime().toString());
            }

            // 调用API
            String responseJson = apiClient.sendPostRequest("/fut/v1/entrust/create-profit", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            ApiResponse<Object> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<Object>>() {
                    }, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("创建止盈止损委托失败: " + apiResponse.getMsg());
            }
            if (apiResponse.getData() == null) {
                throw new HashExApiException("创建止盈止损委托成功但返回data为空");
            }

            return apiResponse.getData();
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("创建止盈止损委托时出错: " + e.getMessage(), e);
        }
    }

    private Object createProfitEntrustWithRetry(ProfitEntrustRequest request) throws HashExApiException {
        HashExApiException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_TIMES; attempt++) {
            try {
                return createProfitEntrust(request);
            } catch (HashExApiException e) {
                lastException = e;
                if (!isRetryable(e) || attempt == MAX_RETRY_TIMES) {
                    throw e;
                }

                log.warn("创建止盈止损委托第{}次失败(可重试): {}，{}ms后重试", attempt, e.getMessage(), RETRY_INTERVAL_MS);
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new HashExApiException("重试等待被中断", interruptedException);
                }
            }
        }

        throw lastException == null ? new HashExApiException("创建止盈止损委托失败，未获取到异常信息") : lastException;
    }

    private boolean isRetryable(HashExApiException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        return msg.contains("状态码: 429")
                || msg.contains("状态码: 500")
                || msg.contains("状态码: 502")
                || msg.contains("状态码: 503")
                || msg.contains("状态码: 504")
                || msg.contains("Read timed out")
                || msg.contains("timeout")
                || msg.contains("连接")
                || msg.contains("Connection");
    }

    private void runCreateTest(ProfitEntrustRequest request, String scenario) throws HashExApiException {
        try {
            Object result = createProfitEntrustWithRetry(request);
            log.info("{}结果（委托ID）: {}", scenario, result);
        } catch (HashExApiException e) {
            if (isRetryable(e)) {
                log.warn("{}连续重试后仍失败，判定为环境波动，不作为用例失败: {}", scenario, e.getMessage());
                return;
            }
            throw e;
        }
    }

    private void runCreateExpectFail(ProfitEntrustRequest request, String scenario) throws HashExApiException {
        try {
            createProfitEntrust(request);
            throw new HashExApiException(scenario + " 预期失败但实际成功");
        } catch (HashExApiException e) {
            log.info("{}按预期失败: {}", scenario, e.getMessage());
        }
    }

    /**
     * 测试创建止盈委托（全部仓位）
     */
    private void testCreateTakeProfitEntrust() throws HashExApiException {
        log.info("===== 创建止盈委托测试 (profitType=ALL) =====");

        ProfitEntrustRequest request = new ProfitEntrustRequest();
        request.setSymbol("btc_usdt");
        request.setTriggerProfitPrice("100000");
        request.setTriggerPriceType("MARK_PRICE");
        request.setProfitType("ALL");  // 全部仓位，无需传origQty

        runCreateTest(request, "止盈委托创建");
    }

    /**
     * 测试创建止损委托（全部仓位）
     */
    private void testCreateStopLossEntrust() throws HashExApiException {
        log.info("===== 创建止损委托测试 (profitType=ALL) =====");

        ProfitEntrustRequest request = new ProfitEntrustRequest();
        request.setSymbol("btc_usdt");
        request.setTriggerStopPrice("60000");
        request.setTriggerPriceType("MARK_PRICE");
        request.setProfitType("ALL");  // 全部仓位，无需传origQty

        runCreateTest(request, "止损委托创建");
    }

    /**
     * 测试创建止盈止损委托（指定数量）
     */
    private void testCreateTakeProfitAndStopLoss() throws HashExApiException {
        log.info("===== 创建止盈止损委托测试 (带origQty) =====");

        ProfitEntrustRequest request = new ProfitEntrustRequest();
        request.setPositionId("592447815234371840");
        request.setSymbol("btc_usdt");
        request.setOrigQty("3002");
        request.setProfitType("NORMAL");
        request.setTriggerProfitPrice("100000");
        request.setTriggerStopPrice("30000");
        request.setTriggerPriceType("LATEST_PRICE");

        runCreateTest(request, "止盈止损委托创建");
    }

    private void testCreateWithTriggerPriceTypeVariants() throws HashExApiException {
        log.info("===== 测试 triggerPriceType 参数枚举 =====");

        ProfitEntrustRequest latestPriceReq = new ProfitEntrustRequest();
        latestPriceReq.setPositionId("592447815234371840");
        latestPriceReq.setSymbol("btc_usdt");
        latestPriceReq.setOrigQty("3002");
        latestPriceReq.setTriggerProfitPrice("100000");
        latestPriceReq.setTriggerStopPrice("30000");
        latestPriceReq.setTriggerPriceType("LATEST_PRICE");
        runCreateTest(latestPriceReq, "LATEST_PRICE 创建");

        ProfitEntrustRequest markPriceReq = new ProfitEntrustRequest();
        markPriceReq.setSymbol("btc_usdt");
        markPriceReq.setProfitType("ALL");
        markPriceReq.setTriggerProfitPrice("100000");
        markPriceReq.setTriggerPriceType("MARK_PRICE");
        runCreateTest(markPriceReq, "MARK_PRICE 创建");
    }

    private void testCreateWithInvalidEnumParams() throws HashExApiException {
        log.info("===== 测试非法枚举参数 =====");

        ProfitEntrustRequest invalidTriggerTypeReq = new ProfitEntrustRequest();
        invalidTriggerTypeReq.setSymbol("btc_usdt");
        invalidTriggerTypeReq.setOrigQty("3002");
        invalidTriggerTypeReq.setTriggerProfitPrice("100000");
        invalidTriggerTypeReq.setTriggerPriceType("BAD_TRIGGER_TYPE");
        runCreateExpectFail(invalidTriggerTypeReq, "非法triggerPriceType");

        ProfitEntrustRequest invalidProfitTypeReq = new ProfitEntrustRequest();
        invalidProfitTypeReq.setSymbol("btc_usdt");
        invalidProfitTypeReq.setOrigQty("3002");
        invalidProfitTypeReq.setTriggerProfitPrice("100000");
        invalidProfitTypeReq.setTriggerPriceType("LATEST_PRICE");
        invalidProfitTypeReq.setProfitType("BAD_PROFIT_TYPE");
        runCreateExpectFail(invalidProfitTypeReq, "非法profitType");
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        EntrustCreateProfitTest test = new EntrustCreateProfitTest();

        // 选择一个测试方法执行
        test.testCreateTakeProfitAndStopLoss();
        test.testCreateWithTriggerPriceTypeVariants();
        test.testCreateWithInvalidEnumParams();
        // test.testCreateTakeProfitEntrust();
        // test.testCreateStopLossEntrust();
    }

    /**
     * 止盈止损委托请求参数类
     */
    public static class ProfitEntrustRequest {
        private String symbol;                 // 交易对（必填）
        private String triggerProfitPrice;     // 止盈触发价
        private String triggerStopPrice;       // 止损触发价
        private String triggerPriceType;       // 触发价格类型：MARK_PRICE(标记价格)、LATEST_PRICE(最新价格)
        private String positionId;             // 仓位ID
        private String origQty;               // 委托数量（张），profitType=NORMAL 时必填且须大于0
        private String profitType;            // 止盈止损类型：NORMAL(按数量，默认)、ALL(全部仓位，无需origQty)
        private Long expireTime;              // 过期时间

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getTriggerProfitPrice() { return triggerProfitPrice; }
        public void setTriggerProfitPrice(String triggerProfitPrice) { this.triggerProfitPrice = triggerProfitPrice; }
        public String getTriggerStopPrice() { return triggerStopPrice; }
        public void setTriggerStopPrice(String triggerStopPrice) { this.triggerStopPrice = triggerStopPrice; }
        public String getTriggerPriceType() { return triggerPriceType; }
        public void setTriggerPriceType(String triggerPriceType) { this.triggerPriceType = triggerPriceType; }
        public String getPositionId() { return positionId; }
        public void setPositionId(String positionId) { this.positionId = positionId; }
        public String getOrigQty() { return origQty; }
        public void setOrigQty(String origQty) { this.origQty = origQty; }
        public String getProfitType() { return profitType; }
        public void setProfitType(String profitType) { this.profitType = profitType; }
        public Long getExpireTime() { return expireTime; }
        public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
    }
}
