package com.example.openapi.test.future.order;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * 合约历史订单列表查询测试类
 */
public class OrderListHistoryTest {

    private static final Logger log = LoggerFactory.getLogger(OrderListHistoryTest.class);
    private static final String[] REQUIRED_DATA_FIELDS = {"hasPrev", "hasNext", "items"};
    private static final String[] REQUIRED_ITEM_FIELDS = {
            "orderId", "symbol", "contractType", "orderType", "orderSide", "leverage",
            "positionSide", "timeInForce", "closePosition", "price", "origQty", "avgPrice",
            "executedQty", "marginFrozen", "forceClose", "tradeFee", "state", "createdTime", "updatedTime"
    };
    private static ApiClient apiClient;

    /**
     * 查询历史订单列表
     *
     * @param request 查询请求参数
     * @return 历史订单分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public CursorPageResult<HistoryOrderVO> getHistoryOrderList(HistoryOrderListRequest request) throws HashExApiException {
        try {
            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (request.getSymbol() != null) {
                queryParams.put("symbol", request.getSymbol());
            }
            if (request.getOrderSide() != null) {
                queryParams.put("orderSide", request.getOrderSide());
            }
            if (request.getOrderType() != null) {
                queryParams.put("orderType", request.getOrderType());
            }
            if (request.getForceClose() != null) {
                queryParams.put("forceClose", request.getForceClose().toString());
            }
            if (request.getStartTime() != null) {
                queryParams.put("startTime", request.getStartTime().toString());
            }
            if (request.getEndTime() != null) {
                queryParams.put("endTime", request.getEndTime().toString());
            }
            if (request.getPage() != null) {
                queryParams.put("page", request.getPage().toString());
            }
            if (request.getSize() != null) {
                queryParams.put("size", request.getSize().toString());
            }
            if (request.getContractType() != null) {
                queryParams.put("contractType", request.getContractType());
            }

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/order/list-history", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(responseJson);
            if (jsonObject.getInt("code") != 0) {
                throw new HashExApiException("查询历史订单列表失败: " + jsonObject.getStr("msg"));
            }

            JSONObject dataObj = jsonObject.getJSONObject("data");
            validateDataFields(dataObj);
            CursorPageResult<HistoryOrderVO> pageResult = new CursorPageResult<>();
            pageResult.setHasPrev(dataObj.getBool("hasPrev"));
            pageResult.setHasNext(dataObj.getBool("hasNext"));

            // 解析订单列表
            JSONArray itemsArray = dataObj.getJSONArray("items");
            List<HistoryOrderVO> orderList = new ArrayList<>();

            if (itemsArray != null && !itemsArray.isEmpty()) {
                for (int i = 0; i < itemsArray.size(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    validateItemFields(item);
                    HistoryOrderVO order = new HistoryOrderVO();
                    order.setOrderId(item.getStr("orderId"));
                    order.setClientOrderId(item.getStr("clientOrderId"));
                    order.setSymbol(item.getStr("symbol"));
                    order.setContractType(item.getStr("contractType"));
                    order.setOrderType(item.getStr("orderType"));
                    order.setOrderSide(item.getStr("orderSide"));
                    order.setLeverage(item.getInt("leverage"));
                    order.setPositionSide(item.getStr("positionSide"));
                    order.setTimeInForce(item.getStr("timeInForce"));
                    order.setClosePosition(item.getBool("closePosition"));
                    order.setPrice(item.getStr("price"));
                    order.setOrigQty(item.getStr("origQty"));
                    order.setAvgPrice(item.getStr("avgPrice"));
                    order.setExecutedQty(item.getStr("executedQty"));
                    order.setMarginFrozen(item.getStr("marginFrozen"));
                    order.setTriggerProfitPrice(item.getStr("triggerProfitPrice"));
                    order.setTriggerStopPrice(item.getStr("triggerStopPrice"));
                    order.setSourceId(item.getStr("sourceId"));
                    order.setForceClose(item.getBool("forceClose"));
                    order.setTradeFee(item.getStr("tradeFee"));
                    order.setCloseProfit(item.getStr("closeProfit"));
                    order.setState(item.getStr("state"));
                    order.setCreatedTime(item.getLong("createdTime"));
                    order.setUpdatedTime(item.getLong("updatedTime"));
                    orderList.add(order);
                }
            }

            pageResult.setList(orderList);
            return pageResult;

        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询历史订单列表时出错: " + e.getMessage(), e);
        }
    }

    private void validateDataFields(JSONObject dataObj) throws HashExApiException {
        if (dataObj == null) {
            throw new HashExApiException("历史订单响应data为空");
        }
        for (String field : REQUIRED_DATA_FIELDS) {
            if (!dataObj.containsKey(field)) {
                throw new HashExApiException("历史订单响应缺少字段: " + field);
            }
        }
    }

    private void validateItemFields(JSONObject item) throws HashExApiException {
        for (String field : REQUIRED_ITEM_FIELDS) {
            if (!item.containsKey(field)) {
                throw new HashExApiException("历史订单item缺少字段: " + field + ", item=" + item);
            }
        }
    }

    /**
     * 测试查询历史订单
     */
    private void testQueryHistoryOrders() throws HashExApiException {
        log.info("===== 测试查询历史订单 =====");

        HistoryOrderListRequest request = new HistoryOrderListRequest();
        request.setSymbol("btc_usdt");
        request.setPage(1);
        request.setSize(10);

        CursorPageResult<HistoryOrderVO> result = getHistoryOrderList(request);
        log.info("查询结果 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<HistoryOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            log.info("历史订单列表 ({} 条记录):", orders.size());
            int index = 1;
            for (HistoryOrderVO order : orders) {
                log.info("订单 #{}: {}", index++, order);
            }
        } else {
            log.info("没有历史订单");
        }
    }

