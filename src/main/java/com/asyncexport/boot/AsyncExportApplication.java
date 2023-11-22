package com.asyncexport.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "com.asyncexport.boot.mapper")
public class AsyncExportApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncExportApplication.class, args);
    }

}
