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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/**
 * 合约订单列表查询测试类
 */
public class OrderListTest {

    private static final Logger log = LoggerFactory.getLogger(OrderListTest.class);
    private static ApiClient apiClient;

    private static final String STATE_UNFINISHED = "UNFINISHED";
    private static final String STATE_OPEN = "OPEN";
    private static final String STATE_HISTORY = "HISTORY";
    private static final String STATE_NEW = "NEW";
    private static final String STATE_PARTIALLY_FILLED = "PARTIALLY_FILLED";
    private static final String STATE_FILLED = "FILLED";
    private static final String STATE_CANCELED = "CANCELED";
    private static final String STATE_REJECTED = "REJECTED";
    private static final String STATE_EXPIRED = "EXPIRED";

    private static final Set<String> VALID_STATES = new HashSet<>(Arrays.asList(
            STATE_UNFINISHED,
            STATE_OPEN,
            STATE_HISTORY,
            STATE_NEW,
            STATE_PARTIALLY_FILLED,
            STATE_FILLED,
            STATE_CANCELED,
            STATE_REJECTED,
            STATE_EXPIRED
    ));

    /**
     * 查询订单列表
     *
     * @param request 查询请求参数
     * @return 订单列表分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public PageResult<FutureOrderVO> getOrderList(OrderListRequest request) throws HashExApiException {
        try {
            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (request.getSymbol() != null) {
                queryParams.put("symbol", request.getSymbol());
            }
            if (request.getState() != null) {
                queryParams.put("state", request.getState());
            }
            if (request.getPage() != null) {
                queryParams.put("page", request.getPage().toString());
            }
            if (request.getSize() != null) {
                queryParams.put("size", request.getSize().toString());
            }
            if (request.getStartTime() != null) {
                queryParams.put("startTime", request.getStartTime().toString());
            }
            if (request.getEndTime() != null) {
                queryParams.put("endTime", request.getEndTime().toString());
            }
            if (request.getContractType() != null) {
                queryParams.put("contractType", request.getContractType());
            }
            if (request.getForceClose() != null) {
                queryParams.put("forceClose", request.getForceClose().toString());
            }

            // 调用API
            String responseJson = apiClient.sendGetRequest("/fut/v1/order/list", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(responseJson);
            if (jsonObject.getInt("code") != 0) {
                throw new HashExApiException("查询订单列表失败: " + jsonObject.getStr("msg"));
            }

            JSONObject dataObj = jsonObject.getJSONObject("data");
            PageResult<FutureOrderVO> pageResult = new PageResult<>();
            pageResult.setPage(dataObj.getInt("page"));
            pageResult.setSize(dataObj.getInt("ps"));
            pageResult.setTotal(dataObj.getLong("total"));

            // 计算总页数
            int size = pageResult.getSize();
            long total = pageResult.getTotal();
            pageResult.setPages(size > 0 ? (int) Math.ceil((double) total / size) : 0);

            // 解析订单列表
            JSONArray itemsArray = dataObj.getJSONArray("items");
            List<FutureOrderVO> orderList = new ArrayList<>();

            if (itemsArray != null && !itemsArray.isEmpty()) {
                for (int i = 0; i < itemsArray.size(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    FutureOrderVO order = new FutureOrderVO();
                    order.setOrderId(item.getStr("orderId"));
                    order.setSymbol(item.getStr("symbol"));
                    order.setContractType(item.getStr("contractType"));
                    order.setOrderType(item.getStr("orderType"));
                    order.setOrderSide(item.getStr("orderSide"));
                    order.setLeverage(item.getInt("leverage"));
                    order.setPositionSide(item.getStr("positionSide"));
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
            throw new HashExApiException("查询订单列表时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 测试查询所有未完成订单
     */
    private void testQueryUnfinishedOrders() throws HashExApiException {
        log.info("===== 测试查询所有未完成订单 =====");

        queryAndPrint("btc_usdt", STATE_UNFINISHED, "未完成订单");
    }

    /**
     * 测试 OPEN 别名查询（与 UNFINISHED 语义一致）
     */
    private void testQueryOpenOrders() throws HashExApiException {
        log.info("===== 测试 OPEN 别名查询 =====");
        queryAndPrint("btc_usdt", STATE_OPEN, "OPEN别名未完成订单");
    }

