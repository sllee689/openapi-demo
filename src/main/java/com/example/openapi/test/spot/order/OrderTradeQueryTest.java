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
 * 成交明细查询测试类
 */
public class OrderTradeQueryTest {

    private static final Logger log = LoggerFactory.getLogger(OrderTradeQueryTest.class);
    private static ApiClient apiClient;

    /**
     * 查询成交明细
     *
     * @param orderId     订单ID
     * @param symbol      交易对，如BTC_USDT
     * @param balanceType 账户类型 1.现货账户 2.杠杆账户
     * @param page        页码，从1开始
     * @param size        每页条数，最大100
     * @return 成交明细分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public PageResult<OrderTradeVO> getOrderTrades(
            Long orderId,
            String symbol,
            Integer balanceType,
            Integer page,
            Integer size
    ) throws HashExApiException {
        try {
            // 创建查询参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (orderId != null) {
                queryParams.put("orderId", orderId.toString());
            }

            if (symbol != null && !symbol.isEmpty()) {
                queryParams.put("symbol", symbol);
            }

            if (balanceType != null) {
                queryParams.put("balanceType", balanceType.toString());
            }

            if (page != null) {
                queryParams.put("page", page.toString());
            }

            if (size != null) {
                queryParams.put("size", size.toString());
            }

            // 调用API
            String responseJson = apiClient.sendGetRequest("/spot/v1/u/trade/order/deal", queryParams,true);

            // 解析响应JSON
            JSONObject jsonObject = new JSONObject(responseJson);

            // 使用TypeReference处理泛型
            ApiResponse<ApiPageResult> apiResponse = JSONUtil.toBean(jsonObject,
                    new cn.hutool.core.lang.TypeReference<ApiResponse<ApiPageResult>>() {}, false);

            if (apiResponse.getCode() != 0) {
                throw new HashExApiException("查询成交明细失败: " + apiResponse.getMessage());
            }

            // 从原始API返回转换到标准PageResult
            PageResult<OrderTradeVO> result = new PageResult<>();
            ApiPageResult apiPageResult = apiResponse.getData();

            // 设置分页信息
            result.setNumber(apiPageResult.getPage());
            result.setSize(apiPageResult.getPs());
            result.setTotalElements(apiPageResult.getTotal());
            result.setTotalPages((int) Math.ceil((double) apiPageResult.getTotal() / apiPageResult.getPs()));

            // 处理成交记录列表
            List<OrderTradeVO> trades = JSONUtil.toList(JSONUtil.parseArray(apiPageResult.getItems()),
                    OrderTradeVO.class);
            result.setItems(trades);

            return result;
        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询成交明细时出错: " + e.getMessage(), e);
        }
    }

    /**
     * API返回的分页结果原始格式
     */
    private static class ApiPageResult {
        private int page;       // 当前页码
        private int ps;         // 每页条数
        private long total;     // 总记录数
        private List<Object> items;  // 成交记录列表

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
     * 测试查询指定订单的成交明细
     */
    private void testQueryTradesByOrderId() throws HashExApiException {
        log.info("===== 测试查询指定订单的成交明细 =====");

        // 假设我们有一个已知的订单ID
        Long orderId = 475533479170587712L;

        log.info("查询订单ID {} 的成交明细", orderId);
        PageResult<OrderTradeVO> pageResult = getOrderTrades(
                orderId, null, null, 1, 10
        );

        // 打印查询结果
        log.info("共查询到 {} 条成交记录，总计 {} 条记录",
                pageResult.getItems().size(), pageResult.getTotalElements());
        log.info("当前页: {}, 总页数: {}", pageResult.getNumber(), pageResult.getTotalPages());

        // 打印成交明细
        int index = 1;
        for (OrderTradeVO trade : pageResult.getItems()) {
            log.info("成交记录 #{}", index++);
            log.info("  订单ID: {}", trade.getOrderId());
            log.info("  成交ID: {}", trade.getExecId());
            log.info("  币对: {}", trade.getSymbol());
            log.info("  成交价格: {}", trade.getPrice());
            log.info("  成交数量: {}", trade.getQuantity());
            log.info("  手续费: {}", trade.getFee());
            log.info("  手续费币种: {}", trade.getFeeCoin());
            log.info("  成交时间: {}", trade.getTimestamp());
        }
    }

