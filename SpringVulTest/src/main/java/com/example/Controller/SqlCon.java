package com.example.Controller;

import com.example.Model.User;
import com.example.service.SqlServ;
import com.example.service.SqlServImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SqlCon {
    @RequestMapping(value = "sqlTest", method = RequestMethod.GET)
    public String getSqlTest(){
        return "sqlTest";
    }

    @PostMapping("/sqlTest")
    public String getUserById(@RequestParam String id, Model model) {
        SqlServ sqlServ = new SqlServImpl();
        User user = sqlServ.findUserById(id);
        model.addAttribute("user", user);
        return "sqlTest";
    }
}
