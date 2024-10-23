package com.rasp.utils;

import com.alibaba.fastjson2.JSONObject;
import com.rasp.vulHook.RceHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/*一些工具方法：如设置上下文，清楚上下文已经告警页面的重定向*/
public class RASPUtils {
    public static String alertInfo = null;
    private static final ThreadLocal<Object> requestContext = new ThreadLocal<>();
    private static final ThreadLocal<Object> responseContext = new ThreadLocal<>();

    public static <T> void setRequest(T request) {
        requestContext.set(request);
    }
    public static <T> void setResponse(T response) {
        responseContext.set(response);
    }

    public static void clear() {
        requestContext.remove();
        responseContext.remove();
        System.out.println("上下文已经清除");
    }

    // FQ Oracle. 用泛型来支持Jakarta
    public static <T> T getRequest() {
        return (T) requestContext.get();
    }

    public static <T> T getResponse() {
        return (T) responseContext.get();
    }


    // 确认使用本地还是云端的告警页面
    public static String whichSite(String alertSite){
        String site = alertSite;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(
                    alertSite + "/alert.html"
            ).openConnection();
            con.setRequestMethod("GET");
            // 如果本地没有配置告警页面，就使用云端的
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                site = "https://chenlvtang.top";
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return site;
    }

    public static void alert(String message) {
        String alertSite = "";
        // 实现重定向到告警页面
        try {
            // 获取本地URL地址
            Method getRequestURL = getRequest().getClass().getMethod("getRequestURL");
            alertSite = whichSite(String.valueOf(
                    getRequestURL.invoke(getRequest())
                    )
            );
            // 重定向
            setRedirect(alertSite + "/alert.html?message=" + message);
            // 清除上下文
            clear();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public static String getUA() throws Exception{
        String ua = "";
        //获取攻击者UA
        Method getHeader = getRequest().getClass().getMethod("getHeader", String.class);
        getHeader.invoke(getRequest(), "User-Agent");

        return ua;
    }

    public static <T extends HttpServletRequest> String getIP () throws Exception{
        String ip = "";
        Method getHeader = getRequest().getClass().getMethod("getHeader", String.class);

        ip = (String) getHeader.invoke(getRequest(), "X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = (String) getHeader.invoke(getRequest(), "Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = (String) getHeader.invoke(getRequest(), "WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = (String) getHeader.invoke(getRequest(), "HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = (String) getHeader.invoke(getRequest(), "HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            Method getRemoteAddr = getRequest().getClass().getMethod("getRemoteAddr");
            ip = (String) getRemoteAddr.invoke(getRequest());
        }
        String[] ips = ip.split(",");
        return ips[0].trim();
    }

    public static Map<String, Object> getRequestInfo()
            throws Exception {        // 返回请求报文

            Map<String, Object> requestMap = new HashMap<>();
            // 增加一个URI的记录，来用于kibana筛选被攻击最多的页面/路由
            Method getRequestURI = getRequest().getClass().getMethod("getRequestURI");
            String requestURI = (String) getRequestURI.invoke(getRequest());
            requestMap.put("uri", requestURI);
            // 记录请求URI（要包含请求参数） 如/rce?cmd=ls
            String url = "";
            StringBuilder requestURL = new StringBuilder(requestURI);
            Method getQueryString = getRequest().getClass().getMethod("getQueryString");
            String queryString = (String) getQueryString.invoke(getRequest());

            if (queryString == null || queryString.equals("")) {
                url = requestURL.toString();
            } else {
                url = requestURL.append('?').append(queryString).toString();
            }
            requestMap.put("url", url);

            // 记录请求方法
            Method getMethod = getRequest().getClass().getMethod("getMethod");
            requestMap.put("method", getMethod.invoke(getRequest()));

            // 记录请求头
            Map<String, String> headersMap = new HashMap<>();
            Method getHeaderNames = getRequest().getClass().getMethod("getHeaderNames");
            Enumeration<String> headerNames = (Enumeration<String>) getHeaderNames.invoke(
                    getRequest()
            );
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Method getHeader = getRequest().getClass().getMethod("getHeader", String.class);
                String headerValue = (String) getHeader.invoke(
                  getRequest(),
                  headerName
                );
                headersMap.put(headerName, headerValue);
            }
            requestMap.put("headers", headersMap);

            // 记录请求体
            Method getInputStream = getRequest().getClass().getMethod("getInputStream");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            (InputStream) getInputStream.invoke(
                                    getRequest()
                            ), StandardCharsets.UTF_8));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            requestMap.put("body", requestBody);
            // 如果body已经解析过了
            if ((requestBody.toString()).equals("")){
                Method getParameterMap = getRequest().getClass().getMethod("getParameterMap");
                Map<String, String[]> requestBodyMap = (Map<String, String[]>) getParameterMap.invoke(
                        getRequest()
                );
                JSONObject jsonObject = JSONObject.from(requestBodyMap);
                String requestBodyString = jsonObject.toString();
                requestMap.put("body", requestBodyString);
            }
            return requestMap;

    }

    public static String getStackTrace() {
        // 获取堆栈信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Throwable().printStackTrace(pw);
        return sw.toString();
    }

    public static void getLog(String type) throws Exception {
        // 告警日志记录
        System.setProperty("user.timezone", "GMT+8");
        Logger logger = LogManager.getLogger(RASPUtils.class);
        // 获取请求的IP和User-Agent、报文、堆栈信息
        String ip = RASPUtils.getIP();
        String ua = RASPUtils.getUA();
        Map<String, Object> requestInfo = RASPUtils.getRequestInfo();
        String stackTrace = RASPUtils.getStackTrace();

        // JSON化攻击日志，便于后续logstash操作
        JSONObject log= new JSONObject();
        // 时间的获取和格式的转化
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = sdf.format(date);
        log.put("time", time);
        // 攻击类型、IP等信息
        log.put("attack_type", type);
        log.put("ip", ip);
        log.put("user_agent", ua);
        // 简略告警信息，URL编码解决Tomcat “在请求目标中找到无效字符。有效字符在RFC 7230和RFC 3986中定义”的报错
        alertInfo = URLEncoder.encode(log.toJSONString(),StandardCharsets.UTF_8);
        log.put("request_info", requestInfo);
        log.put("stackTrace", stackTrace);
        // 输出到ELK框架的详细信息，包括请求报文和堆栈
        logger.warn(log.toJSONString());
    }

    public static void getLogAndAlert(String attackType) throws Exception {
        getLog(attackType);
        alert(alertInfo);
    }

    public static void setRedirect(String alertUrl) throws Exception{
        if (getResponse() != null) {
            // 设置重定向地址
            Method setHeaderMethod = getResponse().getClass().getMethod("setHeader", String.class, String.class);
            setHeaderMethod.invoke(getResponse(), "Location", alertUrl);

            // 使用反射调用 setStatus 方法
            Method setStatusMethod = getResponse().getClass().getMethod("setStatus", int.class);
            setStatusMethod.invoke(getResponse(), 302);

            Method getWriterMethod = getResponse().getClass().getMethod("getWriter", new Class[]{});
            if (getWriterMethod == null) {
                getWriterMethod = getResponse().getClass().getMethod("getOutputStream", new Class[]{});
            }

            // 通过反射调用 getWriter 方法
            Object writer = null;
            writer = getWriterMethod.invoke(getResponse());
            Method printMethod = writer.getClass().getMethod("print", new Class[]{String.class});
            printMethod.invoke( writer, "");

            Method flushMethod =  writer.getClass().getMethod("flush", new Class[]{});
            flushMethod.invoke( writer);

            Method closeMethod = writer.getClass().getMethod("close", new Class[]{});
            closeMethod.invoke( writer);
        }
    }
}
