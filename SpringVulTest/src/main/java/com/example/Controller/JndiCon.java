package com.example.Controller;

import com.example.service.JndiServ;
import com.example.service.JndiServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JndiCon {
    @RequestMapping(value = "jndiTest", method = RequestMethod.GET)
    public String getRceTest(){
        return "jndiTest";
    }

    @PostMapping("jndiTest")
    public String postFileRead(@RequestParam("url") String url, Model model){
        JndiServ jndiServ = new JndiServImpl();
        String result = jndiServ.getResult(url);
        model.addAttribute("result", result);
        return  "jndiTest";
    }
}
