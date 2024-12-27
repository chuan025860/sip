package com.sip.sipproject.controller;

import com.sip.sipproject.bean.Customer;
import com.sip.sipproject.security.JwtTokenProvider;
import com.sip.sipproject.service.CustomerService;
import com.sip.sipproject.service.DefaultPictureService;
import com.sip.sipproject.service.MailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Controller
public class CustomerController {
    @Autowired
    CustomerService customerService;
    @Autowired
    DefaultPictureService defaultPictureService;
    @Autowired
    MailService mailService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    //全域Model
    @ModelAttribute
    public void addAttributes(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 從 token 中提取使用者名稱
            String customerLoginName = jwtTokenProvider.getCustomerNameFromToken(token);
            Integer customerLoginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
            model.addAttribute("customerLoginName", customerLoginName);
            model.addAttribute("customerLoginId", customerLoginId);
        }
    }

    //導向修改帳號頁
    @GetMapping("/customer/index/setaccount")
    public String setAccount(HttpSession httpSession, Model model) {
        return "customer/setAccount";
    }

    //檢查是否綁定google
    @GetMapping("customer/index/setaccount/checkGoogleID")
    @ResponseBody
    public String checkGoogleID(@CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        if (customerService.checkGoogleIdExistByLoginId(loginId)) {
            System.out.println(loginId + " 已綁定");
            return "Y";
        }
        System.out.println(loginId + " 未綁定");
        return "N";
    }

    //綁定googleID
    @PutMapping("customer/index/setaccount/bindGoogleID")
    @ResponseBody
    public String bindGoogleID(@RequestBody Map<String, String> googleData, @CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        String googleID = googleData.get("sub");
        Customer result = customerService.oauth2CheckLogin(googleID);
        if (result == null) {
            Customer customer = customerService.findByID(loginId);
            customer.setGoogleID(googleID);
            customerService.updateCustomer(customer);
            System.out.println(loginId + " 已寫入googleID: " + googleID);
            return "Y";
        }

        return "N";
    }

    //清除googleID
    @DeleteMapping("customer/index/setaccount/clearGoogleID")
    @ResponseBody
    public String clearGoogleID(@RequestParam("customerLoginId") String loginid) {
        Customer customer = customerService.findByID(Integer.valueOf(loginid));
        customer.setGoogleID(null);
        customerService.updateCustomer(customer);
        System.out.println(loginid + " 已清除googleID");
        return "Y";

    }

    //檢查是否綁定line
    @GetMapping("customer/index/setaccount/checkLineID")
    @ResponseBody
    public String checkLineID(@CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        if (customerService.checkLineIdExistByLoginId(loginId)) {
            System.out.println(loginId + " 已綁定");
            return "Y";
        }
        System.out.println(loginId + " 未綁定");
        return "N";
    }


    //清除lineID
    @DeleteMapping("customer/index/setaccount/clearLineID")
    @ResponseBody
    public String clearLineID(@RequestParam("customerLoginId") Integer loginid) {
        Customer customer = customerService.findByID(loginid);
        customer.setLineID(null);
        customerService.updateCustomer(customer);
        System.out.println(loginid + " 已清除LineID");
        return "Y";
    }

    //寄送email成功
    @PostMapping("/customer/index/setaccount/change_password")
    @ResponseBody
    public String getMailCodeByCustomer(@RequestParam("email") String email, HttpSession httpSession) {
        if (Objects.equals(email, httpSession.getAttribute("customerloginEmail"))) {//判斷是否為同一個email
            //寄送驗證碼
            mailService.sendResetPwdCodeMail(email, httpSession);
            httpSession.setAttribute("resetMail", email);
            return "Y";
        } else {
            return "N";
        }
    }

    //轉變更密碼頁面
    @GetMapping("/customer/index/setaccount/changereset_password")
    public String changeresetPassword(HttpSession httpSession, Model model) {
        //判斷是否已寄送mail
        if (httpSession.getAttribute("resetMail") != null) {
            return "customer/changePwd";
        } else if //判斷有無resetMail 沒有轉回setaccount
        (httpSession.getAttribute("resetMail") == null) {
            return "redirect:/customer/index/setaccount";
        }
        return "redirect:/customer/login";
    }

    //更改密碼
    @PutMapping("/customer/index/setaccount/changereset_password")
    @ResponseBody
    public String postResetPassword(@RequestParam("newPwd") String newPwd,
                                    @RequestParam("verificationCode") String verificationCode,
                                    HttpSession httpSession,
                                    Model model, HttpServletResponse response) {

        String resetMail = httpSession.getAttribute("resetMail").toString();
        String sessionCode = httpSession.getAttribute("verificationCode").toString();


        System.out.println("更新Pwd Email: " + resetMail);
        System.out.println("Session驗證碼: " + sessionCode);
        System.out.println("輸入驗證碼: " + verificationCode);

        if (Objects.equals(verificationCode, sessionCode)) {
            Boolean pwdResult = customerService.resetPwd(resetMail, newPwd);
            if (pwdResult) {
                httpSession.removeAttribute("resetMail");
                httpSession.removeAttribute("verificationCode");
                System.out.println(resetMail + "驗證碼已刪除");
                mailService.sendMail(resetMail, "密碼變更成功!!!", "您的帳號: " + resetMail + "\n密碼已變更" + "\n變更時間: " + new Date());
                //登出   刪除 JWT Cookie
                Cookie jwtCookie = new Cookie("Authorization", null);
                jwtCookie.setPath("/"); // 設定 cookie 路徑，確保它能在整個應用中被刪除
                jwtCookie.setMaxAge(0); // 設定 cookie 的最大生存時間為 0，表示立即過期
                response.addCookie(jwtCookie); // 添加 cookie 到響應中

                return "Y";
            } else {
                return "errMsg";
            }
        } else {
            return "N";
        }
    }

    //重新寄送驗證碼
    @GetMapping("/customer/index/setaccount/resendVerificationCode_password")
    public String resendVerificationCode(HttpSession httpSession) {
        //判斷是否登入、hotelID
        if (httpSession.getAttribute("resetMail") != null) {
            String email = httpSession.getAttribute("resetMail").toString();
            mailService.sendResetPwdCodeMail(email, httpSession);
            return "customer/changePwd";
        } else if (httpSession.getAttribute("resetMail") == null) {
            return "redirect:/customer/index/setaccount";
        }
        return "redirect:/customer/login";
    }

    @PostMapping("/customer/index/setaccount/picture")
    @ResponseBody
    public String AddCutomerPicture(@RequestParam MultipartFile file, @CookieValue(value = "Authorization", required = false) String token) throws IOException {
        if (file.isEmpty()) {
            // 圖片為空，可以加入對應的錯誤處理邏輯，例如傳回一個錯誤頁面或訊息
            return "N";
        }
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        Customer customer = customerService.findByID(loginId);
        customer.setHeadshot(file.getBytes());
        customerService.updateCustomer(customer);
        return "Y";
    }

    //導向會員詳細資料頁
    @GetMapping("/customer/index/customerdetail")
    public String customerdetail(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        Customer customer = customerService.findByID(loginId);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDateStr = outputDateFormat.format(customer.getBirthday());
        System.out.println(formattedDateStr);
        model.addAttribute("customer", customer);
        model.addAttribute("Birthday", formattedDateStr);
        return "/customer/customerDetail";
    }

    //導向會員更新資料頁
    @PostMapping("/customer/index/updateCustomer")
    public String upCustomer(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        Customer customer = customerService.findByID(loginId);
        model.addAttribute("customer", customer);
        return "/customer/updateCustomer";
    }

    //更新資料
    @PutMapping("/customer/index/update")
    @ResponseBody
    public String customerupdate(@RequestBody Customer updateCustomer, @CookieValue(value = "Authorization", required = false) String token) {
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        Customer customer = customerService.findByID(loginId);
        customer.setCustomerName(updateCustomer.getCustomerName());
        customer.setBirthday(updateCustomer.getBirthday());
        customer.setSex(updateCustomer.getSex());
        customer.setCountry(updateCustomer.getCountry());
        customer.setCity(updateCustomer.getCity());
        customer.setRegion(updateCustomer.getRegion());
        customer.setStreet(updateCustomer.getStreet());
        customer.setPostalCode(updateCustomer.getPostalCode());
        customer.setPhone(updateCustomer.getPhone());
        customerService.updateCustomer(customer);
        return "Y";
    }
}
