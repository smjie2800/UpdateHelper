package com.updateHelper;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class KYUpdateHelperApp extends SpringBootServletInitializer {
    private static final Logger logger = Logger.getLogger(KYUpdateHelperApp.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(KYUpdateHelperApp.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(KYUpdateHelperApp.class, args);
        userOperation();
    }
    public static void userOperation() {
        //等待用户操作
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String input = "";
        while (!input.toLowerCase().equals("exit")){
            try {
                logger.info("服务运行中，退出请输入exit……");
                input = bufferedReader.readLine();

            } catch (IOException e) {
                logger.error("读取用户输入错误：",e);
            }
        }
        logger.info("用户终止，服务器退出……");
        System.exit(0);
    }
}