    /**
     * 测试按时间范围查询历史订单
     */
    private void testQueryByTimeRange() throws HashExApiException {
        log.info("===== 按时间范围查询历史订单 =====");

        HistoryOrderListRequest request = new HistoryOrderListRequest();
        request.setSymbol("btc_usdt");

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 7 * 24 * 60 * 60 * 1000L; // 7天前
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPage(1);
        request.setSize(10);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("查询时间范围: {} 至 {}",
                sdf.format(new Date(startTime)), sdf.format(new Date(endTime)));

        CursorPageResult<HistoryOrderVO> result = getHistoryOrderList(request);
        log.info("查询结果 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<HistoryOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            log.info("历史订单列表 ({} 条记录):", orders.size());
            int index = 1;
            for (HistoryOrderVO order : orders) {
                log.info("订单 #{}: {}", index++, order);
            }
        } else {
            log.info("该时间范围内没有历史订单");
        }
    }

    /**
     * 测试按买卖方向查询历史订单
     */
    private void testQueryByOrderSide() throws HashExApiException {
        log.info("===== 按买卖方向查询历史订单 =====");

        HistoryOrderListRequest request = new HistoryOrderListRequest();
        request.setSymbol("btc_usdt");
        request.setOrderSide("BUY");
        request.setPage(1);
        request.setSize(10);

        CursorPageResult<HistoryOrderVO> result = getHistoryOrderList(request);
        log.info("BUY方向历史订单 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<HistoryOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            for (HistoryOrderVO order : orders) {
                log.info("{}", order);
            }
        } else {
            log.info("没有BUY方向的历史订单");
        }
    }

    private void testQueryByOrderTypeVariants() throws HashExApiException {
        log.info("===== 测试 orderType 参数枚举 =====");
        String[] orderTypes = {"LIMIT", "MARKET"};
        for (String orderType : orderTypes) {
            HistoryOrderListRequest request = new HistoryOrderListRequest();
            request.setSymbol("btc_usdt");
            request.setOrderType(orderType);
            request.setPage(1);
            request.setSize(10);
            try {
                CursorPageResult<HistoryOrderVO> result = getHistoryOrderList(request);
                log.info("orderType={} 查询成功, hasPrev={}, hasNext={}",
                        orderType, result.getHasPrev(), result.getHasNext());
            } catch (HashExApiException e) {
                log.warn("orderType={} 查询失败(疑似不支持): {}", orderType, e.getMessage());
            }
        }
    }

    private void testQueryByForceCloseVariants() throws HashExApiException {
        log.info("===== 测试 forceClose 参数取值 =====");
        Boolean[] forceCloseValues = {Boolean.FALSE, Boolean.TRUE};
        for (Boolean forceClose : forceCloseValues) {
            HistoryOrderListRequest request = new HistoryOrderListRequest();
            request.setSymbol("btc_usdt");
            request.setForceClose(forceClose);
            request.setPage(1);
            request.setSize(10);
            CursorPageResult<HistoryOrderVO> result = getHistoryOrderList(request);
            log.info("forceClose={} 查询成功, hasPrev={}, hasNext={}",
                    forceClose, result.getHasPrev(), result.getHasNext());
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        OrderListHistoryTest test = new OrderListHistoryTest();

        // 执行测试
        test.testQueryHistoryOrders();
        test.testQueryByTimeRange();
        test.testQueryByOrderSide();
        test.testQueryByOrderTypeVariants();
        test.testQueryByForceCloseVariants();
    }

    /**
     * 历史订单VO
     */
    public static class HistoryOrderVO {
        private String orderId;
        private String clientOrderId;
        private String symbol;
        private String contractType;
        private String orderType;
        private String orderSide;
        private Integer leverage;
        private String positionSide;
        private String timeInForce;
        private Boolean closePosition;
        private String price;
        private String origQty;
        private String avgPrice;
        private String executedQty;
        private String marginFrozen;
        private String triggerProfitPrice;
        private String triggerStopPrice;
        private String sourceId;
        private Boolean forceClose;
        private String tradeFee;
        private String closeProfit;
        private String state;
        private Long createdTime;
        private Long updatedTime;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getClientOrderId() { return clientOrderId; }
        public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getContractType() { return contractType; }
        public void setContractType(String contractType) { this.contractType = contractType; }
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        public String getOrderSide() { return orderSide; }
        public void setOrderSide(String orderSide) { this.orderSide = orderSide; }
        public Integer getLeverage() { return leverage; }
        public void setLeverage(Integer leverage) { this.leverage = leverage; }
        public String getPositionSide() { return positionSide; }
        public void setPositionSide(String positionSide) { this.positionSide = positionSide; }
        public String getTimeInForce() { return timeInForce; }
        public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }
        public Boolean getClosePosition() { return closePosition; }
        public void setClosePosition(Boolean closePosition) { this.closePosition = closePosition; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getOrigQty() { return origQty; }
        public void setOrigQty(String origQty) { this.origQty = origQty; }
        public String getAvgPrice() { return avgPrice; }
        public void setAvgPrice(String avgPrice) { this.avgPrice = avgPrice; }
        public String getExecutedQty() { return executedQty; }
        public void setExecutedQty(String executedQty) { this.executedQty = executedQty; }
        public String getMarginFrozen() { return marginFrozen; }
        public void setMarginFrozen(String marginFrozen) { this.marginFrozen = marginFrozen; }
        public String getTriggerProfitPrice() { return triggerProfitPrice; }
        public void setTriggerProfitPrice(String triggerProfitPrice) { this.triggerProfitPrice = triggerProfitPrice; }
        public String getTriggerStopPrice() { return triggerStopPrice; }
        public void setTriggerStopPrice(String triggerStopPrice) { this.triggerStopPrice = triggerStopPrice; }
        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }
        public Boolean getForceClose() { return forceClose; }
        public void setForceClose(Boolean forceClose) { this.forceClose = forceClose; }
        public String getTradeFee() { return tradeFee; }
        public void setTradeFee(String tradeFee) { this.tradeFee = tradeFee; }
        public String getCloseProfit() { return closeProfit; }
        public void setCloseProfit(String closeProfit) { this.closeProfit = closeProfit; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public Long getCreatedTime() { return createdTime; }
        public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }
        public Long getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(Long updatedTime) { this.updatedTime = updatedTime; }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("历史订单 [%s] {\n" +
                            "  交易对: %s, 合约类型: %s\n" +
                            "  方向: %s, 类型: %s, 仓位: %s, 有效期: %s\n" +
                            "  价格: %s, 数量: %s, 已成交: %s\n" +
                            "  成交均价: %s, 冻结保证金: %s\n" +
                            "  状态: %s, 杠杆: %dx\n" +
                            "  交易费: %s, 平仓盈亏: %s\n" +
                            "  创建时间: %s\n" +
                            "  更新时间: %s\n}",
                    orderId,
                    symbol, contractType,
                    orderSide, orderType, positionSide, timeInForce,
                    "0".equals(price) ? "市价" : price, origQty, executedQty,
                    "0".equals(avgPrice) ? "未成交" : avgPrice, marginFrozen,
                    state, leverage,
                    tradeFee, closeProfit == null ? "无" : closeProfit,
                    createdTime == null ? "未知" : sdf.format(new Date(createdTime)),
                    updatedTime == null ? "未知" : sdf.format(new Date(updatedTime)));
        }
    }

    /**
     * 历史订单列表查询请求参数类
     */
    public static class HistoryOrderListRequest {
        private String symbol;
        private String orderSide;
        private String orderType;
        private Boolean forceClose;
        private Long startTime;
        private Long endTime;
        private Integer page;
        private Integer size;
        private String contractType;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getOrderSide() { return orderSide; }
        public void setOrderSide(String orderSide) { this.orderSide = orderSide; }
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        public Boolean getForceClose() { return forceClose; }
        public void setForceClose(Boolean forceClose) { this.forceClose = forceClose; }
        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        public String getContractType() { return contractType; }
        public void setContractType(String contractType) { this.contractType = contractType; }
    }

    /**
     * 游标分页结果类
     */
    public static class CursorPageResult<T> {
        private List<T> list;
        private Boolean hasPrev;
        private Boolean hasNext;

        public List<T> getList() { return list; }
        public void setList(List<T> list) { this.list = list; }
        public Boolean getHasPrev() { return hasPrev; }
        public void setHasPrev(Boolean hasPrev) { this.hasPrev = hasPrev; }
        public Boolean getHasNext() { return hasNext; }
        public void setHasNext(Boolean hasNext) { this.hasNext = hasNext; }
    }
}
