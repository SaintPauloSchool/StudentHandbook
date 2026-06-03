package com.sp.common.utils.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;

/**
 * 對稱密鑰密碼算法工具類
 *
 */
public class CipherUtils
{
    /**
     * 生成隨機祕鑰
     *
     * @param keyBitSize 字節大小
     * @param algorithmName 算法名稱
     * @return 創建密匙
     */
    public static Key generateNewKey(int keyBitSize, String algorithmName)
    {
        KeyGenerator kg;
        try
        {
            kg = KeyGenerator.getInstance(algorithmName);
        }
        catch (NoSuchAlgorithmException e)
        {
            String msg = "Unable to acquire " + algorithmName + " algorithm.  This is required to function.";
            throw new IllegalStateException(msg, e);
        }
        kg.init(keyBitSize);
        return kg.generateKey();
    }
}
