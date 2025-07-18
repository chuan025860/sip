package org.chyunn_web.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public  boolean sendSimpleMail(String to, String subject, String contentHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("it@chyunn.com.tw"); // 必須明確設為與登入帳號相同
            helper.setTo(to);                             // 收件者
            helper.setSubject(subject);                   // 主旨
            helper.setText(contentHtml, true); // true = HTML
            System.out.println("發出email:"+to);
            mailSender.send(message);
            System.out.println("寄發成功email");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ 發送失敗：" + e.getMessage());
            return false;
        }

    }
}
