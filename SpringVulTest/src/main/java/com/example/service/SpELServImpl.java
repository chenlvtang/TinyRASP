package com.example.service;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.naming.Context;
import javax.naming.InitialContext;

public class SpELServImpl implements SpELServ{
    @Override
    public String getResult(String expression) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);
            String result = (String) exp.getValue();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "出错了";
    }
}
