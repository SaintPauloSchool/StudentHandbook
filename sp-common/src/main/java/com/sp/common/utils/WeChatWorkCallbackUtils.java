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
 * 企業微信回調工具類
 * 用於處理企業微信回調相關的籤名驗證和消息解密
 *
 * <p>籤名算法（GET/POST 通用）：將 token、timestamp、nonce、第四個參數（echostr 或 encrypt）
 * 四個字段按字典序排序後拼接，做 SHA-1 哈希，結果與 msg_signature 比對。</p>
 */
public class WeChatWorkCallbackUtils {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkCallbackUtils.class);

    private WeChatWorkCallbackUtils() {
        // 工具類，禁止實例化
    }

    // -------------------------------------------------------------------------
    // 籤名驗證
    // -------------------------------------------------------------------------

    /**
     * 驗證消息推送籤名（POST 場景）
     * 參與籤名：token + timestamp + nonce + encrypt
     *
     * @param token     回調 Token
     * @param timestamp 時間戳
     * @param nonce     隨機數
     * @param encrypt   XML 中的 Encrypt 字段內容
     * @param signature 企業微信傳遞的 msg_signature
     * @return true 驗證通過
     */
    public static boolean verifySignature(String token, String timestamp, String nonce,
                                          String encrypt, String signature) {
        return sha1Verify(signature, token, timestamp, nonce, encrypt);
    }

    /**
     * 驗證 URL 有效性籤名（GET 場景）
     * 參與籤名：token + timestamp + nonce + echostr
     *
     * @param token     回調 Token
     * @param timestamp 時間戳
     * @param nonce     隨機數
     * @param echostr   加密的隨機字符串
     * @param signature 企業微信傳遞的 msg_signature
     * @return true 驗證通過
     */
    public static boolean verifySignatureWithEchoStr(String token, String timestamp, String nonce,
                                                     String echostr, String signature) {
        return sha1Verify(signature, token, timestamp, nonce, echostr);
    }

    /**
     * 通用籤名校驗：將若干字段排序拼接後做 SHA-1，與期望籤名比對
     */
    private static boolean sha1Verify(String expected, String... fields) {
        Arrays.sort(fields);
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            sb.append(f);
        }
        String computed = sha1Hex(sb.toString());
        logger.debug("籤名校驗 — 計算值: {}, 期望值: {}", computed, expected);
        return computed.equalsIgnoreCase(expected);
    }

    // -------------------------------------------------------------------------
    // 消息解密
    // -------------------------------------------------------------------------

    /**
     * 解密 echostr（URL 驗證場景）
     * 企業微信加密格式：AES-CBC，Key=AESKey，IV=AESKey前16字節
     * 明文結構：16字節隨機數 + 4字節消息長度(大端) + 消息內容 + receiveid
     *
     * @param echostr        Base64 編碼的密文
     * @param encodingAesKey 企業微信配置的 EncodingAESKey（43或44字符）
     * @return 解密後的明文字符串
     * @throws Exception 解密失敗
     */
    public static String decryptEchoStr(String echostr, String encodingAesKey) throws Exception {
        byte[] aesKey = decodeAesKey(encodingAesKey);
        byte[] encryptedData = Base64.getDecoder().decode(echostr);

        byte[] decrypted = aesDecrypt(aesKey, encryptedData);

        // 跳過頭部 16 字節隨機數，讀取 4 字節大端消息長度
        ByteBuffer buf = ByteBuffer.wrap(decrypted, 16, decrypted.length - 16);
        int msgLen = buf.getInt();

        byte[] msgBytes = new byte[msgLen];
        buf.get(msgBytes);
        return new String(msgBytes, StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // 私有輔助方法
    // -------------------------------------------------------------------------

    /**
     * 處理並解碼 EncodingAESKey 爲 AES 密鑰字節數組
     * 企業微信的 EncodingAESKey 爲 43 位 Base64 字符，需補齊末尾 "=" 再解碼
     */
    private static byte[] decodeAesKey(String encodingAesKey) {
        String key = encodingAesKey.trim();
        if (key.length() == 43) {
            key += "=";
        }
        return Base64.getDecoder().decode(key);
    }

    /**
     * AES-CBC 解密，IV 取 AESKey 前 16 字節
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
     * 計算字符串的 SHA-1 哈希，返回小寫十六進制字符串
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