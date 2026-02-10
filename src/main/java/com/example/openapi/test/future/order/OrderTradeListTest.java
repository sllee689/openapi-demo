package com.example.openapi.test.future.order;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.HashExApiException;
import com.example.openapi.test.ApiResponse;
import com.example.openapi.test.future.FutureTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;


/**
 * 合约订单成交明细查询测试类
 */
public class OrderTradeListTest {

private static final Logger log = LoggerFactory.getLogger(OrderTradeListTest.class);
private static ApiClient apiClient;

public OrderTradeListTest(ApiClient apiClient) {
    OrderTradeListTest.apiClient = apiClient;
}

/**
 * 查询订单成交明细
 *
 * @param request 查询请求参数
 * @return 成交明细分页结果
 * @throws HashExApiException 如果API调用失败
 */
public PageResult<OrderTradeVO> getOrderTradeList(TradeListRequest request) throws HashExApiException {
    try {
        // 创建参数Map
        TreeMap<String, String> queryParams = new TreeMap<>();

        // 添加可选参数
        if (request.getSymbol() != null) {
            queryParams.put("symbol", request.getSymbol());
        }
        if (request.getOrderId() != null) {
            queryParams.put("orderId", request.getOrderId().toString());
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

        // 调用API
        String responseJson = apiClient.sendGetRequest("/fut/v1/order/trade-list", queryParams, true);

        // 解析响应JSON
        JSONObject jsonObject = JSONUtil.parseObj(responseJson);
        ApiResponse<JSONObject> apiResponse = JSONUtil.toBean(jsonObject,
                new cn.hutool.core.lang.TypeReference<ApiResponse<JSONObject>>() {}, false);

        if (apiResponse.getCode() != 0) {
            throw new HashExApiException("查询成交明细失败: " + apiResponse.getMsg());
        }

        // 解析数据部分
        JSONObject data = apiResponse.getData();
        if (data == null) {
            throw new HashExApiException("成交明细数据为空");
        }

        // 创建分页结果对象
        PageResult<OrderTradeVO> result = new PageResult<>();
        result.setPage(data.getInt("page", 1));
        result.setSize(data.getInt("ps", 10));
        result.setTotal(data.getLong("total", 0L));

        // 计算总页数
        int pages = (int) Math.ceil((double) result.getTotal() / result.getSize());
        result.setPages(pages);

        // 解析成交明细列表
        JSONArray itemsArray = data.getJSONArray("items");
        List<OrderTradeVO> tradeList = new ArrayList<>();

        if (itemsArray != null && !itemsArray.isEmpty()) {
            for (int i = 0; i < itemsArray.size(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                OrderTradeVO tradeVO = new OrderTradeVO();

                tradeVO.setOrderId(item.getStr("orderId"));
                tradeVO.setExecId(item.getStr("execId"));
                tradeVO.setSymbol(item.getStr("symbol"));
                tradeVO.setQuantity(item.getStr("quantity"));
                tradeVO.setPrice(item.getStr("price"));
                tradeVO.setFee(item.getStr("fee"));
                tradeVO.setFeeCoin(item.getStr("feeCoin"));
                tradeVO.setTimestamp(item.getLong("timestamp"));

                tradeList.add(tradeVO);
            }
        }

        result.setList(tradeList);
        return result;

    } catch (Exception e) {
        if (e instanceof HashExApiException) {
            throw (HashExApiException) e;
        }
        throw new HashExApiException("查询成交明细时出错: " + e.getMessage(), e);
    }
}

/**
 * 测试查询指定订单的成交明细
 */
private void testQueryByOrderId(String orderId) throws HashExApiException {
    log.info("===== 测试查询指定订单的成交明细 =====");

    TradeListRequest request = new TradeListRequest();
    request.setOrderId(orderId);
    request.setPage(1);
    request.setSize(10);

    PageResult<OrderTradeVO> result = getOrderTradeList(request);
    log.info("查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
            result.getTotal(), result.getPages(), result.getPage(), result.getSize());

    List<OrderTradeVO> trades = result.getList();
    if (trades != null && !trades.isEmpty()) {
        log.info("成交明细列表:");
        for (OrderTradeVO trade : trades) {
            log.info(trade.toString());
        }
    } else {
        log.info("未查询到成交明细");
    }
}

/**
 * 测试按时间范围查询成交明细
 */
private void testQueryByTimeRange() throws HashExApiException {
    log.info("===== 按时间范围查询成交明细 =====");

    TradeListRequest request = new TradeListRequest();

    // 查询最近24小时内的成交明细
    long endTime = System.currentTimeMillis();
    long startTime = endTime - 24 * 60 * 60 * 1000; // 24小时前

    request.setStartTime(startTime);
    request.setEndTime(endTime);
    request.setPage(1);
    request.setSize(10);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    log.info("查询时间范围: {} 至 {}",
            sdf.format(new Date(startTime)), sdf.format(new Date(endTime)));

    PageResult<OrderTradeVO> result = getOrderTradeList(request);
    log.info("查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
            result.getTotal(), result.getPages(), result.getPage(), result.getSize());

    List<OrderTradeVO> trades = result.getList();
    if (trades != null && !trades.isEmpty()) {
        log.info("成交明细列表:");
        for (OrderTradeVO trade : trades) {
            log.info(trade.toString());
        }
    } else {
        log.info("未查询到成交明细");
    }
}

/**
 * 测试按交易对查询成交明细
 */
private void testQueryBySymbol(String symbol) throws HashExApiException {
    log.info("===== 按交易对查询成交明细 =====");

    TradeListRequest request = new TradeListRequest();
    request.setSymbol(symbol);
    request.setPage(1);
    request.setSize(10);

    PageResult<OrderTradeVO> result = getOrderTradeList(request);
    log.info("查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
            result.getTotal(), result.getPages(), result.getPage(), result.getSize());

    List<OrderTradeVO> trades = result.getList();
    if (trades != null && !trades.isEmpty()) {
        log.info("成交明细列表:");
        for (OrderTradeVO trade : trades) {
            log.info(trade.toString());
        }
    } else {
        log.info("未查询到成交明细");
    }
}

public static void main(String[] args) throws HashExApiException {
    ApiClient client = new ApiClient(
            FutureTestConfig.BASE_URL,
            FutureTestConfig.ACCESS_KEY,
            FutureTestConfig.SECRET_KEY);

    OrderTradeListTest tradeListTest = new OrderTradeListTest(client);

    // 执行测试
    // 1. 测试查询指定订单的成交明细
    tradeListTest.testQueryByOrderId("477760118982191936");

    // 2. 测试按时间范围查询成交明细
    tradeListTest.testQueryByTimeRange();

    // 3. 测试按交易对查询成交明细
    tradeListTest.testQueryBySymbol("btc_usdt");
}

/**
 * 成交明细VO
 */
public static class OrderTradeVO {
    private String orderId;       // 订单ID
    private String execId;        // 成交ID
    private String symbol;        // 交易对
    private String quantity;      // 成交数量
    private String price;         // 成交价格
    private String fee;           // 手续费
    private String feeCoin;       // 手续费币种
    private Long timestamp;       // 成交时间戳

    // Getters and Setters
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

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = timestamp != null ? sdf.format(new Date(timestamp)) : "未知";

        return String.format("成交ID: %s | 订单ID: %s | 交易对: %s | 数量: %s | 价格: %s | 手续费: %s %s | 时间: %s",
                execId,
                orderId,
                symbol,
                quantity,
                price,
                fee,
                feeCoin != null ? feeCoin.toUpperCase() : "",
                timeStr);
    }
}

/**
 * 成交明细查询请求参数类
 */
public static class TradeListRequest {
    private String symbol;      // 交易对
    private String orderId;     // 订单ID
    private Long startTime;     // 开始时间
    private Long endTime;       // 结束时间
    private Integer page;       // 页码
    private Integer size;       // 每页大小

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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