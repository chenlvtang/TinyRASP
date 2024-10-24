package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RceServImpl implements RceServ {

    @Override
    public String getResult(String cmd) throws IOException {
        String result = "";
        if (cmd != null) {

                Process p = Runtime.getRuntime().exec("cmd /c" + "ping " + cmd);

                InputStream ip = p.getInputStream();
                InputStreamReader ipR = new InputStreamReader(ip, "GBK");
                BufferedReader reader = new BufferedReader(ipR);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
        }
        return result;
    }
}
