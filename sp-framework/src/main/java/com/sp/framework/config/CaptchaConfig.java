package com.sp.framework.config;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import static com.google.code.kaptcha.Constants.*;

/**
 * 驗證碼配置
 *
 */
@Configuration
public class CaptchaConfig
{
    @Bean(name = "captchaProducer")
    public DefaultKaptcha getKaptchaBean()
    {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有邊框 默認爲true 我們可以自己設置yes，no
        properties.setProperty(KAPTCHA_BORDER, "yes");
        // 驗證碼文本字符顏色 默認爲Color.BLACK
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "black");
        // 驗證碼圖片寬度 默認爲200
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        // 驗證碼圖片高度 默認爲50
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 驗證碼文本字符大小 默認爲40
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "38");
        // KAPTCHA_SESSION_KEY
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCode");
        // 驗證碼文本字符長度 默認爲5
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        // 驗證碼文本字體樣式 默認爲new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        // 圖片樣式 水紋com.google.code.kaptcha.impl.WaterRipple 魚眼com.google.code.kaptcha.impl.FishEyeGimpy 陰影com.google.code.kaptcha.impl.ShadowGimpy
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    @Bean(name = "captchaProducerMath")
    public DefaultKaptcha getKaptchaBeanMath()
    {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有邊框 默認爲true 我們可以自己設置yes，no
        properties.setProperty(KAPTCHA_BORDER, "yes");
        // 邊框顏色 默認爲Color.BLACK
        properties.setProperty(KAPTCHA_BORDER_COLOR, "105,179,90");
        // 驗證碼文本字符顏色 默認爲Color.BLACK
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue");
        // 驗證碼圖片寬度 默認爲200
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        // 驗證碼圖片高度 默認爲50
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 驗證碼文本字符大小 默認爲40
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "35");
        // KAPTCHA_SESSION_KEY
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCodeMath");
        // 驗證碼文本生成器
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL, "com.sp.framework.config.KaptchaTextCreator");
        // 驗證碼文本字符間距 默認爲2
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "3");
        // 驗證碼文本字符長度 默認爲5
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "6");
        // 驗證碼文本字體樣式 默認爲new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        // 驗證碼噪點顏色 默認爲Color.BLACK
        properties.setProperty(KAPTCHA_NOISE_COLOR, "white");
        // 幹擾實現類
        properties.setProperty(KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        // 圖片樣式 水紋com.google.code.kaptcha.impl.WaterRipple 魚眼com.google.code.kaptcha.impl.FishEyeGimpy 陰影com.google.code.kaptcha.impl.ShadowGimpy
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
