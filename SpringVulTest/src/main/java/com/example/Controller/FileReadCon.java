package com.example.Controller;

import com.example.service.FileReadServ;
import com.example.service.FileReadServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller

public class FileReadCon {
    @GetMapping(value = "/fileTest")
    public String getFileRead(){
        return "fileTest";
    }
    @PostMapping(value = "/fileTest")
    public String postFileRead(@RequestParam("fileName") String fileName, Model model){
        FileReadServ fileReadServ = new FileReadServImpl();
        String result = fileReadServ.getResult(fileName);
        model.addAttribute("result", result);
        return  "fileTest";
    }
}
