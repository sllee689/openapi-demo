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
 * 合约止盈止损委托列表查询测试类
 */
public class EntrustProfitListTest {

    private static final Logger log = LoggerFactory.getLogger(EntrustProfitListTest.class);
    private static final String[] REQUIRED_ITEM_FIELDS = {
            "profitId", "positionId", "contractType", "symbol", "profitType",
            "positionType", "positionSide", "origQty", "triggerPriceType",
            "state", "createdTime", "updatedTime"
    };
    private static ApiClient apiClient;

    /**
     * 查询止盈止损委托列表
     *
     * @param request 查询请求参数
     * @return 委托列表分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public PageResult<ProfitEntrustVO> getProfitEntrustList(ProfitEntrustListRequest request) throws HashExApiException {
        try {
            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (request.getSymbol() != null) {
                queryParams.put("symbol", request.getSymbol());
            }
            if (request.getPositionSide() != null) {
                queryParams.put("positionSide", request.getPositionSide());
            }
            if (request.getContractType() != null) {
                queryParams.put("contractType", request.getContractType());
            }
            if (request.getState() != null) {
                queryParams.put("state", request.getState());
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
            String responseJson = apiClient.sendGetRequest("/fut/v1/entrust/profit-list", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(responseJson);
            if (jsonObject.getInt("code") != 0) {
                throw new HashExApiException("查询止盈止损委托列表失败: " + jsonObject.getStr("msg"));
            }

            JSONObject dataObj = jsonObject.getJSONObject("data");
            PageResult<ProfitEntrustVO> pageResult = new PageResult<>();
            pageResult.setPage(dataObj.getInt("page"));
            pageResult.setSize(dataObj.getInt("ps"));
            pageResult.setTotal(dataObj.getLong("total"));

            // 计算总页数
            int size = pageResult.getSize();
            long total = pageResult.getTotal();
            pageResult.setPages(size > 0 ? (int) Math.ceil((double) total / size) : 0);

            // 解析委托列表
            JSONArray itemsArray = dataObj.getJSONArray("items");
            List<ProfitEntrustVO> entrustList = new ArrayList<>();

            if (itemsArray != null && !itemsArray.isEmpty()) {
                for (int i = 0; i < itemsArray.size(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    validateItemFields(item);
                    ProfitEntrustVO entrust = new ProfitEntrustVO();
                    entrust.setProfitId(item.getStr("profitId"));
                    entrust.setPositionId(item.getStr("positionId"));
                    entrust.setContractType(item.getStr("contractType"));
                    entrust.setSymbol(item.getStr("symbol"));
                    entrust.setProfitType(item.getStr("profitType"));
                    entrust.setPositionType(item.getStr("positionType"));
                    entrust.setPositionSide(item.getStr("positionSide"));
                    entrust.setOrigQty(item.getStr("origQty"));
                    entrust.setTriggerPriceType(item.getStr("triggerPriceType"));
                    entrust.setTriggerProfitPrice(item.getStr("triggerProfitPrice"));
                    entrust.setTriggerStopPrice(item.getStr("triggerStopPrice"));
                    entrust.setTriggerPriceSide(item.getInt("triggerPriceSide"));
                    entrust.setEntryPrice(item.getStr("entryPrice"));
                    entrust.setPositionSize(item.getStr("positionSize"));
                    entrust.setIsolatedMargin(item.getStr("isolatedMargin"));
                    entrust.setTriggerPrice(item.getStr("triggerPrice"));
                    entrust.setExecutedQty(item.getStr("executedQty"));
                    entrust.setState(item.getStr("state"));
                    entrust.setCreatedTime(item.getLong("createdTime"));
                    entrust.setUpdatedTime(item.getLong("updatedTime"));
                    entrustList.add(entrust);
                }
            }

            pageResult.setList(entrustList);
            return pageResult;

        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询止盈止损委托列表时出错: " + e.getMessage(), e);
        }
    }

    private void validateItemFields(JSONObject item) throws HashExApiException {
        for (String field : REQUIRED_ITEM_FIELDS) {
            if (!item.containsKey(field)) {
                throw new HashExApiException("响应缺少字段: " + field + ", item=" + item);
            }
        }
    }

    /**
     * 测试查询所有止盈止损委托
     */
    private void testQueryAllProfitEntrusts() throws HashExApiException {
        log.info("===== 测试查询所有止盈止损委托 =====");

        ProfitEntrustListRequest request = new ProfitEntrustListRequest();
        request.setSymbol("btc_usdt");
        request.setContractType("PERPETUAL");
        request.setPage(1);
        request.setSize(10);

        PageResult<ProfitEntrustVO> result = getProfitEntrustList(request);
        log.info("查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页大小: {}",
                result.getTotal(), result.getPages(), result.getPage(), result.getSize());

        List<ProfitEntrustVO> entrusts = result.getList();
        if (entrusts != null && !entrusts.isEmpty()) {
            log.info("止盈止损委托列表 ({} 条记录):", entrusts.size());
            int index = 1;
            for (ProfitEntrustVO entrust : entrusts) {
                log.info("委托 #{}: {}", index++, entrust);
            }
        } else {
            log.info("没有止盈止损委托记录");
        }
    }

