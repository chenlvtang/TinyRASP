package com.rasp.utils;

import com.alibaba.fastjson2.JSONObject;
import com.rasp.vulHook.RceHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
    private static final ThreadLocal<HttpServletRequest> requestContext = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> responseContext = new ThreadLocal<>();

    public static void setRequest(HttpServletRequest request) {
        requestContext.set(request);
    }

    public static void setResponse(HttpServletResponse response) {
        responseContext.set(response);
    }

    public static void clear() {
        requestContext.remove();
        responseContext.remove();
        System.out.println("上下文已经清除");
    }

    public static HttpServletRequest getRequest() {
        return requestContext.get();
    }

    public static HttpServletResponse getResponse() {
        return responseContext.get();
    }

    public static void alert(String message) {
        // 实现重定向到告警页面
        try {
            HttpServletResponse response = getResponse();
            HttpServletRequest request = getRequest();
            // 检测本地是否配置告警页面
            String alertSite = String.valueOf(request.getRequestURL());
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(
                    alertSite + "/alert.html"
            ).openConnection();
            con.setRequestMethod("GET");
            // 如果本地没有配置告警页面，就使用云端的
            if(con.getResponseCode() != HttpURLConnection.HTTP_OK){
                alertSite = "https://chenlvtang.top";
            }
            // 发送告警信息
            response.sendRedirect(alertSite + "/alert.html?message=" +  message);
            // 清除上下文
            clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(message);
    }

    public static String getUA(){
        //获取攻击者UA
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getHeader("User-Agent");
        }
        return null;
    }

    public static String getIP(){
        //获取攻击者IP
        HttpServletRequest request =  getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String[] ips = ip.split(",");
        return ips[0].trim();
    }

    public static Map<String, Object> getRequestInfo()
            throws IOException, IOException {        // 返回请求报文
        HttpServletRequest request = getRequest();
        Map<String, Object> requestMap = new HashMap<>();
        // 增加一个URI的记录，来用于kibana筛选被攻击最多的页面/路由
        String requestURI = request.getRequestURI();
        requestMap.put("uri", requestURI);
        // 记录请求URI（要包含请求参数） 如/rce?cmd=ls
        String url = "";
        StringBuilder requestURL = new StringBuilder(requestURI);
        String queryString = request.getQueryString();

        if (queryString == null) {
            url = requestURL.toString();
        } else {
            url = requestURL.append('?').append(queryString).toString();
        }
        requestMap.put("url", url);

        // 记录请求方法
        requestMap.put("method", request.getMethod());

        // 记录请求头
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersMap.put(headerName, headerValue);
        }
        requestMap.put("headers", headersMap);

        // 记录请求体
        ServletInputStream inputStream = request.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        requestMap.put("body", requestBody.toString());

        return requestMap;
    }

    public static String getStackTrace() {
        // 获取堆栈信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Throwable().printStackTrace(pw);
        return sw.toString();
    }

    public static void getLog(String type) throws IOException {
        // 告警日志记录
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
}
