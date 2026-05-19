package com.sp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 企业微信回调工具类
 * 用于处理企业微信回调相关的签名验证和消息解密
 *
 * <p>签名算法（GET/POST 通用）：将 token、timestamp、nonce、第四个参数（echostr 或 encrypt）
 * 四个字段按字典序排序后拼接，做 SHA-1 哈希，结果与 msg_signature 比对。</p>
 */
public class WeChatWorkCallbackUtils {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkCallbackUtils.class);

    private WeChatWorkCallbackUtils() {
        // 工具类，禁止实例化
    }

    // -------------------------------------------------------------------------
    // 签名验证
    // -------------------------------------------------------------------------

    /**
     * 验证消息推送签名（POST 场景）
     * 参与签名：token + timestamp + nonce + encrypt
     *
     * @param token     回调 Token
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param encrypt   XML 中的 Encrypt 字段内容
     * @param signature 企业微信传递的 msg_signature
     * @return true 验证通过
     */
    public static boolean verifySignature(String token, String timestamp, String nonce,
                                          String encrypt, String signature) {
        return sha1Verify(signature, token, timestamp, nonce, encrypt);
    }

    /**
     * 验证 URL 有效性签名（GET 场景）
     * 参与签名：token + timestamp + nonce + echostr
     *
     * @param token     回调 Token
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   加密的随机字符串
     * @param signature 企业微信传递的 msg_signature
     * @return true 验证通过
     */
    public static boolean verifySignatureWithEchoStr(String token, String timestamp, String nonce,
                                                     String echostr, String signature) {
        return sha1Verify(signature, token, timestamp, nonce, echostr);
    }

    /**
     * 通用签名校验：将若干字段排序拼接后做 SHA-1，与期望签名比对
     */
    private static boolean sha1Verify(String expected, String... fields) {
        Arrays.sort(fields);
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            sb.append(f);
        }
        String computed = sha1Hex(sb.toString());
        logger.debug("签名校验 — 计算值: {}, 期望值: {}", computed, expected);
        return computed.equalsIgnoreCase(expected);
    }

    // -------------------------------------------------------------------------
    // 消息解密
    // -------------------------------------------------------------------------

    /**
     * 解密 echostr（URL 验证场景）
     * 企业微信加密格式：AES-CBC，Key=AESKey，IV=AESKey前16字节
     * 明文结构：16字节随机数 + 4字节消息长度(大端) + 消息内容 + receiveid
     *
     * @param echostr        Base64 编码的密文
     * @param encodingAesKey 企业微信配置的 EncodingAESKey（43或44字符）
     * @return 解密后的明文字符串
     * @throws Exception 解密失败
     */
    public static String decryptEchoStr(String echostr, String encodingAesKey) throws Exception {
        byte[] aesKey = decodeAesKey(encodingAesKey);
        byte[] encryptedData = Base64.getDecoder().decode(echostr);

        byte[] decrypted = aesDecrypt(aesKey, encryptedData);

        // 跳过头部 16 字节随机数，读取 4 字节大端消息长度
        ByteBuffer buf = ByteBuffer.wrap(decrypted, 16, decrypted.length - 16);
        int msgLen = buf.getInt();

        byte[] msgBytes = new byte[msgLen];
        buf.get(msgBytes);
        return new String(msgBytes, StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // 私有辅助方法
    // -------------------------------------------------------------------------

    /**
     * 处理并解码 EncodingAESKey 为 AES 密钥字节数组
     * 企业微信的 EncodingAESKey 为 43 位 Base64 字符，需补齐末尾 "=" 再解码
     */
    private static byte[] decodeAesKey(String encodingAesKey) {
        String key = encodingAesKey.trim();
        if (key.length() == 43) {
            key += "=";
        }
        return Base64.getDecoder().decode(key);
    }

    /**
     * AES-CBC 解密，IV 取 AESKey 前 16 字节
     */
    private static byte[] aesDecrypt(byte[] aesKey, byte[] encryptedData) throws Exception {
        byte[] iv = Arrays.copyOf(aesKey, 16);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(aesKey, "AES"),
                new IvParameterSpec(iv));
        return cipher.doFinal(encryptedData);
    }

    /**
     * 计算字符串的 SHA-1 哈希，返回小写十六进制字符串
     */
    private static String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-1 算法不可用", e);
        }
    }
}