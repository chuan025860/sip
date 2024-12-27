package com.sip.sipproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 啟用定時任務
public class SipProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipProjectApplication.class, args);
    }

}
