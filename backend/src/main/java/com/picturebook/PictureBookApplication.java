package com.picturebook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.picturebook.mapper")
public class PictureBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(PictureBookApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  儿童绘本阅读行为分析与成长跟踪系统启动成功！");
        System.out.println("  接口文档地址: http://localhost:8080/api");
        System.out.println("==========================================");
    }
}