    /**
     * 测试查询历史订单
     */
    private void testQueryHistoryOrders() throws HashExApiException {
        log.info("===== 测试查询历史订单 =====");

        OrderListRequest request = new OrderListRequest();
        request.setSymbol("btc_usdt");
        request.setState(STATE_HISTORY); // 历史订单
        request.setPage(1);
        request.setSize(10);

        // 设置时间范围（过去7天）
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 7 * 24 * 60 * 60 * 1000; // 7天前
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        PageResult<FutureOrderVO> result = getOrderList(request);
        log.info("查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
                result.getTotal(), result.getPages(), result.getPage(), result.getSize());

        List<FutureOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            log.info("历史订单列表 ({} 条记录):", orders.size());
            int index = 1;
            for (FutureOrderVO order : orders) {
                log.info("订单 #{}: {}", index++, order);
            }
        } else {
            log.info("没有历史订单");
        }
    }

    /**
     * 测试按特定状态查询订单
     */
    private void testQueryOrdersByStatus() throws HashExApiException {
        log.info("===== 测试按特定状态查询订单 =====");

        queryAndPrint("btc_usdt", STATE_CANCELED, "已取消订单");
    }

    /**
     * 测试按精确枚举状态查询（示例：NEW）
     */
    private void testQueryOrdersByExactState() throws HashExApiException {
        log.info("===== 测试按精确枚举状态查询 =====");
        queryAndPrint("btc_usdt", STATE_NEW, "NEW状态订单");
    }

    /**
     * 全状态覆盖测试：逐个验证状态筛选是否可用，并统计是否存在数据
     */
    private void testAllStateCoverage() throws HashExApiException {
        log.info("===== 全状态覆盖测试（用于文档收敛） =====");
        List<String> states = Arrays.asList(
                STATE_UNFINISHED,
                STATE_OPEN,
                STATE_HISTORY,
                STATE_NEW,
                STATE_PARTIALLY_FILLED,
                STATE_FILLED,
                STATE_CANCELED,
                STATE_REJECTED,
                STATE_EXPIRED
        );

        List<String> statesWithData = new ArrayList<>();
        for (String state : states) {
            OrderListRequest request = new OrderListRequest();
            request.setSymbol("btc_usdt");
            request.setState(state);
            request.setPage(1);
            request.setSize(10);

            PageResult<FutureOrderVO> result = getOrderList(request);
            long total = result.getTotal() == null ? 0 : result.getTotal();
            if (total > 0) {
                statesWithData.add(state);
            }
            log.info("STATE_CHECK | state={} | total={}", state, total);
        }

        log.info("STATE_WITH_DATA | {}", String.join(",", statesWithData));
    }

    private void queryAndPrint(String symbol, String state, String sceneName) throws HashExApiException {
        OrderListRequest request = new OrderListRequest();
        request.setSymbol(symbol);
        request.setState(state);
        request.setPage(1);
        request.setSize(10);

        PageResult<FutureOrderVO> result = getOrderList(request);
        log.info("{} 查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
                sceneName, result.getTotal(), result.getPages(), result.getPage(), result.getSize());

        List<FutureOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            log.info("{} 列表 ({} 条记录):", sceneName, orders.size());
            int index = 1;
            for (FutureOrderVO order : orders) {
                log.info("订单 #{}: {}", index++, order);
            }
        } else {
            log.info("{} 查询结果为空", sceneName);
        }
    }

    /**
     * 按时间范围查询订单
     */
    private void testQueryOrdersByTimeRange() throws HashExApiException {
        log.info("===== 按时间范围查询订单 =====");

        OrderListRequest request = new OrderListRequest();
        request.setSymbol("btc_usdt");

        // 查询最近24小时内的订单
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 24 * 60 * 60 * 1000; // 24小时前
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPage(1);
        request.setSize(10);

        PageResult<FutureOrderVO> result = getOrderList(request);
        log.info("最近24小时订单查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
                result.getTotal(), result.getPages(), result.getPage(), result.getSize());

        List<FutureOrderVO> orders = result.getList();
        if (orders != null && !orders.isEmpty()) {
            log.info("最近24小时订单列表 ({} 条记录):", orders.size());
            int index = 1;
            for (FutureOrderVO order : orders) {
                log.info("订单 #{}: {}", index++, order);
            }
        } else {
            log.info("最近24小时内没有订单");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        OrderListTest orderListTest = new OrderListTest();

        // 执行测试
        orderListTest.testAllStateCoverage();
        orderListTest.testQueryUnfinishedOrders();
        orderListTest.testQueryOpenOrders();
        orderListTest.testQueryHistoryOrders();
        orderListTest.testQueryOrdersByStatus();
        orderListTest.testQueryOrdersByExactState();
        orderListTest.testQueryOrdersByTimeRange();
    }

    /**
     * 合约订单VO
     */
    public static class FutureOrderVO {
        private String orderId;             // 订单ID
        private String symbol;              // 交易对
        private String contractType;        // 合约类型
        private String orderType;           // 订单类型
        private String orderSide;           // 买卖方向
        private Integer leverage;           // 杠杆倍数
        private String positionSide;        // 仓位方向
        private Boolean closePosition;      // 是否平仓
        private String price;               // 价格
        private String origQty;             // 原始数量
        private String avgPrice;            // 成交均价
        private String executedQty;         // 已成交数量
        private String marginFrozen;        // 冻结保证金
        private String triggerProfitPrice;  // 止盈价
        private String triggerStopPrice;    // 止损价
        private String sourceId;            // 来源ID
        private Boolean forceClose;         // 是否强平
        private String tradeFee;            // 交易费
        private String closeProfit;         // 平仓盈亏
        private String state;               // 订单状态
        private Long createdTime;           // 创建时间
        private Long updatedTime;           // 更新时间

        // Getters and Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = contractType;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getOrderSide() {
            return orderSide;
        }

        public void setOrderSide(String orderSide) {
            this.orderSide = orderSide;
        }

        public Integer getLeverage() {
            return leverage;
        }

        public void setLeverage(Integer leverage) {
            this.leverage = leverage;
        }

        public String getPositionSide() {
            return positionSide;
        }

        public void setPositionSide(String positionSide) {
            this.positionSide = positionSide;
        }

        public Boolean getClosePosition() {
            return closePosition;
        }

        public void setClosePosition(Boolean closePosition) {
            this.closePosition = closePosition;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getOrigQty() {
            return origQty;
        }

        public void setOrigQty(String origQty) {
            this.origQty = origQty;
        }

        public String getAvgPrice() {
            return avgPrice;
        }

        public void setAvgPrice(String avgPrice) {
            this.avgPrice = avgPrice;
        }

        public String getExecutedQty() {
            return executedQty;
        }

        public void setExecutedQty(String executedQty) {
            this.executedQty = executedQty;
        }

        public String getMarginFrozen() {
            return marginFrozen;
        }

        public void setMarginFrozen(String marginFrozen) {
            this.marginFrozen = marginFrozen;
        }

        public String getTriggerProfitPrice() {
            return triggerProfitPrice;
        }

        public void setTriggerProfitPrice(String triggerProfitPrice) {
            this.triggerProfitPrice = triggerProfitPrice;
        }

        public String getTriggerStopPrice() {
            return triggerStopPrice;
        }

        public void setTriggerStopPrice(String triggerStopPrice) {
            this.triggerStopPrice = triggerStopPrice;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public Boolean getForceClose() {
            return forceClose;
        }

        public void setForceClose(Boolean forceClose) {
            this.forceClose = forceClose;
        }

        public String getTradeFee() {
            return tradeFee;
        }

        public void setTradeFee(String tradeFee) {
            this.tradeFee = tradeFee;
        }

        public String getCloseProfit() {
            return closeProfit;
        }

        public void setCloseProfit(String closeProfit) {
            this.closeProfit = closeProfit;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            if (state == null) {
                this.state = null;
                return;
            }
            String normalized = state.trim().toUpperCase(Locale.ROOT);
            if (!VALID_STATES.contains(normalized)) {
                throw new IllegalArgumentException("invalid state: " + state + ", allowed: " + VALID_STATES);
            }
            this.state = normalized;
        }

        public Long getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Long createdTime) {
            this.createdTime = createdTime;
        }

        public Long getUpdatedTime() {
            return updatedTime;
        }

        public void setUpdatedTime(Long updatedTime) {
            this.updatedTime = updatedTime;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("订单 [%s] {\n" +
                            "  交易对: %s, 合约类型: %s\n" +
                            "  方向: %s, 类型: %s, 仓位: %s\n" +
                            "  价格: %s, 数量: %s, 已成交: %s\n" +
                            "  成交均价: %s, 冻结保证金: %s\n" +
                            "  状态: %s, 杠杆: %dx\n" +
                            "  交易费: %s, 平仓盈亏: %s\n" +
                            "  创建时间: %s\n" +
                            "  更新时间: %s\n" +
                            "  止盈价: %s, 止损价: %s\n" +
                            "  强平: %s, 平仓: %s" +
                            "\n}",
                    orderId,
                    symbol, contractType,
                    orderSide, orderType, positionSide,
                    "0".equals(price) ? "市价" : price, origQty, executedQty,
                    "0".equals(avgPrice) ? "未成交" : avgPrice, marginFrozen,
                    state, leverage,
                    tradeFee, closeProfit == null ? "无" : closeProfit,
                    createdTime == null ? "未知" : sdf.format(new Date(createdTime)),
                    updatedTime == null ? "未知" : sdf.format(new Date(updatedTime)),
                    triggerProfitPrice == null ? "未设置" : triggerProfitPrice,
                    triggerStopPrice == null ? "未设置" : triggerStopPrice,
                    (forceClose != null && forceClose) ? "是" : "否",
                    (closePosition != null && closePosition) ? "是" : "否");
        }
    }

    /**
     * 订单列表查询请求参数类
     */
    public static class OrderListRequest {
        private String symbol;          // 交易对
        private String state;           // 订单状态
        private Integer page;           // 页码
        private Integer size;           // 每页大小
        private Long startTime;         // 开始时间
        private Long endTime;           // 结束时间
        private String contractType;    // 合约类型
        private Boolean forceClose;     // 是否强平

        // Getters and Setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = contractType;
        }

        public Boolean getForceClose() {
            return forceClose;
        }

        public void setForceClose(Boolean forceClose) {
            this.forceClose = forceClose;
        }
    }

    /**
     * 分页结果类
     */
    public static class PageResult<T> {
        private List<T> list;    // 数据列表
        private Long total;      // 总记录数
        private Integer pages;   // 总页数
        private Integer page;    // 当前页码
        private Integer size;    // 每页大小

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Integer getPages() {
            return pages;
        }

        public void setPages(Integer pages) {
            this.pages = pages;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
