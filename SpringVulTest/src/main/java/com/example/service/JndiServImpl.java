package com.example.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JndiServImpl implements JndiServ{
    @Override
    public String getResult(String url) {
        try {
            Context ctx = new InitialContext();
            ctx.lookup(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "查询成功";
    }
}
