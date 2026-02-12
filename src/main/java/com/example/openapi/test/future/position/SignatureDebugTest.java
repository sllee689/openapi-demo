package com.example.openapi.test.future.position;

import cn.hutool.crypto.SecureUtil;
import com.example.openapi.test.future.FutureTestConfig;
import com.example.openapi.utils.HashexApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * 签名调试测试：对比客户端签名和网关签名是否一致
 * <p>
 * 用于排查 /fut/v1/position/change-type 的 1005 签名错误
 */
public class SignatureDebugTest {

    private static final Logger log = LoggerFactory.getLogger(SignatureDebugTest.class);

    /**
     * 模拟网关端签名生成逻辑（使用 Hutool SecureUtil）
     */
    public static String gatewayGenerateSignature(String secretKey, TreeMap<String, String> sortedParams, String timestamp) {
        StringBuilder rawString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!first) {
                rawString.append("&");
            }
            rawString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        rawString.append("&timestamp=").append(timestamp);
        return SecureUtil.hmacSha256(secretKey).digestHex(rawString.toString());
    }

    /**
     * 对比签名结果
     */
    private static void compareSignatures(String testName, TreeMap<String, String> params) {
        String secretKey = FutureTestConfig.SECRET_KEY;
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 构建原始签名字符串
        StringBuilder rawString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) rawString.append("&");
            rawString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        rawString.append("&timestamp=").append(timestamp);

        log.info("===== {} =====", testName);
        log.info("参数: {}", params);
        log.info("签名原始字符串: {}", rawString);

        // 客户端签名方式（javax.crypto.Mac）
        String clientSig = HashexApiUtils.generateSignature(secretKey, params, timestamp);
        log.info("客户端签名 (javax.crypto): {}", clientSig);

        // 网关端签名方式（Hutool SecureUtil）
        String gatewaySig = gatewayGenerateSignature(secretKey, new TreeMap<>(params), timestamp);
        log.info("网关端签名 (Hutool):       {}", gatewaySig);

        boolean match = clientSig.equals(gatewaySig);
        log.info("签名一致: {} {}", match ? "✅" : "❌", match ? "" : "<<< 签名不一致！>>>");
        log.info("");
    }

    public static void main(String[] args) {
        log.info("========== 签名对比调试 ==========\n");

        // 1. change-type 参数（失败的请求）
        TreeMap<String, String> changeTypeParams = new TreeMap<>();
        changeTypeParams.put("symbol", "btc_usdt");
        changeTypeParams.put("positionType", "CROSSED");
        changeTypeParams.put("positionModel", "AGGREGATION");
        changeTypeParams.put("contractType", "PERPETUAL");
        changeTypeParams.put("positionSide", "LONG");
        compareSignatures("change-type（失败接口）", changeTypeParams);

        // 2. adjust-leverage 参数（成功的请求）
        TreeMap<String, String> adjustLeverageParams = new TreeMap<>();
        adjustLeverageParams.put("symbol", "btc_usdt");
        adjustLeverageParams.put("positionSide", "LONG");
        adjustLeverageParams.put("leverage", "20");
        compareSignatures("adjust-leverage（成功接口）", adjustLeverageParams);

        // 3. close-all 参数（成功的请求）
        TreeMap<String, String> closeAllParams = new TreeMap<>();
        closeAllParams.put("symbol", "eth_usdt");
        closeAllParams.put("contractType", "PERPETUAL");
        compareSignatures("close-all（成功接口）", closeAllParams);

        // 4. 空参数测试
        TreeMap<String, String> emptyParams = new TreeMap<>();
        compareSignatures("空参数", emptyParams);

        // 5. 测试 Hutool SecureUtil 和 javax.crypto 对中文/特殊字符的处理
        TreeMap<String, String> specialParams = new TreeMap<>();
        specialParams.put("key1", "value_with_underscore");
        specialParams.put("key2", "VALUE_UPPER");
        compareSignatures("特殊字符测试", specialParams);
    }
}