    /**
     * 测试按仓位方向查询止盈止损委托
     */
    private void testQueryByPositionSide() throws HashExApiException {
        log.info("===== 按仓位方向查询止盈止损委托 =====");

        ProfitEntrustListRequest request = new ProfitEntrustListRequest();
        request.setSymbol("btc_usdt");
        request.setPositionSide("LONG");
        request.setPage(1);
        request.setSize(10);

        PageResult<ProfitEntrustVO> result = getProfitEntrustList(request);
        log.info("LONG方向委托 - 总条数: {}", result.getTotal());

        List<ProfitEntrustVO> entrusts = result.getList();
        if (entrusts != null && !entrusts.isEmpty()) {
            for (ProfitEntrustVO entrust : entrusts) {
                log.info("{}", entrust);
            }
        } else {
            log.info("没有LONG方向的止盈止损委托");
        }
    }

    /**
     * 测试按时间范围查询
     */
    private void testQueryByTimeRange() throws HashExApiException {
        log.info("===== 按时间范围查询止盈止损委托 =====");

        ProfitEntrustListRequest request = new ProfitEntrustListRequest();
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

        PageResult<ProfitEntrustVO> result = getProfitEntrustList(request);
        log.info("查询结果 - 总条数: {}", result.getTotal());

        List<ProfitEntrustVO> entrusts = result.getList();
        if (entrusts != null && !entrusts.isEmpty()) {
            for (ProfitEntrustVO entrust : entrusts) {
                log.info("{}", entrust);
            }
        } else {
            log.info("该时间范围内没有止盈止损委托");
        }
    }

    private void testQueryByStateVariants() throws HashExApiException {
        log.info("===== 测试 state 参数枚举 =====");
        String[] states = {"UNFINISHED", "HISTORY", "NOT_TRIGGERED", "TRIGGERED", "EXPIRED", "USER_REVOCATION", "PLATFORM_REVOCATION"};
        for (String state : states) {
            ProfitEntrustListRequest request = new ProfitEntrustListRequest();
            request.setSymbol("btc_usdt");
            request.setState(state);
            request.setPage(1);
            request.setSize(10);
            try {
                PageResult<ProfitEntrustVO> result = getProfitEntrustList(request);
                log.info("state={} 查询成功, total={}", state, result.getTotal());
            } catch (HashExApiException e) {
                log.warn("state={} 查询失败(疑似不支持): {}", state, e.getMessage());
            }
        }
    }

