package com.jack.test.myspringmvc.controller;

import com.jack.test.myspringmvc.annotation.MyController;
import com.jack.test.myspringmvc.annotation.MyRequestMapping;
import sun.tools.attach.HotSpotVirtualMachine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Created by jie on 2018/4/5.
 */
@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyRequestMapping("/one")
    public void one(HttpServletRequest request, HttpServletResponse response, String name){
        System.out.println("test param name = " + name);
        try {
            SftpUtil.getMyObj(1, name);
            response.getWriter().write("传入参数name:" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
