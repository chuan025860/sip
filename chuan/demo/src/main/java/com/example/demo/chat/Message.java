package com.example.demo.chat;

import lombok.Data;

@Data
public class Message {
    private String receiveEmail;
    private String senderEmail;
    private String text;

}
