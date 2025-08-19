package com.yonyou.util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 使用例子：https://www.jianshu.com/p/a221027276c7
 *
 * @author Simon
 */
public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static final CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(50);

        RequestConfig globalConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(20000)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
        httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(globalConfig).build();
    }

    private static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public static String doGet(String url, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpGet get = new HttpGet(url);
        if (headers != null && !headers.isEmpty()) {
            for (final Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
        }
        String responseString = httpClient.execute(get, response -> EntityUtils.toString(response.getEntity()));
        get.releaseConnection();
        return responseString;
    }

    /**
     * 通过post方式请求
     *
     * @param url        请求url
     * @param parameters 参数
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> headers, String parameters, String contentType) {
        String result = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost method = new HttpPost(url);
        if (parameters != null && !parameters.trim().isEmpty()) {
            try {
                // 建立一个NameValuePair数组，用于存储欲传送的参数
                method.addHeader("Content-type", contentType);
                method.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;");
                if (headers != null && !headers.isEmpty()) {
                    for (final Map.Entry<String, String> entry : headers.entrySet()) {
                        method.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                method.setEntity(new StringEntity(parameters, StandardCharsets.UTF_8));
                HttpResponse response = httpclient.execute(method);
                response.addHeader("Content-Type", contentType);
                int statusCode = response.getStatusLine().getStatusCode();
                result = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> headers, String parameters) {
        return doPost(url, headers, parameters, "application/json;charset=UTF-8");
    }

    public static String doPostByForm(String url, Map<String, String> headers, Map<String, Object> formMap) {
        String result = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost method = new HttpPost(url);
        try {
            // 建立一个NameValuePair数组，用于存储欲传送的参数
            method.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            method.setHeader("Accept", "text/plain");
            if (headers != null && !headers.isEmpty()) {
                for (final Map.Entry<String, String> entry : headers.entrySet()) {
                    method.addHeader(entry.getKey(), entry.getValue());
                }
            }
            List<NameValuePair> parameters = new ArrayList<>();
            for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                if (entry.getValue() != null) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                } else {
                    parameters.add(new BasicNameValuePair(entry.getKey(), ""));
                }

            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
            method.setEntity(formEntity);
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String doPostByFile(String url, Map<String, String> headers, String parameters) {
        String result = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost method = new HttpPost(url);
        if (parameters != null && !"".equals(parameters.trim())) {
            try {
                if (headers != null && !headers.isEmpty()) {
                    for (final Map.Entry<String, String> entry : headers.entrySet()) {
                        method.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                method.setEntity(new StringEntity(parameters, StandardCharsets.UTF_8));
                HttpResponse response = httpclient.execute(method);
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                int statusCode = response.getStatusLine().getStatusCode();
                result = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 绕过验证
     */
    public static SSLContext createIgnoreVerifySSL() throws Exception {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return sslContext;
    }

    /**
     * 通过post方式请求
     *
     * @param url        请求url
     * @param parameters 参数
     * @return
     * @throws IOException
     */
    public static String doPostNoSSL(String url, Map<String, String> headers, String parameters) {
        String result = "";

        SSLContext sslContext = null;
        try {
            sslContext = createIgnoreVerifySSL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LayeredConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        //设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", csf)
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier()) // 忽略主机名验证
                .setConnectionManager(connManager).build();
        HttpPost method = new HttpPost(url);
        if (parameters != null && !"".equals(parameters.trim())) {
            try {
                // 建立一个NameValuePair数组，用于存储欲传送的参数
                method.addHeader("Content-type", "application/json; charset=utf-8");
                method.setHeader("Accept", "application/json");
                if (headers != null && !headers.isEmpty()) {
                    for (final Map.Entry<String, String> entry : headers.entrySet()) {
                        method.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                method.setEntity(new StringEntity(parameters, StandardCharsets.UTF_8));
                HttpResponse response = httpclient.execute(method);
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                int statusCode = response.getStatusLine().getStatusCode();
                result = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 通过post方式请求
     *
     * @param url 请求url
     * @return
     * @throws IOException
     */
    public static String doPostNoParam(String url, Map<String, String> headers) {
        String result = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost method = new HttpPost(url);
        try {
            // 建立一个NameValuePair数组，用于存储欲传送的参数
            method.addHeader("Content-type", "application/json; charset=utf-8");
            method.setHeader("Accept", "application/json");
            if (headers != null && !headers.isEmpty()) {
                for (final Map.Entry<String, String> entry : headers.entrySet()) {
                    method.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpResponse response = httpclient.execute(method);
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            int statusCode = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 可用版本
     *
     * @param url
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doGetHeads(String url, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        if (url.indexOf("?") == -1) {
            url = url + "?";
        }
        if (headers != null && !headers.isEmpty()) {
            for (final Map.Entry<String, String> entry : headers.entrySet()) {
                url = url + "&" + entry.getKey() + "=" + entry.getValue();
            }
        }
        if (url.indexOf("?&") != -1) {
            url = url.replace("?&", "?");
        }
        HttpGet get = new HttpGet(url);
        String responseString = httpClient.execute(get, response -> EntityUtils.toString(response.getEntity()));
        get.releaseConnection();
        return responseString;
    }

    public static String toGetRequestUrl(String baseUrl, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder sb = new StringBuilder(baseUrl);
        if (baseUrl.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }

        List<String> pairs = new ArrayList<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String name = null;
            try {
                name = URLEncoder.encode(entry.getKey(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String value = null;
            try {
                value = URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            pairs.add(name + "=" + value);
        }

        sb.append(String.join("&", pairs));
        return sb.toString();
    }

}
