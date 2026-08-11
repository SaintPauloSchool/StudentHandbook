package com.sp.common.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sp.common.constant.Constants;
import com.sp.common.utils.StringUtils;
import org.springframework.http.MediaType;

/**
 * 通用http發送方法
 *
 */
public class HttpUtils
{
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    
    // 設置更短的連接超時時間和讀取超時時間，避免504超時
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 15000;    // 15秒    // 5秒

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url 發送請求的 URL
     * @return 所代表遠程資源的響應結果
     */
    public static String sendGet(String url)
    {
        return sendGet(url, StringUtils.EMPTY);
    }

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url 發送請求的 URL
     * @param param 請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @return 所代表遠程資源的響應結果
     */
    public static String sendGet(String url, String param)
    {
        return sendGet(url, param, Constants.UTF8);
    }

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url 發送請求的 URL
     * @param param 請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @param contentType 編碼類型
     * @return 所代表遠程資源的響應結果
     */
    public static String sendGet(String url, String param, String contentType)
    {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try
        {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            log.info("sendGet - {}", redactSensitiveUrl(urlNameString));
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            // 設置連接和讀取超時時間
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", redactSensitiveBody(result.toString()));
        }
        catch (ConnectException e)
        {
            log.error("調用HttpUtils.sendGet ConnectException, url=" + redactSensitiveUrl(url) + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("調用HttpUtils.sendGet SocketTimeoutException, url=" + redactSensitiveUrl(url) + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("調用HttpUtils.sendGet IOException, url=" + redactSensitiveUrl(url) + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("調用HttpsUtil.sendGet Exception, url=" + redactSensitiveUrl(url) + ",param=" + param, e);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception ex)
            {
                log.error("調用in.close Exception, url=" + redactSensitiveUrl(url) + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    /** 日誌中隱藏 corpsecret / access_token / code 等敏感查詢參數 */
    private static String redactSensitiveUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url
                .replaceAll("(?i)(corpsecret=)[^&]*", "$1***")
                .replaceAll("(?i)(access_token=)[^&]*", "$1***")
                .replaceAll("(?i)(?<=[?&]code=)[^&]*", "***");
    }

    /** 日誌中隱藏響應裡的 access_token */
    private static String redactSensitiveBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        return body.replaceAll("(?i)(\"access_token\"\\s*:\\s*\")[^\"]*", "$1***");
    }

    /**
     * 向指定 URL 發送POST方法的請求
     *
     * @param url 發送請求的 URL
     * @param param 請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @return 所代表遠程資源的響應結果
     */
    public static String sendPost(String url, String param)
    {
        return sendPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    /**
     * 向指定 URL 發送POST方法的請求
     * 
     * @param url 發送請求的 URL
     * @param param 請求參數
     * @param contentType 內容類型
     * @return 所代表遠程資源的響應結果
     */
    public static String sendPost(String url, String param, String contentType)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try
        {
            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", contentType);
            // 設置連接和讀取超時時間
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", result);
        }
        catch (ConnectException e)
        {
            log.error("調用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("調用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("調用HttpUtils.sendPost IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("調用HttpsUtil.sendPost Exception, url=" + url + ",param=" + param, e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                log.error("調用in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    public static String sendSSLPost(String url, String param)
    {
        return sendSSLPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    public static String sendSSLPost(String url, String param, String contentType)
    {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        try
        {
            log.info("sendSSLPost - {}", urlNameString);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", contentType);
            // 設置連接和讀取超時時間
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ret = "";
            while ((ret = br.readLine()) != null)
            {
                if (ret != null && !"".equals(ret.trim()))
                {
                    result.append(new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
            }
            log.info("recv - {}", result);
            conn.disconnect();
            br.close();
        }
        catch (ConnectException e)
        {
            log.error("調用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("調用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("調用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("調用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param, e);
        }
        return result.toString();
    }

    private static class TrustAnyTrustManager implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }
}