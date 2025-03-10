package com.example.Controller;

import com.example.service.RceServ;
import com.example.service.RceServImpl;
import com.example.service.SerialServ;
import com.example.service.SerialServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SerialCon {
    @RequestMapping(value = "serialTest", method = RequestMethod.GET)
    public String getRceTest(){
        return "serialTest";
    }

    @PostMapping("serialTest")
    public String postFileRead(@RequestParam("serial") String serial, Model model){
        SerialServ serialServ = new SerialServImpl();
        String result = serialServ.getResult(serial);
        model.addAttribute("result", result);
        return  "serialTest";
    }
}
