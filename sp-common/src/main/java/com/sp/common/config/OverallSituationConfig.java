package com.sp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局配置類
 *
 */
@Component
@ConfigurationProperties(prefix = "sp")
public class OverallSituationConfig
{
    /** 項目名稱 */
    private static String name;

    /** 版本 */
    private static String version;

    /** 版權年份 */
    private static String copyrightYear;

    /** 實例演示開關 */
    private static boolean demoEnabled;

    /** 上傳路徑 */
    private static String profile;

    /** 獲取地址開關 */
    private static boolean addressEnabled;

    public static String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        OverallSituationConfig.name = name;
    }

    public static String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        OverallSituationConfig.version = version;
    }

    public static String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        OverallSituationConfig.copyrightYear = copyrightYear;
    }

    public static boolean isDemoEnabled()
    {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled)
    {
        OverallSituationConfig.demoEnabled = demoEnabled;
    }

    public static String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        OverallSituationConfig.profile = profile;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        OverallSituationConfig.addressEnabled = addressEnabled;
    }

    /**
     * 獲取導入上傳路徑
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 獲取頭像上傳路徑
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 獲取下載路徑
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * 獲取上傳路徑
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }
}
