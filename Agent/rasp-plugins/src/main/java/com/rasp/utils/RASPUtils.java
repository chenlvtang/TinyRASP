package com.rasp.utils;

import com.alibaba.fastjson2.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletInputStream;
import java.io.*;
import java.lang.reflect.Method;
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
//        String site = alertSite;
//        try {
//            HttpURLConnection.setFollowRedirects(false);
//            HttpURLConnection con = (HttpURLConnection) new URL(
//                    alertSite + "/alert.html"
//            ).openConnection();
//
//            /**
//             * ======== 这一部分为了提高性能
//             */
//            // 设置连接超时时间为5秒
//            con.setConnectTimeout(5000);
//            // 设置读取超时时间为5秒
//            con.setReadTimeout(5000);
//            /**
//             * ===============
//             */
//            con.setRequestMethod("GET");
//            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                throw new RuntimeException("本地没有告警页面");
//            }
//        }
//        catch (Exception e){
//            site = "https://chenlvtang.top";
//        }
//        return site;
        return "https://chenlvtang.top";
    }

    /**
     * 实现重定向到告警页面
     * @param message
     */
    public static void alert(String message) {
        String alertSite = "";

        // 发送告警信息
        try{
            alertSite = whichSite(
                    String.valueOf(
                            getRequest().getClass().
                            getMethod("getRequestURL").
                            invoke(getRequest()))
            );
            setRedirect(alertSite + "/alert.html?message=" + message);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        // 清除上下文
        clear();
    }

    public static String getUA() {
        String ua = "";

        try {
            ua = (String) getRequest().getClass()
                    .getMethod("getHeader", String.class)
                    .invoke(getRequest(), "User-Agent");
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        return ua;
    }

    public static String getIP(){
        String ip = "";
        try{
            ip = (String) getRequest().getClass().getMethod("getHeader", String.class)
                    .invoke(getRequest(),"X-Forwarded-For");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip =  (String)getRequest().getClass().getMethod("getHeader", String.class)
                        .invoke(getRequest(),"Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip =  (String)getRequest().getClass().getMethod("getHeader", String.class)
                        .invoke(getRequest(),"WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip =  (String)getRequest().getClass().getMethod("getHeader", String.class)
                        .invoke(getRequest(),"HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip =  (String)getRequest().getClass().getMethod("getHeader", String.class)
                        .invoke(getRequest(),"HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip =  (String)getRequest().getClass().getMethod("getRemoteAddr")
                        .invoke(getRequest());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        String[] ips = ip.split(",");
        return ips[0].trim();
    }

    public static Map<String, Object> getRequestInfo()
            throws Exception {        // 返回请求报文
        Map<String, Object> requestMap = new HashMap<>();
        try {
            // 增加一个URI的记录，来用于kibana筛选被攻击最多的页面/路由
            String requestURI = (String) getRequest().getClass().getMethod("getRequestURI")
                    .invoke(getRequest());
            requestMap.put("uri", requestURI);

            // 记录请求URL（要包含请求参数） 如/rce?cmd=ls
            String url = "";
            StringBuilder requestURL = new StringBuilder(requestURI);
            String queryString = (String) getRequest().getClass().getMethod("getQueryString")
                    .invoke(getRequest());
            if (queryString == null) {
                url = requestURL.toString();
            } else {
                url = requestURL.append('?').append(queryString).toString();
            }
            requestMap.put("url", url);

            // 记录请求方法
            requestMap.put("method", getRequest().getClass().getMethod("getMethod")
                    .invoke(getRequest()));

            // 记录请求头
            Map<String, String> headersMap = new HashMap<>();
            Enumeration<String> headerNames = (Enumeration<String>) getRequest().getClass().getMethod("getHeaderNames")
                    .invoke(getRequest());
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = (String) getRequest().getClass().getMethod("getHeader", String.class)
                        .invoke(getRequest(), headerName);
                headersMap.put(headerName, headerValue);
            }
            requestMap.put("headers", headersMap);

            // 记录请求体
            InputStream inputStream = (InputStream) getRequest().getClass().getMethod("getInputStream")
                    .invoke(getRequest());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            requestMap.put("body", requestBody);
            // 如果body已经解析过了
            if ((requestBody.toString()).equals("")){
                Map<String, String[]> requestBodyMap = (Map<String, String[]>) getRequest().getClass().getMethod("getParameterMap")
                                .invoke(getRequest());
                JSONObject jsonObject = JSONObject.from(requestBodyMap);
                String requestBodyString = jsonObject.toString();
                requestMap.put("body", requestBodyString);
            }
        }catch (Exception e){
           throw new RuntimeException(e);
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
        alertInfo = URLEncoder.encode(log.toJSONString(), String.valueOf(StandardCharsets.UTF_8));
        log.put("request_info", requestInfo);
        log.put("stackTrace", stackTrace);
        // 输出到ELK框架的详细信息，包括请求报文和堆栈
        logger.warn(log.toJSONString());
    }

    public static void getLogAndAlert(String attackType) throws Exception {
        try {
            getLog(attackType);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

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
