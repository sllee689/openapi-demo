package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 历史订单查询测试类
 *
 * 测试 /spot/v1/u/trade/order/history 接口
 * 该接口用于查询已完成的历史订单信息，包括已成交、已撤销、已过期等非活跃状态的订单
 */
public class OrderHistoryQueryTest {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryQueryTest.class);
    private static ApiClient apiClient;

    // 订单状态常量
    private static final Map<String, String> ORDER_STATUS_MAP = new HashMap<>();
    static {
        ORDER_STATUS_MAP.put("NEW", "新建订单（未成交）");
        ORDER_STATUS_MAP.put("PARTIALLY_FILLED", "部分成交");
        ORDER_STATUS_MAP.put("PARTIALLY_CANCELED", "部分撤销");
        ORDER_STATUS_MAP.put("FILLED", "全部成交");
        ORDER_STATUS_MAP.put("CANCELED", "已撤销");
        ORDER_STATUS_MAP.put("REJECTED", "下单失败");
        ORDER_STATUS_MAP.put("EXPIRED", "已过期");
    }

    /**
     * 测试查询BTC_USDT最近订单历���
     */
    private void testQueryRecentOrders() throws HashExApiException {
        log.info("===== 测试查询最近订单历史 =====");

        // 查询BTC_USDT最近20条订单历史
        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户
        Integer limit = 20;

        log.info("查询 {} 账户类型 {} 最近 {} 条订单历史", symbol, balanceType, limit);
        ScrollPageResult<OrderVO> pageResult = getHistoryOrders(
                symbol, null, null, balanceType, null, null, limit
        );

        // 打印查询结果
        printOrderResults(pageResult);
    }

    /**
     * 测试按条件查询订单历史
     */
    private void testQueryOrdersWithConditions() throws HashExApiException {
        log.info("===== 测试按条件查询订单历史 =====");

        // 查询过去24小时内的BTC_USDT订单
        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户

        // 计算24小时前的时间戳
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);
        Integer limit = 10;

        log.info("查询币对 {} 账户类型 {} 从 {} 至 {} 的最近 {} 条订单",
                symbol, balanceType, formatTimestamp(startTime), formatTimestamp(endTime), limit);

        ScrollPageResult<OrderVO> pageResult = getHistoryOrders(
                symbol, startTime, endTime, balanceType, null, null, limit
        );

        // 打印查询结果
        printOrderResults(pageResult);
    }

    /**
     * 分页查询测试
     */
    private void testPaginationQuery() throws HashExApiException {
        log.info("===== 测试分页查询订单历史 =====");

        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户
        Integer limit = 5;
        Long nextId = null;
        String direction = null;

        // 第一页
        log.info("查询第一页数据，每页 {} 条", limit);
        ScrollPageResult<OrderVO> page1 = getHistoryOrders(
                symbol, null, null, balanceType, nextId, direction, limit
        );

        log.info("第一页共 {} 条记录", page1.getList().size());
        printOrderResults(page1);
        nextId = page1.getNextId();

        // 下一页
        if (page1.isHasNextPage() && nextId != null) {
            log.info("查询下一页数据，ID: {}, 方向: NEXT", nextId);
            direction = "NEXT";
            ScrollPageResult<OrderVO> page2 = getHistoryOrders(
                    symbol, null, null, balanceType, nextId, direction, limit
            );
            log.info("下一页共 {} 条记录", page2.getList().size());
            printOrderResults(page2);

            // 上一页
            if (page2.isHasPrevPage()) {
                log.info("查询上一页数据，ID: {}, 方向: PREV", nextId);
                direction = "PREV";
                ScrollPageResult<OrderVO> prevPage = getHistoryOrders(
                        symbol, null, null, balanceType, nextId, direction, limit
                );
                log.info("上一页共 {} 条记录", prevPage.getList().size());
                printOrderResults(prevPage);
            }
        }
    }

    /**
     * 测试查询已取消的订单
     */
    private void testQueryCanceledOrders() throws HashExApiException {
        log.info("===== 测试查询已取消订单 =====");

        // 查询过去7天内的已取消BTC_USDT订单
        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户

        // 计算7天前的时间戳
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (7 * 24 * 60 * 60 * 1000);
        Integer limit = 10;

        log.info("查询币对 {} 账户类型 {} 从 {} 至 {} 的已取消订单",
                symbol, balanceType, formatTimestamp(startTime), formatTimestamp(endTime));

        ScrollPageResult<OrderVO> allOrders = getHistoryOrders(
                symbol, startTime, endTime, balanceType, null, null, 50
        );

        // 筛选已取消订单
        List<OrderVO> canceledOrders = new ArrayList<>();
        for (OrderVO order : allOrders.getList()) {
            if ("CANCELED".equals(order.getState()) || "PARTIALLY_CANCELED".equals(order.getState())) {
                canceledOrders.add(order);
            }
        }

        log.info("共找到 {} 条已取消订单", canceledOrders.size());

        // 打印已取消订单详情
        int index = 1;
        for (OrderVO order : canceledOrders) {
            log.info("已取消订单 #{}", index++);
            printOrderDetail(order);
        }
    }

    /**
     * 测试查询杠杆账户订单
     */
    private void testQueryLeverageOrders() throws HashExApiException {
        log.info("===== 测试查询杠杆账户订单 =====");

        String symbol = "BTC_USDT";
        Integer balanceType = 2; // 杠杆账户
        Integer limit = 10;

        log.info("查询币对 {} 杠杆账户最近 {} 条订单历史", symbol, limit);
        ScrollPageResult<OrderVO> pageResult = getHistoryOrders(
                symbol, null, null, balanceType, null, null, limit
        );

        // 打印查询结果
        printOrderResults(pageResult);
    }

    /**
     * 格式化时间戳为可读形式
     */
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    /**
     * 打印订单结果集
     */
    private void printOrderResults(ScrollPageResult<OrderVO> pageResult) {
        log.info("共查询到 {} 条订单记录", pageResult.getList().size());
        log.info("是否有上一页: {}", pageResult.isHasPrevPage());
        log.info("是否有下一页: {}", pageResult.isHasNextPage());
        if (pageResult.isHasNextPage()) {
            log.info("下一页ID: {}", pageResult.getNextId());
        }

        // 打印订单详情
        int index = 1;
        for (OrderVO order : pageResult.getList()) {
            log.info("订单 #{}", index++);
            printOrderDetail(order);
        }
    }

    /**
     * 打印单个订单详情
     */
    private void printOrderDetail(OrderVO order) {
        log.info("  订单ID: {}", order.getOrderId());
        log.info("  客户端订单ID: {}", order.getClientOrderId());
        log.info("  币对: {}", order.getSymbol());
        log.info("  订单类型: {}", order.getOrderType());
        log.info("  方向: {}", order.getOrderSide());
        log.info("  账户类型: {}", order.getBalanceType() == 1 ? "现货账户" : "杠杆账户");
        log.info("  有效方式: {}", order.getTimeInForce());
        log.info("  价格: {}", order.getPrice());
        log.info("  原始数量: {}", order.getOrigQty());
        log.info("  已成交数量: {}", order.getExecutedQty());
        log.info("  平均成交价: {}", order.getAvgPrice());
        log.info("  冻结保证金: {}", order.getMarginFrozen());
        log.info("  订单来源: {}", order.getSourceId() != null ? order.getSourceId() : "无");
        log.info("  是否强平: {}", order.getForceClose() != null ? order.getForceClose() : "否");
        log.info("  状态: {} ({})", order.getState(), ORDER_STATUS_MAP.getOrDefault(order.getState(), "未知状态"));
        log.info("  创建时间: {}", formatTimestamp(order.getCreatedTime()));
    }

    public static void main(String[] args) throws HashExApiException {
        //替换自己的 accessKey 和 secretKey
        apiClient = new ApiClient("https://open.mgbx.com",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        OrderHistoryQueryTest historyTest = new OrderHistoryQueryTest();

        // 测试查询最近订单历史
        historyTest.testQueryRecentOrders();

        // 测试按条件查询订单历史
        historyTest.testQueryOrdersWithConditions();

        // 测试分页查询
        historyTest.testPaginationQuery();

        // 测试查询已取消订单
        historyTest.testQueryCanceledOrders();

        // 测试查询杠杆账户订单
        historyTest.testQueryLeverageOrders();
    }

    /**
     * 查询历史订单
     *
     * @param symbol      交易对，如BTC_USDT
     * @param startTime   起始时间戳
     * @param endTime     结束时间戳
     * @param balanceType 账户类型 1.现货账户 2.杠杆账户
     * @param id          分页标识，来自上一次请求结果
     * @param direction   翻页方向："NEXT"(下一页)，"PREV"(上一页)
     * @param limit       每页记录数
     * @return 订单历史列表
     * @throws HashExApiException 如果API调用失败
     */
    public ScrollPageResult<OrderVO> getHistoryOrders(
            String symbol,
            Long startTime,
            Long endTime,
            Integer balanceType,
            Long id,
            String direction,
            Integer limit
    ) throws HashExApiException {
        try {
            TreeMap<String, String> params = new TreeMap<>();

            if (symbol != null) {
                params.put("symbol", symbol);
            }
            if (startTime != null) {
                params.put("startTime", String.valueOf(startTime));
            }
            if (endTime != null) {
                params.put("endTime", String.valueOf(endTime));
            }
            if (balanceType != null) {
                params.put("balanceType", String.valueOf(balanceType));
            }
            if (id != null) {
                params.put("id", String.valueOf(id));
            }
            if (direction != null) {
                params.put("direction", direction);
            }
            if (limit != null) {
                params.put("limit", String.valueOf(limit));
            }

            // 发送GET请求并请求签名
            String response = apiClient.sendGetRequest("/spot/v1/u/trade/order/history", params, true);
            log.info("历史订单查询响应: {}", response);

            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(response);
            if (jsonResponse.getInt("code") != 0) {
                throw new HashExApiException("查询历史订单失败: " + jsonResponse.getStr("msg"));
            }

            JSONObject data = jsonResponse.getJSONObject("data");
            ScrollPageResult<OrderVO> result = new ScrollPageResult<>();
            result.setHasPrev(data.getBool("hasPrev"));
            result.setHasNext(data.getBool("hasNext"));

            // 将items数组转换为OrderVO列表
            List<OrderVO> orderList = new ArrayList<>();
            for (Object item : data.getJSONArray("items")) {
                JSONObject orderJson = (JSONObject) item;
                OrderVO orderVO = JSONUtil.toBean(orderJson, OrderVO.class);
                orderList.add(orderVO);
            }

            result.setItems(orderList);
            return result;
        } catch (Exception e) {
            throw new HashExApiException("查询历史订单时发生错误: " + e.getMessage(), e);
        }
    }


    /**
     * API返回的分页结果
     */
    public static class PageResult {
        private boolean hasPrev;  // 是否有上一页
        private boolean hasNext;  // 是否有���一页
        private List<OrderVO> list = new ArrayList<>(); // 订单列表

        public boolean isHasPrev() {
            return hasPrev;
        }

        public void setHasPrev(boolean hasPrev) {
            this.hasPrev = hasPrev;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public List<OrderVO> getList() {
            return list;
        }

        public void setList(List<OrderVO> list) {
            this.list = list;
        }
    }

    /**
     * 滚动分页结果类
     */
    public static class ScrollPageResult<T> {
        private boolean hasPrev;    // 是否有上一页
        private boolean hasNext;    // 是否有下一页
        private List<T> items = new ArrayList<>();  // 数据项列表，与返回的JSON结构一致

        public boolean isHasPrev() {
            return hasPrev;
        }

        public void setHasPrev(boolean hasPrev) {
            this.hasPrev = hasPrev;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public List<T> getList() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }

        // 兼容方法，便于代码重构
        public boolean isHasPrevPage() {
            return hasPrev;
        }

        public boolean isHasNextPage() {
            return hasNext;
        }

        public Long getNextId() {
            return null;  // API实际没有返回nextId
        }
    }


    /**
     * 订单详情VO类
     */
    public static class OrderVO {
        private Long orderId;         // 订单ID
        private String clientOrderId; // 客户端订单ID
        private String symbol;        // 交易对
        private String orderType;     // 订单类型：LIMIT或MARKET
        private String orderSide;     // 买卖方向：BUY或SELL
        private Integer balanceType;  // 账户类型：1现货账户，2杠杆账户
        private String timeInForce;   // 订单有效方式，如GTC
        private String price;         // 委托价格
        private String origQty;       // 原始委托数量
        private String avgPrice;      // 平均成交价
        private String executedQty;   // 已成交数量
        private String marginFrozen;  // 冻结保证金
        private String state;         // 订单状态
        private Long createdTime;     // 创建时间戳
        private String sourceId;      // 订单来源ID，可能为null
        private String forceClose;    // 是��强平标志，可能为null

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getClientOrderId() {
            return clientOrderId;
        }

        public void setClientOrderId(String clientOrderId) {
            this.clientOrderId = clientOrderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
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

        public Integer getBalanceType() {
            return balanceType;
        }

        public void setBalanceType(Integer balanceType) {
            this.balanceType = balanceType;
        }

        public String getTimeInForce() {
            return timeInForce;
        }

        public void setTimeInForce(String timeInForce) {
            this.timeInForce = timeInForce;
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

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Long getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Long createdTime) {
            this.createdTime = createdTime;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getForceClose() {
            return forceClose;
        }

        public void setForceClose(String forceClose) {
            this.forceClose = forceClose;
        }
    }
}