package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.utils.WeChatWorkCallbackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 企业微信回调控制器
 * 用于处理企业微信的回调配置URL验证和消息接收
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
     * 验证URL有效性
     * 企业微信在配置回调URL时会发送GET请求进行验证
     *
     * @param msgSignature 微信加密签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param echostr      加密的随机字符串
     * @return 解密后的echostr
     */
    @Anonymous
    @GetMapping
    public String verifyURL(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        logger.info("接收到URL验证请求: msg_signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        try {
            // URL传输时"+"会被转成空格，还原后才能正确做Base64解码
            String processedEchostr = echostr.replace(" ", "+");

            if (!WeChatWorkCallbackUtils.verifySignatureWithEchoStr(token, timestamp, nonce, processedEchostr, msgSignature)) {
                logger.warn("URL验证签名失败");
                return "";
            }

            String result = WeChatWorkCallbackUtils.decryptEchoStr(processedEchostr, encodingAesKey);
            logger.info("URL验证成功");
            return result;
        } catch (Exception e) {
            logger.error("URL验证过程中出现异常", e);
            return "";
        }
    }

    /**
     * 接收企业微信推送的消息
     *
     * @param msgSignature 微信加密签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param postData     推送的XML数据
     * @return 响应结果
     */
    @Anonymous
    @PostMapping
    public String receiveMessage(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String postData) {

        logger.info("接收到企业微信推送消息: msg_signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        try {
            // 从XML body中解析<Encrypt>字段，企业微信签名规范：token+timestamp+nonce+encrypt 四者排序后SHA1
            String encrypt = extractEncrypt(postData);
            if (encrypt == null || encrypt.isEmpty()) {
                logger.warn("无法从请求体中解析 Encrypt 字段");
                return "fail";
            }

            if (!WeChatWorkCallbackUtils.verifySignature(token, timestamp, nonce, encrypt, msgSignature)) {
                logger.warn("消息推送签名验证失败");
                return "fail";
            }

            // TODO: 解析XML并处理不同类型的业务消息
            logger.info("消息处理成功");
            return "success";
        } catch (Exception e) {
            logger.error("处理推送消息时出现异常", e);
            return "fail";
        }
    }

    /**
     * 从XML字符串中提取 &lt;Encrypt&gt; 标签的CDATA内容
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