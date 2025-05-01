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
 * 未完成订单查询测试类
 */
public class UnfinishedOrderQueryTest {

    private static final Logger log = LoggerFactory.getLogger(UnfinishedOrderQueryTest.class);
    private static ApiClient apiClient;

    public static  void setApiClient(ApiClient apiClient) {
        UnfinishedOrderQueryTest.apiClient = apiClient;
    }

    /**
     * 查询未完成订单
     *
     * @param symbol        交易对，如BTC_USDT
     * @param balanceType   账户类型 1.现货账户 2.杠杆账户
     * @param clientOrderId 自定义订单ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param state         订单状态 1：新建订单;未成交; 2：部分成交；3：全部成交；4：已撤销；5：下单失败；6：已过期; 9:未完成；10：历史订单
     * @param page          页码，从1开始
     * @param size          每页条数，最大100
     * @return 未完成订单分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public PageResult<OrderVO> getUnfinishedOrders(
            String symbol,
            Integer balanceType,
            String clientOrderId,
            Long startTime,
            Long endTime,
            Integer state,
            Integer page,
            Integer size
    ) throws HashExApiException {
        try {
            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (symbol != null && !symbol.isEmpty()) {
                queryParams.put("symbol", symbol);
            }

            if (balanceType != null) {
                queryParams.put("balanceType", balanceType.toString());
            }

            if (clientOrderId != null && !clientOrderId.isEmpty()) {
                queryParams.put("clientOrderId", clientOrderId);
            }

            if (startTime != null) {
                queryParams.put("startTime", startTime.toString());
            }

            if (endTime != null) {
                queryParams.put("endTime", endTime.toString());
            }

            if (state != null) {
                queryParams.put("state", state.toString());
            }

            if (page != null) {
                queryParams.put("page", page.toString());
            }

            if (size != null) {
                queryParams.put("size", size.toString());
            }
            queryParams.put("state", null);

            // 调用API - 使用正确的API路径(添加/spot/v1前缀)
            String responseJson = apiClient.sendGetRequest("/spot/v1/u/trade/order/list", queryParams,true);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用TypeReference处理泛型
            ApiResponse<ApiPageResult> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<>() {
                    }, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("查询未完成订单失败: " + apiResponse.getMsg());
            }

            // 从API返回转换到标准PageResult
            PageResult<OrderVO> result = new PageResult<>();
            ApiPageResult apiPageResult = apiResponse.getData();

            // 设置分页信息
            result.setNumber(apiPageResult.getPage());
            result.setSize(apiPageResult.getPs());
            result.setTotalElements(apiPageResult.getTotal());
            result.setTotalPages((int) Math.ceil((double) apiPageResult.getTotal() / apiPageResult.getPs()));

            // 处理订单列表
            List<OrderVO> orders = JSONUtil.toList(JSONUtil.parseArray(apiPageResult.getItems()), OrderVO.class);
            result.setItems(orders);

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询未完成订单时出错: " + e.getMessage(), e);
        }
    }

    /**
     * API返回的分页结果原始格式
     */
    private static class ApiPageResult {
        private int page;       // 当前页码
        private int ps;         // 每页条数
        private long total;     // 总记录数
        private List<Object> items;  // 订单记录列表

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPs() {
            return ps;
        }

        public void setPs(int ps) {
            this.ps = ps;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public List<Object> getItems() {
            return items;
        }

        public void setItems(List<Object> items) {
            this.items = items;
        }
    }

    /**
     * 测试查询未完成订单
     */
    private void testQueryUnfinishedOrders() throws HashExApiException {
        log.info("===== 测试查询未完成订单 =====");

        // 查询BTC_USDT交易对的未完成订单
        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户
        Integer state = 9; // 9表示未完成订单
        Integer page = 1;
        Integer size = 10;

        log.info("查询币对 {} 账户类型 {} 的未完成订单，第 {} 页，每页 {} 条",
                symbol, balanceType, page, size);

        PageResult<OrderVO> pageResult = getUnfinishedOrders(
                symbol, balanceType, null, null, null, state, page, size
        );

        // 打印查询结果
        log.info("共查询到 {} 条未完成订单，总计 {} 条记录",
                pageResult.getItems().size(), pageResult.getTotalElements());
        log.info("当前页: {}, 总页数: {}", pageResult.getNumber(), pageResult.getTotalPages());

        // 打印订单明细
        int index = 1;
        for (OrderVO order : pageResult.getItems()) {
            log.info("订单 #{}", index++);
            log.info("  订单ID: {}", order.getOrderId());
            log.info("  客户订单ID: {}", order.getClientOrderId());
            log.info("  币对: {}", order.getSymbol());
            log.info("  订单类型: {}", order.getOrderType());
            log.info("  交易方向: {}", order.getOrderSide());
            log.info("  账户类型: {}", order.getBalanceType());
            log.info("  价格: {}", order.getPrice());
            log.info("  数量: {}", order.getOrigQty());
            log.info("  已成交数量: {}", order.getExecutedQty());
            log.info("  保证金冻结: {}", order.getMarginFrozen());
            log.info("  状态: {}", order.getState());
            log.info("  创建时间: {}", order.getCreatedTime());
        }
    }

