package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.utils.WeChatWorkCallbackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 企業微信回調控制器
 * 用於處理企業微信的回調配置URL驗證和消息接收
 */
@RestController
@RequestMapping("/wechat/callback")
public class WeChatWorkCallbackController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkCallbackController.class);

    @Value("${wechat.work.callback.token}")
    private String token;

    @Value("${wechat.work.callback.encodingAesKey}")
    private String encodingAesKey;

    /**
     * 驗證URL有效性
     * 企業微信在配置回調URL時會發送GET請求進行驗證
     *
     * @param msgSignature 微信加密籤名
     * @param timestamp    時間戳
     * @param nonce        隨機數
     * @param echostr      加密的隨機字符串
     * @return 解密後的echostr
     */
    @Anonymous
    @GetMapping
    public String verifyURL(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        logger.info("接收到URL驗證請求: msg_signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        try {
            // URL傳輸時"+"會被轉成空格，還原後才能正確做Base64解碼
            String processedEchostr = echostr.replace(" ", "+");

            if (!WeChatWorkCallbackUtils.verifySignatureWithEchoStr(token, timestamp, nonce, processedEchostr, msgSignature)) {
                logger.warn("URL驗證籤名失敗");
                return "";
            }

            String result = WeChatWorkCallbackUtils.decryptEchoStr(processedEchostr, encodingAesKey);
            logger.info("URL驗證成功");
            return result;
        } catch (Exception e) {
            logger.error("URL驗證過程中出現異常", e);
            return "";
        }
    }

    /**
     * 接收企業微信推送的消息
     *
     * @param msgSignature 微信加密籤名
     * @param timestamp    時間戳
     * @param nonce        隨機數
     * @param postData     推送的XML數據
     * @return 響應結果
     */
    @Anonymous
    @PostMapping
    public String receiveMessage(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String postData) {

        logger.info("接收到企業微信推送消息: msg_signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        try {
            // 從XML body中解析<Encrypt>字段，企業微信籤名規範：token+timestamp+nonce+encrypt 四者排序後SHA1
            String encrypt = extractEncrypt(postData);
            if (encrypt == null || encrypt.isEmpty()) {
                logger.warn("無法從請求體中解析 Encrypt 字段");
                return "fail";
            }

            if (!WeChatWorkCallbackUtils.verifySignature(token, timestamp, nonce, encrypt, msgSignature)) {
                logger.warn("消息推送籤名驗證失敗");
                return "fail";
            }

            // TODO: 解析XML並處理不同類型的業務消息
            logger.info("消息處理成功");
            return "success";
        } catch (Exception e) {
            logger.error("處理推送消息時出現異常", e);
            return "fail";
        }
    }

    /**
     * 從XML字符串中提取 &lt;Encrypt&gt; 標籤的CDATA內容
     */
    private String extractEncrypt(String xml) {
        if (xml == null) return null;
        int encryptTag = xml.indexOf("<Encrypt>");
        if (encryptTag < 0) return null;
        int start = xml.indexOf("<![CDATA[", encryptTag);
        int end   = xml.indexOf("]]>", start);
        if (start < 0 || end < 0) return null;
        return xml.substring(start + 9, end).trim();
    }
}