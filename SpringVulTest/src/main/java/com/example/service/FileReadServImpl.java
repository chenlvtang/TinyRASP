package com.example.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;

public class FileReadServImpl implements FileReadServ{
    @Override
    public String getResult(String fileName) {
        String result = "";
        try {
            File file = new File("/foo/bar/" + fileName);
            FileInputStream in = null;
            in = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            int length = in.read(buffer);
            result = new String(buffer, 0, length);
            in.close();
        } catch (Exception e) {
        }
        return  result;
    }
}
