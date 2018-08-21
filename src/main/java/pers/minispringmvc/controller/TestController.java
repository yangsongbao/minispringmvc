package pers.minispringmvc.controller;


import pers.minispringmvc.annotation.YController;
import pers.minispringmvc.annotation.YRequestMapping;
import pers.minispringmvc.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@YController
@YRequestMapping("/controller")
public class TestController {

    @YRequestMapping("/test1")
    public void test1(HttpServletRequest request,
                      HttpServletResponse response,
                      @YRequestParam("param") String param) {
        System.out.println(param);
        try {
            response.getWriter().write("test1 method success! param:" + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @YRequestMapping("/test2")
    public void test2(HttpServletRequest request,
                      HttpServletResponse response,
                      @YRequestParam("param") String param) {
        System.out.println(param);
        try {
            response.getWriter().write("test2 method success! param:" + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