    /**
     * 测试查询指定交易对的成交明细
     */
    private void testQueryTradesBySymbol() throws HashExApiException {
        log.info("===== 测试查询指定交易对的成交明细 =====");

        // 查询BTC_USDT交易对的成交明细
        String symbol = "BTC_USDT";
        Integer balanceType = 1; // 现货账户
        Integer page = 1;
        Integer size = 10;

        log.info("查询币对 {} 账户类型 {} 的成交明细，第 {} 页，每页 {} 条",
                symbol, balanceType, page, size);

        PageResult<OrderTradeVO> pageResult = getOrderTrades(
                null, symbol, balanceType, page, size
        );

        // 打印查询结果
        log.info("共查询到 {} 条成交记录，总计 {} 条记录",
                pageResult.getItems().size(), pageResult.getTotalElements());

        // 打印成交明细
        int index = 1;
        for (OrderTradeVO trade : pageResult.getItems()) {
            log.info("成交记录 #{}", index++);
            log.info("  成交ID: {}", trade.getExecId());
            log.info("  订单ID: {}", trade.getOrderId());
            log.info("  币对: {}", trade.getSymbol());
            log.info("  成交价格: {}", trade.getPrice());
            log.info("  成交数量: {}", trade.getQuantity());
            log.info("  手续费: {}", trade.getFee());
            log.info("  手续费币种: {}", trade.getFeeCoin());
            log.info("  成交时间: {}", trade.getTimestamp());
        }
    }

    /**
     * 测试分页查询
     */
    private void testPaginationQuery() throws HashExApiException {
        log.info("===== 测试分页查询成交明细 =====");

        String symbol = "BTC_USDT";
        Integer size = 5;

        // 第一页
        log.info("查询第一页数据，每页 {} 条", size);
        PageResult<OrderTradeVO> page1 = getOrderTrades(
                null, symbol, 1, 1, size
        );

        log.info("第一页共 {} 条记录，总计 {} 条记录",
                page1.getItems().size(), page1.getTotalElements());

        if (page1.getTotalPages() > 1) {
            // 查询第二页
            log.info("查询第二页数据，每页 {} 条", size);
            PageResult<OrderTradeVO> page2 = getOrderTrades(
                    null, symbol, 1, 2, size
            );

            log.info("第二页共 {} 条记录", page2.getItems().size());

            // 打印第二页成交记录
            int index = 1;
            for (OrderTradeVO trade : page2.getItems()) {
                log.info("成交记录 #{}", index++);
                log.info("  成交ID: {}", trade.getExecId());
                log.info("  订单ID: {}", trade.getOrderId());
                log.info("  币对: {}", trade.getSymbol());
                log.info("  成交价格: {}", trade.getPrice());
                log.info("  成交数量: {}", trade.getQuantity());
            }
        } else {
            log.info("没有第二页数据");
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient("https://open.hashex.vip",
                "0a9970e8986247d6e6d5deadc886a4e558c0a1c4f2047c2a00bc96e2efd24499",
                "dd89a125f1ebaa52e4dd0cff848424eb49e51526e2d585bfedfbc8d055a2b01a");
        OrderTradeQueryTest tradeTest = new OrderTradeQueryTest();

        // 测试查询指定订单的成交明细
        tradeTest.testQueryTradesByOrderId();

        // 测试查询指定交易对的成交明细
        tradeTest.testQueryTradesBySymbol();

        // 测试分页查询
        tradeTest.testPaginationQuery();
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
     * 成交明细VO
     */
    public static class OrderTradeVO {
        private String orderId;      // 订单ID
        private String execId;       // 成交ID
        private String symbol;       // 交易对
        private String quantity;     // 成交数量
        private String price;        // 成交价格
        private String fee;          // 手续费
        private String feeCoin;      // 手续费币种
        private Long timestamp;      // 成交时间戳

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getExecId() {
            return execId;
        }

        public void setExecId(String execId) {
            this.execId = execId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public String getFeeCoin() {
            return feeCoin;
        }

        public void setFeeCoin(String feeCoin) {
            this.feeCoin = feeCoin;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}