package com.example.vultest;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "RceServlet", value = "/rce-servlet")
public class RceServlet extends HttpServlet {

    public void init() {
    }

//    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
////        System.out.println(System.getProperty("java.class.path"));
//        String cmd = request.getParameter("cmd");
//        if(cmd != null){
//            Process p =  Runtime.getRuntime().exec(cmd);
//            try{
//                InputStream ip = p.getInputStream();
//                InputStreamReader ipR = new InputStreamReader(ip);
//                BufferedReader reader = new BufferedReader(ipR);
//                String line = "";
//                while ((line = reader.readLine())!= null){
//                    System.out.println(line);
//                }
//            }catch (Exception e){
//            }
//        }
//    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cmd = request.getParameter("ip");

        // 处理并返回结果
        if(cmd != null){
            try{
                Process p =  Runtime.getRuntime().exec("cmd /c" + "ping " + cmd);
                String result = "";
                InputStream ip = p.getInputStream();
                InputStreamReader ipR = new InputStreamReader(ip);
                BufferedReader reader = new BufferedReader(ipR);
                String line = "";
                while ((line = reader.readLine())!= null){
//                    System.out.println(line);
                    result += line;
                }
                // 将结果返回给前端
//                response.setContentType("text/plain");
//                response.setCharacterEncoding("UTF-8");
//                response.getWriter().write(result);

                request.setAttribute("result", result);
                request.getRequestDispatcher("/rceTest.jsp").forward(request, response);
            }catch (Exception e){
            }
        }
    }
    public void destroy() {
    }
}