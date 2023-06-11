package com.example.Controller;

import com.example.service.SpELServ;
import com.example.service.SpELServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpELCon {
    @RequestMapping(value = "spelTest", method = RequestMethod.GET)
    public String getRceTest(){
        return "spelTest";
    }

    @PostMapping("spelTest")
    public String postFileRead(@RequestParam("expression") String expression, Model model){
        SpELServ spelServ = new SpELServImpl();
        String result = spelServ.getResult(expression);
        model.addAttribute("result", result);
        return  "spelTest";
    }
}