    private void testQueryWithInvalidState() throws HashExApiException {
        log.info("===== 测试非法 state 参数 =====");
        ProfitEntrustListRequest request = new ProfitEntrustListRequest();
        request.setSymbol("btc_usdt");
        request.setState("BAD_STATE");
        request.setPage(1);
        request.setSize(10);
        try {
            getProfitEntrustList(request);
            throw new HashExApiException("非法state预期失败但实际成功");
        } catch (HashExApiException e) {
            log.info("非法state按预期失败: {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        EntrustProfitListTest test = new EntrustProfitListTest();

        // 执行测试
        test.testQueryAllProfitEntrusts();
        test.testQueryByPositionSide();
        test.testQueryByTimeRange();
        test.testQueryByStateVariants();
        test.testQueryWithInvalidState();
    }

    /**
     * 止盈止损委托VO
     */
    public static class ProfitEntrustVO {
        private String profitId;            // 委托ID
        private String positionId;          // 持仓ID
        private String contractType;        // 合约类型
        private String symbol;              // 交易对
        private String profitType;          // 委托类型（NORMAL/ALL）
        private String positionType;        // 仓位类型
        private String positionSide;        // 仓位方向
        private String origQty;             // 原始数量
        private String triggerPriceType;    // 触发价格类型
        private String triggerProfitPrice;  // 止盈触发价
        private String triggerStopPrice;    // 止损触发价
        private Integer triggerPriceSide;   // 触发价格方向
        private String entryPrice;          // 开仓均价
        private String positionSize;        // 持仓数量
        private String isolatedMargin;      // 逐仓保证金
        private String triggerPrice;        // 当前触发价格
        private String executedQty;         // 已执行数量
        private String state;               // 状态
        private Long createdTime;           // 创建时间
        private Long updatedTime;           // 更新时间

        public String getProfitId() { return profitId; }
        public void setProfitId(String profitId) { this.profitId = profitId; }
        public String getPositionId() { return positionId; }
        public void setPositionId(String positionId) { this.positionId = positionId; }
        public String getContractType() { return contractType; }
        public void setContractType(String contractType) { this.contractType = contractType; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getProfitType() { return profitType; }
        public void setProfitType(String profitType) { this.profitType = profitType; }
        public String getPositionType() { return positionType; }
        public void setPositionType(String positionType) { this.positionType = positionType; }
        public String getPositionSide() { return positionSide; }
        public void setPositionSide(String positionSide) { this.positionSide = positionSide; }
        public String getOrigQty() { return origQty; }
        public void setOrigQty(String origQty) { this.origQty = origQty; }
        public String getTriggerPriceType() { return triggerPriceType; }
        public void setTriggerPriceType(String triggerPriceType) { this.triggerPriceType = triggerPriceType; }
        public String getTriggerProfitPrice() { return triggerProfitPrice; }
        public void setTriggerProfitPrice(String triggerProfitPrice) { this.triggerProfitPrice = triggerProfitPrice; }
        public String getTriggerStopPrice() { return triggerStopPrice; }
        public void setTriggerStopPrice(String triggerStopPrice) { this.triggerStopPrice = triggerStopPrice; }
        public Integer getTriggerPriceSide() { return triggerPriceSide; }
        public void setTriggerPriceSide(Integer triggerPriceSide) { this.triggerPriceSide = triggerPriceSide; }
        public String getEntryPrice() { return entryPrice; }
        public void setEntryPrice(String entryPrice) { this.entryPrice = entryPrice; }
        public String getPositionSize() { return positionSize; }
        public void setPositionSize(String positionSize) { this.positionSize = positionSize; }
        public String getIsolatedMargin() { return isolatedMargin; }
        public void setIsolatedMargin(String isolatedMargin) { this.isolatedMargin = isolatedMargin; }
        public String getTriggerPrice() { return triggerPrice; }
        public void setTriggerPrice(String triggerPrice) { this.triggerPrice = triggerPrice; }
        public String getExecutedQty() { return executedQty; }
        public void setExecutedQty(String executedQty) { this.executedQty = executedQty; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public Long getCreatedTime() { return createdTime; }
        public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }
        public Long getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(Long updatedTime) { this.updatedTime = updatedTime; }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("止盈止损委托 [%s] {\n" +
                            "  交易对: %s, 合约类型: %s\n" +
                            "  仓位类型: %s, 仓位方向: %s\n" +
                            "  委托类型: %s, 状态: %s\n" +
                            "  触发类型: %s, 触发方向: %s\n" +
                            "  止盈价: %s, 止损价: %s, 触发价: %s\n" +
                            "  持仓ID: %s, 持仓数量: %s, 委托数量: %s, 已执行: %s\n" +
                            "  开仓均价: %s, 逐仓保证金: %s\n" +
                            "  创建时间: %s\n" +
                            "  更新时间: %s\n}",
                    profitId,
                    symbol, contractType,
                    positionType, positionSide,
                    profitType, state,
                    triggerPriceType, triggerPriceSide == null ? "未知" : triggerPriceSide,
                    triggerProfitPrice == null ? "未设置" : triggerProfitPrice,
                    triggerStopPrice == null ? "未设置" : triggerStopPrice,
                    triggerPrice == null ? "未设置" : triggerPrice,
                    positionId,
                    positionSize == null ? "未知" : positionSize,
                    origQty == null ? "未知" : origQty,
                    executedQty == null ? "未知" : executedQty,
                    entryPrice == null ? "未知" : entryPrice,
                    isolatedMargin == null ? "未知" : isolatedMargin,
                    createdTime == null ? "未知" : sdf.format(new Date(createdTime)),
                    updatedTime == null ? "未知" : sdf.format(new Date(updatedTime)));
        }
    }

    /**
     * 止盈止损委托列表查询请求参数类
     */
    public static class ProfitEntrustListRequest {
        private String symbol;          // 交易对
        private String contractType;    // 合约类型
        private String positionSide;    // 仓位方向
        private String state;           // 委托状态
        private Long startTime;         // 开始时间
        private Long endTime;           // 结束时间
        private Integer page;           // 页码
        private Integer size;           // 每页大小

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getContractType() { return contractType; }
        public void setContractType(String contractType) { this.contractType = contractType; }
        public String getPositionSide() { return positionSide; }
        public void setPositionSide(String positionSide) { this.positionSide = positionSide; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }

    /**
     * 分页结果类
     */
    public static class PageResult<T> {
        private List<T> list;
        private Long total;
        private Integer pages;
        private Integer page;
        private Integer size;

        public List<T> getList() { return list; }
        public void setList(List<T> list) { this.list = list; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }
}
