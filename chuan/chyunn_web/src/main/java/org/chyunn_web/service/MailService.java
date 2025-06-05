package org.chyunn_web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    public void sendSimpleMail( String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("it@chyunn.com.tw"); // 必須明確設為與登入帳號相同
        message.setTo("dimimg92306@gmail.com");                             // 收件者
        message.setSubject(subject);                   // 主旨
        message.setText(text);                         // 內容
        mailSender.send(message);
        System.out.println("scuess");
    }
}
