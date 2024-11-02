package com.sip.sipproject.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailService {

    @Autowired
    JavaMailSender mailSender;


    public void sendRegisterPwdCodeMail(String email, HttpSession httpSession) {
        //生成驗證碼
        String verificationCode = getVerificationCode();

        // 將驗證碼存入 HttpSession
        httpSession.setAttribute("verificationCode", verificationCode);

        //寄送MAIL
        sendMail(email, "註冊驗證碼", "您的註冊驗證碼是：" + verificationCode + "\n驗證連結:" + "http://localhost:8080/sip/hotel/registerCode?verificationCode=" + verificationCode);
    }

    public void sendRegisterPwdCodeMailForCustomer(String email, HttpSession httpSession) {
        //生成驗證碼
        String verificationCode = getVerificationCode();

        // 將驗證碼存入 HttpSession
        httpSession.setAttribute("verificationCode", verificationCode);

        //寄送MAIL
        sendMail(email, "註冊驗證碼", "您的註冊驗證碼是：" + verificationCode + "\n驗證連結:" + "http://localhost:8080/sip/customer/registerCode?verificationCode=" + verificationCode);
    }

    public void sendResetPwdCodeMail(String email, HttpSession httpSession) {
        //生成驗證碼
        String verificationCode = getVerificationCode();

        // 將驗證碼存入 HttpSession
        httpSession.setAttribute("verificationCode", verificationCode);

        //寄送MAIL
        sendMail(email, "重新設定密碼", "您的驗證碼是：" + verificationCode);
    }

    public void sendMail(String email, String title, String mailMsg) {
        //寄送MAIL
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SIP.com<sipbooking.com.service@gmail.com>");
        message.setTo(email);
        message.setSubject("SIP.com " + title);
        message.setText(mailMsg);
        mailSender.send(message);
    }

    public String getVerificationCode() {
        // 生成6位數驗證碼
        int length = 6;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }

}
