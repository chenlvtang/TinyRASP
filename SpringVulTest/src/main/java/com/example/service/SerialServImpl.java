package com.example.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

public class SerialServImpl implements SerialServ {
    @Override
    public String getResult(String cmd) {
        String result = "";
        try {
            // 解码base64编码的cmd参数
            byte[] decodedCmd = Base64.getDecoder().decode(cmd);

            // 反序列化cmd参数
            ByteArrayInputStream bais = new ByteArrayInputStream(decodedCmd);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            // 将反序列化后的对象转换为字符串
            result = obj.toString();
            ois.close();
            bais.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
