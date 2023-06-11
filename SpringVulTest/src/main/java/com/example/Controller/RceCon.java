package com.example.Controller;

import com.example.service.RceServ;
import com.example.service.RceServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RceCon {
    @RequestMapping(value = "rceTest", method = RequestMethod.GET)
    public String getRceTest(){
        return "rceTest";
    }

    @PostMapping("rceTest")
    public String postFileRead(@RequestParam("cmd") String cmd, Model model){
        RceServ rceServ = new RceServImpl();
        String result = rceServ.getResult(cmd);
        model.addAttribute("result", result);
        return  "rceTest";
    }
}
