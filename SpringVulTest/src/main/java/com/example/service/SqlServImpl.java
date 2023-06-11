package com.example.service;

import com.example.Dao.UserDao;
import com.example.Model.User;
import org.springframework.beans.factory.annotation.Autowired;

public class SqlServImpl implements SqlServ{

    @Override
    public User findUserById(String id) {
        UserDao userDao = new UserDao();
        return userDao.getUserById(id);
    }
}
