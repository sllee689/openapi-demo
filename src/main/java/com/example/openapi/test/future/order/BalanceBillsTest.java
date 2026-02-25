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
 * 合约资金账单查询测试类
 */
public class BalanceBillsTest {

    private static final Logger log = LoggerFactory.getLogger(BalanceBillsTest.class);
    private static final String[] REQUIRED_DATA_FIELDS = {"hasPrev", "hasNext", "items"};
    private static final String[] REQUIRED_ITEM_FIELDS = {
            "id", "coin", "balanceType", "symbol", "positionId",
            "type", "amount", "side", "afterAmount", "createdTime"
    };
    private static ApiClient apiClient;

    /**
     * 查询资金账单
     *
     * @param request 查询请求参数
     * @return 资金账单分页结果
     * @throws HashExApiException 如果API调用失败
     */
    public CursorPageResult<BalanceBillVO> getBalanceBills(BalanceBillsRequest request) throws HashExApiException {
        try {
            // 创建参数Map
            TreeMap<String, String> queryParams = new TreeMap<>();

            // 添加可选参数
            if (request.getCoin() != null) {
                queryParams.put("coin", request.getCoin());
            }
            if (request.getSymbol() != null) {
                queryParams.put("symbol", request.getSymbol());
            }
            if (request.getBalanceType() != null) {
                queryParams.put("balanceType", request.getBalanceType());
            }
            if (request.getType() != null) {
                queryParams.put("type", request.getType());
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
            String responseJson = apiClient.sendGetRequest("/fut/v1/balance/bills", queryParams, true);

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(responseJson);
            if (jsonObject.getInt("code") != 0) {
                throw new HashExApiException("查询资金账单失败: " + jsonObject.getStr("msg"));
            }

            JSONObject dataObj = jsonObject.getJSONObject("data");
            validateDataFields(dataObj);
            CursorPageResult<BalanceBillVO> pageResult = new CursorPageResult<>();
            pageResult.setHasPrev(dataObj.getBool("hasPrev"));
            pageResult.setHasNext(dataObj.getBool("hasNext"));

            // 解析账单列表
            JSONArray itemsArray = dataObj.getJSONArray("items");
            List<BalanceBillVO> billList = new ArrayList<>();

            if (itemsArray != null && !itemsArray.isEmpty()) {
                for (int i = 0; i < itemsArray.size(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    validateItemFields(item);
                    BalanceBillVO bill = new BalanceBillVO();
                    bill.setId(item.getStr("id"));
                    bill.setCoin(item.getStr("coin"));
                    bill.setBalanceType(item.getStr("balanceType"));
                    bill.setSymbol(item.getStr("symbol"));
                    bill.setPositionId(item.getLong("positionId"));
                    bill.setType(item.getStr("type"));
                    bill.setAmount(item.getStr("amount"));
                    bill.setSide(item.getStr("side"));
                    bill.setAfterAmount(item.getStr("afterAmount"));
                    bill.setCreatedTime(item.getLong("createdTime"));
                    billList.add(bill);
                }
            }

            pageResult.setList(billList);
            return pageResult;

        } catch (Exception e) {
            if (e instanceof HashExApiException) {
                throw (HashExApiException) e;
            }
            throw new HashExApiException("查询资金账单时出错: " + e.getMessage(), e);
        }
    }

    private void validateDataFields(JSONObject dataObj) throws HashExApiException {
        if (dataObj == null) {
            throw new HashExApiException("资金账单响应data为空");
        }
        for (String field : REQUIRED_DATA_FIELDS) {
            if (!dataObj.containsKey(field)) {
                throw new HashExApiException("资金账单响应缺少字段: " + field);
            }
        }
    }

    private void validateItemFields(JSONObject item) throws HashExApiException {
        for (String field : REQUIRED_ITEM_FIELDS) {
            if (!item.containsKey(field)) {
                throw new HashExApiException("资金账单item缺少字段: " + field + ", item=" + item);
            }
        }
    }

    /**
     * 测试查询所有资金账单
     */
    private void testQueryAllBills() throws HashExApiException {
        log.info("===== 测试查询所有资金账单 =====");

        BalanceBillsRequest request = new BalanceBillsRequest();
        request.setPage(1);
        request.setSize(10);

        CursorPageResult<BalanceBillVO> result = getBalanceBills(request);
        log.info("查询结果 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<BalanceBillVO> bills = result.getList();
        if (bills != null && !bills.isEmpty()) {
            log.info("资金账单列表 ({} 条记录):", bills.size());
            int index = 1;
            for (BalanceBillVO bill : bills) {
                log.info("账单 #{}: {}", index++, bill);
            }
        } else {
            log.info("没有资金账单记录");
        }
    }

    /**
     * 测试按币种查询资金账单
     */
    private void testQueryByCoin() throws HashExApiException {
        log.info("===== 按币种查询资金账单 =====");

        BalanceBillsRequest request = new BalanceBillsRequest();
        request.setCoin("USDT");
        request.setPage(1);
        request.setSize(10);

        CursorPageResult<BalanceBillVO> result = getBalanceBills(request);
        log.info("USDT账单 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<BalanceBillVO> bills = result.getList();
        if (bills != null && !bills.isEmpty()) {
            for (BalanceBillVO bill : bills) {
                log.info("{}", bill);
            }
        } else {
            log.info("没有USDT的资金账单");
        }
    }

    /**
     * 测试按时间范围查询资金账单
     */
    private void testQueryByTimeRange() throws HashExApiException {
        log.info("===== 按时间范围查询资金账单 =====");

        BalanceBillsRequest request = new BalanceBillsRequest();

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 7 * 24 * 60 * 60 * 1000L; // 7天前
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPage(1);
        request.setSize(10);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("查询时间范围: {} 至 {}",
                sdf.format(new Date(startTime)), sdf.format(new Date(endTime)));

        CursorPageResult<BalanceBillVO> result = getBalanceBills(request);
        log.info("查询结果 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<BalanceBillVO> bills = result.getList();
        if (bills != null && !bills.isEmpty()) {
            for (BalanceBillVO bill : bills) {
                log.info("{}", bill);
            }
        } else {
            log.info("该时间范围内没有资金账单");
        }
    }

    /**
     * 测试按业务类型查询资金账单
     */
    private void testQueryByType() throws HashExApiException {
        log.info("===== 按业务类型查询资金账单 =====");

        BalanceBillsRequest request = new BalanceBillsRequest();
        request.setType("FUND");
        request.setPage(1);
        request.setSize(10);

        CursorPageResult<BalanceBillVO> result = getBalanceBills(request);
        log.info("FUND类型账单 - hasPrev: {}, hasNext: {}", result.getHasPrev(), result.getHasNext());

        List<BalanceBillVO> bills = result.getList();
        if (bills != null && !bills.isEmpty()) {
            for (BalanceBillVO bill : bills) {
                log.info("{}", bill);
            }
        } else {
            log.info("没有FUND类型的资金账单");
        }
    }

    private void testQueryByBalanceTypeVariants() throws HashExApiException {
        log.info("===== 测试 balanceType 参数枚举 =====");
        String[] balanceTypes = {"CONTRACT", "COPY"};
        for (String balanceType : balanceTypes) {
            BalanceBillsRequest request = new BalanceBillsRequest();
            request.setBalanceType(balanceType);
            request.setPage(1);
            request.setSize(10);
            try {
                CursorPageResult<BalanceBillVO> result = getBalanceBills(request);
                log.info("balanceType={} 查询成功, hasPrev={}, hasNext={}",
                        balanceType, result.getHasPrev(), result.getHasNext());
            } catch (HashExApiException e) {
                log.warn("balanceType={} 查询失败(疑似不支持): {}", balanceType, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws HashExApiException {
        apiClient = new ApiClient(
                FutureTestConfig.BASE_URL,
                FutureTestConfig.ACCESS_KEY,
                FutureTestConfig.SECRET_KEY);
        BalanceBillsTest test = new BalanceBillsTest();

        // 执行测试
        test.testQueryAllBills();
        test.testQueryByCoin();
        test.testQueryByTimeRange();
        test.testQueryByType();
        test.testQueryByBalanceTypeVariants();
    }

    /**
     * 资金账单VO
     */
    public static class BalanceBillVO {
        private String id;              // 账单ID
        private String coin;            // 币种
        private String balanceType;     // 资金类型：CONTRACT(合约)、COPY(跟单)
        private String symbol;          // 交易对
        private Long positionId;        // 持仓ID
        private String type;            // 业务类型
        private String amount;          // 变动金额
        private String side;            // 方向
        private String afterAmount;     // 变动后余额
        private Long createdTime;       // 创建时间

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCoin() { return coin; }
        public void setCoin(String coin) { this.coin = coin; }
        public String getBalanceType() { return balanceType; }
        public void setBalanceType(String balanceType) { this.balanceType = balanceType; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public Long getPositionId() { return positionId; }
        public void setPositionId(Long positionId) { this.positionId = positionId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getSide() { return side; }
        public void setSide(String side) { this.side = side; }
        public String getAfterAmount() { return afterAmount; }
        public void setAfterAmount(String afterAmount) { this.afterAmount = afterAmount; }
        public Long getCreatedTime() { return createdTime; }
        public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("账单 [%s] { 币种: %s, 资金类型: %s, 业务类型: %s, 变动金额: %s, 变动后余额: %s, 交易对: %s, 方向: %s, 时间: %s }",
                    id,
                    coin,
                    balanceType,
                    type,
                    amount,
                    afterAmount,
                    symbol == null ? "无" : symbol,
                    side == null ? "无" : side,
                    createdTime == null ? "未知" : sdf.format(new Date(createdTime)));
        }
    }

    /**
     * 资金账单查询请求参数类
     */
    public static class BalanceBillsRequest {
        private String coin;            // 币种
        private String symbol;          // 交易对
        private String balanceType;     // 账户类型：CONTRACT(合约)、COPY(跟单)
        private String type;            // 业务类型（示例：FUND，以服务端实际枚举为准）
        private Long startTime;         // 开始时间
        private Long endTime;           // 结束时间
        private Integer page;           // 页码
        private Integer size;           // 每页大小

        public String getCoin() { return coin; }
        public void setCoin(String coin) { this.coin = coin; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getBalanceType() { return balanceType; }
        public void setBalanceType(String balanceType) { this.balanceType = balanceType; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
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
