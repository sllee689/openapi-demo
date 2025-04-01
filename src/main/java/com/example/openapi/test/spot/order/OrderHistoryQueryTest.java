package com.example.openapi.test.spot.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeMap;

/**
 * 历史订单查询测试类
 */
public class OrderHistoryQueryTest {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryQueryTest.class);
    private static ApiClient apiClient;



    /**
     * 测试查询BTC_USDT最近订单历史
     */
    private void testQueryRecentOrders() throws HashExApiException {
        log.info("===== 测试查询最近订单历史 =====");

        // 查询BTC_USDT最近20条订单历史
        String symbol = "BTC_USDT";
        Integer limit = 20;

        log.info("查询 {} 最近 {} 条订单历史", symbol, limit);
        ScrollPageResult<OrderQueryTest.OrderVO> pageResult = getHistoryOrders(
                symbol, null, null, null, null, null, limit
        );

        // 打印查询结果
        log.info("共查询到 {} 条订单记录", pageResult.getList().size());
        log.info("是否还有上一页: {}", pageResult.isHasPrevPage());
        log.info("是否还有下一页: {}", pageResult.isHasNextPage());
        if (pageResult.isHasNextPage()) {
            log.info("下一页ID: {}", pageResult.getNextId());
        }

        // 打印订单详情
        int index = 1;
        for (OrderQueryTest.OrderVO order : pageResult.getList()) {
            log.info("订单 #{}", index++);
            log.info("  订单ID: {}", order.getOrderId());
            log.info("  客户端订单ID: {}", order.getClientOrderId());
            log.info("  币对: {}", order.getSymbol());
            log.info("  订单类型: {}", order.getOrderType());
            log.info("  方向: {}", order.getOrderSide());
            log.info("  价格: {}", order.getPrice());
            log.info("  原始数量: {}", order.getOrigQty());
            log.info("  已成交数量: {}", order.getExecutedQty());
            log.info("  平均成交价: {}", order.getAvgPrice());
            log.info("  状态: {} ({})", order.getState(), order.getStateText());
            log.info("  创建时间: {}", order.getCreatedTime());
        }
    }

    /**
     * 测试按条件查询订单历史
     */
    private void testQueryOrdersWithConditions() throws HashExApiException {
        log.info("===== 测试按条件查询订单历史 =====");

        // 查询过去24小时内的BTC_USDT买单
        String symbol = "BTC_USDT";
        String direction = "1";
        // 计算24小时前的时间戳
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);
        Integer limit = 10;

        log.info("查询币对 {} 方向 {} 从 {} 至 {} 的最近 {} 条订单",
                symbol, direction, startTime, endTime, limit);

        ScrollPageResult<OrderQueryTest.OrderVO> pageResult = getHistoryOrders(
                symbol, direction, 1, startTime, endTime, null, limit
        );

        // 打印查询结果
        log.info("共查询到 {} 条订单记录", pageResult.getList().size());

        // 打印订单详情
        int index = 1;
        for (OrderQueryTest.OrderVO order : pageResult.getList()) {
            log.info("订单 #{}", index++);
            log.info("  订单ID: {}", order.getOrderId());
            log.info("  币对: {}", order.getSymbol());
            log.info("  方向: {}", order.getOrderSide());
            log.info("  价格: {}", order.getPrice());
            log.info("  数量: {}", order.getOrigQty());
            log.info("  状态: {} ({})", order.getState(), order.getStateText());
            log.info("  创建时间: {}", order.getCreatedTime());
        }
    }

    /**
     * 分页查询测试
     */
    private void testPaginationQuery() throws HashExApiException {
        log.info("===== 测试分页查询订单历史 =====");

        String symbol = "BTC_USDT";
        Integer limit = 5;
        Long nextId = null;

        // 第一页
        log.info("查询第一页数据，每页 {} 条", limit);
        ScrollPageResult<OrderQueryTest.OrderVO> page1 = getHistoryOrders(
                symbol, null, null, null, null, nextId, limit
        );

        log.info("第一页共 {} 条记录", page1.getList().size());
        nextId = page1.getNextId();

        if (page1.isHasNextPage() && nextId != null) {
            // 第二页
            log.info("查询第二页数据，ID从 {} 开始", nextId);
            ScrollPageResult<OrderQueryTest.OrderVO> page2 = getHistoryOrders(
                    symbol, null, null, null, null, nextId, limit
            );

            log.info("第二页共 {} 条记录", page2.getList().size());

            // 打印第二页订单
            int index = 1;
            for (OrderQueryTest.OrderVO order : page2.getList()) {
                log.info("第二页订单 #{}", index++);
                log.info("  订单ID: {}", order.getOrderId());
                log.info("  币对: {}", order.getSymbol());
                log.info("  创建时间: {}", order.getCreatedTime());
            }
        } else {
            log.info("没有更多页");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "yZGQ4OWExMjVmMWViYWE1MmU0ZGQwY2ZmODQ4NDI0ZWI0OWU1MTUyNmUyZDU4NWJmZWRmYmM4ZDA1NWEyYjAxMmE=");
        OrderHistoryQueryTest historyTest = new OrderHistoryQueryTest();

        // 测试查询最近订单历史
        historyTest.testQueryRecentOrders();

        // 测试按条件查询订单历史
        historyTest.testQueryOrdersWithConditions();

        // 测试分页查询
         historyTest.testPaginationQuery();
    }
    /**
     * 查询历史订单
     *
     * @param symbol     交易对，如BTC_USDT
     * @param direction  交易方向，如BUY、SELL，不传表示全部
     * @param balanceType 账户类型 1.现货账户 2.杠杆账户
     * @param startTime  起始时间戳
     * @param endTime    结束时间戳
     * @param id         分页ID
     * @param limit      每页数量
     * @return 订单历史列表
     * @throws HashExApiException 如果API调用失败
     */
    public ScrollPageResult<OrderQueryTest.OrderVO> getHistoryOrders(
            String symbol,
            String direction,
            Integer balanceType,
            Long startTime,
            Long endTime,
            Long id,
            Integer limit
    ) throws HashExApiException {
        try {
            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (symbol != null && !symbol.isEmpty()) {
                queryParams.put("symbol", symbol);
            }

//            if (direction != null && !direction.isEmpty()) {
//                queryParams.put("direction", direction);
//            }

            if (balanceType != null) {
                queryParams.put("balanceType", balanceType.toString());
            }

            if (startTime != null) {
                queryParams.put("startTime", startTime.toString());
            }

            if (endTime != null) {
                queryParams.put("endTime", endTime.toString());
            }

            if (id != null) {
                queryParams.put("id", id.toString());
            }

            if (limit != null) {
                queryParams.put("limit", limit.toString());
            }

            // 调用API
            String responseJson = apiClient.sendGetRequest("/spot/v1/u/trade/order/history", queryParams);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用TypeReference处理泛型
            ApiResponse<PageResult> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<PageResult>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("查询历史订单失败: " + apiResponse.getMessage());
            }

            // 从PageResult转换到ScrollPageResult
            ScrollPageResult<OrderQueryTest.OrderVO> result = new ScrollPageResult<>();
            PageResult pageResult = apiResponse.getData();

            // 设置分页信息
            result.setHasNextPage(pageResult.isHasNext());
            result.setHasPrevPage(pageResult.isHasPrev());

            // 处理订单列表
            List<OrderQueryTest.OrderVO> orders = JSONUtil.toList(JSONUtil.parseArray(pageResult.getItems()),
                    OrderQueryTest.OrderVO.class);
            result.setList(orders);

            // 设置下一页ID（如果有下一页）
            if (pageResult.isHasNext() && !orders.isEmpty()) {
                OrderQueryTest.OrderVO lastOrder = orders.get(orders.size() - 1);
                result.setNextId(Long.parseLong(lastOrder.getOrderId()));
            }

            return result;
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询历史订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * API返回的分页结果原始格式
     */
    private static class PageResult {
        private boolean hasPrev;  // 是否有上一页
        private boolean hasNext;  // 是否有下一页
        private List<Object> items;  // 订单数据列表

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

        public List<Object> getItems() {
            return items;
        }

        public void setItems(List<Object> items) {
            this.items = items;
        }
    }

    /**
     * 滚动分页结果类
     */
    public static class ScrollPageResult<T> {
        private List<T> list;         // 数据列表
        private boolean hasNextPage;  // 是否有下一页
        private boolean hasPrevPage;  // 是否有上一页
        private Long nextId;          // 下一页的ID

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPrevPage() {
            return hasPrevPage;
        }

        public void setHasPrevPage(boolean hasPrevPage) {
            this.hasPrevPage = hasPrevPage;
        }

        public Long getNextId() {
            return nextId;
        }

        public void setNextId(Long nextId) {
            this.nextId = nextId;
        }
    }
}