    /**
     * 测试使用特定条件查询未完成订单
     */
    private void testQueryWithConditions() throws HashExApiException {
        log.info("===== 测试条件查询未完成订单 =====");

        // 设置查询条件
        String symbol = "BTC_USDT";
        Integer balanceType = 1;
        Long startTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000; // 一周前
        Long endTime = System.currentTimeMillis();

        log.info("查询交易对: {}, 账户类型: {}, 时间范围: {} 到 {}",
                symbol, balanceType, startTime, endTime);

        PageResult<OrderVO> pageResult = getUnfinishedOrders(
                symbol, balanceType, null, startTime, endTime, null, 1, 10
        );

        log.info("共查询到 {} 条符合条件的订单", pageResult.getItems().size());

        // 打印订单摘要
        for (int i = 0; i < pageResult.getItems().size(); i++) {
            OrderVO order = pageResult.getItems().get(i);
            log.info("订单 #{}: ID={}, 类型={}, 方向={}, 价格={}, 数量={}, 状态={}",
                    i+1, order.getOrderId(), order.getOrderType(), order.getOrderSide(),
                    order.getPrice(), order.getOrigQty(), order.getState());
        }
    }

    /**
     * 测试分页查询
     */
    private void testPaginationQuery() throws HashExApiException {
        log.info("===== 测试分页查询未完成订单 =====");

        String symbol = "BTC_USDT";
        Integer size = 5;

        // 第一页
        log.info("查询第一页数据，每页 {} 条", size);
        PageResult<OrderVO> page1 = getUnfinishedOrders(
                symbol, 1, null, null, null, null, 1, size
        );

        log.info("第一页共 {} 条记录，总计 {} 条记录",
                page1.getItems().size(), page1.getTotalElements());

        if (page1.getTotalPages() > 1) {
            // 查询第二页
            log.info("查询第二页数据，每页 {} 条", size);
            PageResult<OrderVO> page2 = getUnfinishedOrders(
                    symbol, 1, null, null, null, null, 2, size
            );

            log.info("第二页共 {} 条记录", page2.getItems().size());

            // 打印第二页订单
            int index = 1;
            for (OrderVO order : page2.getItems()) {
                log.info("第二页订单 #{}", index++);
                log.info("  订单ID: {}", order.getOrderId());
                log.info("  币对: {}", order.getSymbol());
                log.info("  创建时间: {}", order.getCreatedTime());
            }
        } else {
            log.info("没有第二页数据");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        //替换自己的 accessKey 和 secretKey
        apiClient = new ApiClient("https://open.mgbx.com",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        UnfinishedOrderQueryTest orderTest = new UnfinishedOrderQueryTest();

        // 测试查询未完成订单
        orderTest.testQueryUnfinishedOrders();

        // 测试条件查询
        orderTest.testQueryWithConditions();

        // 测试分页查询
        orderTest.testPaginationQuery();
    }

    /**
     * 标准分页结果类
     */
    public static class PageResult<T> {
        private List<T> items;          // 数据列表
        private Integer number;          // 当前页码
        private Integer size;            // 每页大小
        private Long totalElements;      // 总记录数
        private Integer totalPages;      // 总页数

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(Long totalElements) {
            this.totalElements = totalElements;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }
    }

    /**
     * 订单数据模型类
     */
    public static class OrderVO {
        private String orderId;         // 订单ID
        private String clientOrderId;   // 客户订单ID
        private String symbol;          // 交易对
        private String orderType;       // 订单类型：LIMIT限价，MARKET市价
        private String orderSide;       // 交易方向：BUY买入，SELL卖出
        private Integer balanceType;    // 账户类型：1现货，2杠杆
        private String timeInForce;     // 有效方式：GTC, IOC, FOK
        private String price;           // 价格
        private String origQty;         // 原始数量
        private String avgPrice;        // 平均成交价
        private String executedQty;     // 已成交数量
        private String marginFrozen;    // 保证金冻结
        private String sourceId;        // 来源ID
        private String forceClose;      // 强平标识
        private String state;           // 状态
        private Long createdTime;       // 创建时间

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
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

        /**
         * 获取状态文本描述
         */
        public String getStateText() {
            if (state == null) return "未知";

            switch (state) {
                case "NEW": return "未成交";
                case "PARTIALLY_FILLED": return "部分成交";
                case "FILLED": return "全部成交";
                case "CANCELED": return "已撤销";
                case "REJECTED": return "下单失败";
                case "EXPIRED": return "已过期";
                default: return state;
            }
        }
    }
}