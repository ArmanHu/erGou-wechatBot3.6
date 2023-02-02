package com.arman;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.arman.idiom.SendTask;


@SpringBootApplication
public class WechatBotApplication {
    public static String[] args;
    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        WechatBotApplication.args = args;
        WechatBotApplication.context = SpringApplication.run(WechatBotApplication.class, args);
        new Thread(new SendTask()).start();
    }

}
