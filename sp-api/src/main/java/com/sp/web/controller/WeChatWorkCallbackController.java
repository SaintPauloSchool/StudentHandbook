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

    // 回调配置参数
    @Value("${wechat.work.callback.token}")
    private String token;

    @Value("${wechat.work.callback.encodingAesKey}")
    private String encodingAesKey;

    @Value("${wechat.work.callback.corpId}")
    private String corpId;

    //永久授权码（用于第三方应用）
    @Value("${wechat.work.callback.permanentCode}")
    private String permanentCode;

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

        logRequest("URL验证请求", msgSignature, timestamp, nonce, echostr);

        try {
            // 先对echostr进行预处理，将空格转换回加号
            String processedEchostr = echostr.replace(" ", "+");

            // 使用处理后的参数进行签名验证
            if (!verifySignature(processedEchostr, msgSignature, timestamp, nonce, true)) {
                logger.warn("签名验证失败");
                return "";
            }

            // 直接使用处理后的echostr进行解密，不再进行额外的URL解码
            // 因为URL解码会把"+"转换为空格，破坏Base64格式
            String result = WeChatWorkCallbackUtils.decryptEchoStr(processedEchostr, encodingAesKey);

            // 在1秒内响应GET请求，响应内容为上一步得到的明文消息内容
            logger.info("URL验证成功，返回结果: {}", result);
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
     * @param postData     推送的数据
     * @return 响应结果
     */
    @Anonymous
    @PostMapping
    public String receiveMessage(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String postData) {

        logRequest("推送消息", msgSignature, timestamp, nonce, postData);

        try {
            // 验证签名
            if (!verifySignature(null, msgSignature, timestamp, nonce, false)) {
                logger.warn("签名验证失败");
                return "fail";
            }

            // 解析XML数据
            // 注意：实际业务中需要解析XML并处理不同类型的消息

            logger.info("消息处理成功");
            return "success";
        } catch (Exception e) {
            logger.error("处理推送消息时出现异常", e);
            return "fail";
        }
    }
    
    /**
     * 记录请求日志
     * 
     * @param requestType 请求类型
     * @param msgSignature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param data 请求数据
     */
    private void logRequest(String requestType, String msgSignature, String timestamp, String nonce, String data) {
        logger.info("接收到企业微信{}: msg_signature={}, timestamp={}, nonce={}, data={}", 
                requestType, msgSignature, timestamp, nonce, data);
        logger.info("本地配置: token={}, encodingAesKey={}", token, encodingAesKey);
    }
    
    /**
     * 验证签名
     * 
     * @param echostr 验证字符串（URL验证时使用）
     * @param msgSignature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param isVerifyUrl 是否为URL验证
     * @return 验证结果
     */
    private boolean verifySignature(String echostr, String msgSignature, String timestamp, String nonce, boolean isVerifyUrl) {
        if (isVerifyUrl) {
            return WeChatWorkCallbackUtils.verifySignatureWithEchoStr(token, timestamp, nonce, echostr, msgSignature);
        } else {
            return WeChatWorkCallbackUtils.verifySignature(token, timestamp, nonce, msgSignature);
        }
    }
}