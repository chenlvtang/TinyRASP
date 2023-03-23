package com.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*一些工具方法：如设置上下文，清楚上下文已经告警页面的重定向*/
public class RASPUtils {
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
    }

    public static HttpServletRequest getRequest() {
        return requestContext.get();
    }

    public static HttpServletResponse getResponse() {
        return responseContext.get();
    }

    public static void alert(String message) {
        try {
            HttpServletResponse response = getResponse();
            HttpServletRequest request = getRequest();
            response.sendRedirect(request.getContextPath() + "/alert.jsp?message=" + message);
//            response.sendRedirect("https://chenlvtang.top");
            clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(message);
    }
}
