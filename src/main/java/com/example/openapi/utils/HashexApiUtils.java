package com.example.openapi.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * Hashex API工具类，提供签名、请求头等功能
 */
public class HashexApiUtils {

    /**
     * 生成签名
     *
     * @param secretKey 用户的秘密密钥
     * @param sortedParams 排序后的参数
     * @return 签名字符串
     */
    public static String generateSignature(String secretKey, TreeMap<String, String> sortedParams,
                                            String timestamp) {
        try {
            // 构建原始字符串
            String rawString = sortedParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            //增加一个时间戳参数
            rawString += "&timestamp=" + timestamp;
            // 使用HMAC-SHA256算法生成签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(rawString.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成签名时出错", e);
        }
    }

    /**
     * 生成API请求时间戳
     * @return 当前时间的毫秒时间戳
     */
    public static String generateTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 生成API请求随机数
     * @return UUID字符串
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString();
    }

    /**
     * 为请求添加认证头信息
     *
     * @param httpGet GET请求对象
     * @param accessKey 访问密钥
     * @param secretKey 秘密密钥
     * @param queryParams 请求参数
     */
    public static void addAuthHeaders(HttpUriRequestBase httpGet, String accessKey, String secretKey, TreeMap<String, String> queryParams) {
        String timestamp = generateTimestamp();
        String nonce = generateNonce();
        String signature = generateSignature(secretKey, queryParams, timestamp);

        httpGet.setHeader("X-Access-Key", accessKey);
        httpGet.setHeader("X-Signature", signature);
        httpGet.setHeader("X-Request-Timestamp", timestamp);
        httpGet.setHeader("X-Request-Nonce", nonce);
    }

}