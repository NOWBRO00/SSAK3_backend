package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.example.config.KakaoProperties;

@SpringBootApplication
@EnableConfigurationProperties(KakaoProperties.class)
public class Main {
    public static void main(String[] args) {
        System.out.println("============ [Main.java] Main.main() 시작 ============");
        SpringApplication.run(Main.class, args);
        System.out.println("============ [Main.java] Main.main() 종료 ============");
    }
}