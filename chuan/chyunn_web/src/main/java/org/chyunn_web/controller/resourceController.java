package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.dto.BulletinPostDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.BulletinPostService;
import org.chyunn_web.service.ResourceBorrowRequestServie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class resourceController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ResourceBorrowRequestServie resourceBorrowRequestServie;
    @Autowired
    BulletinPostService bulletinPostService;

    @GetMapping("/resource/calendar")
    public String calendar(
    ) {
        return "resourceManagement/calendar";
    }

    @GetMapping("/resource/bulletin")
    public String bulletin(Model model
    ) {
        List<BulletinPost> bulletinPosts = bulletinPostService.findAll();
        List<BulletinPostDto>bulletinPostDtos=new ArrayList<>();
        for (BulletinPost post : bulletinPosts) {
            BulletinPostDto bulletinPostDto = bulletinPostService.convertBulletinPostDto(post);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            bulletinPostDto.setPublishTimeStr(post.getPublish_time().format(formatter));
            bulletinPostDto.setCreatorName(post.getCreator().getUsername());
            bulletinPostDtos.add(bulletinPostDto);
        }
        model.addAttribute("bulletinPostDtos", bulletinPostDtos);
        return "resourceManagement/bulletin";
    }

    @GetMapping("/resource/bulletinDetail")
    public String BulletinDetail(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes
    ) {
        try {
            BulletinPost bulletinPost = bulletinPostService.findById(id);
            if (bulletinPost == null) {
                throw new RuntimeException("找不到指定的公告資料");
            }
            String contentHtml = bulletinPost.getContent();
            Pattern pattern = Pattern.compile("<img[^>]+src=[\"'](/chyunn/uploads/ckeditor_images/[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentHtml);
            System.out.println("📸 以下為圖片路徑：");
            while (matcher.find()) {
                String imageUrl = matcher.group(1);
                System.out.println(imageUrl);
            }
            BulletinPostDto bulletinPostDto = bulletinPostService.convertBulletinPostDto(bulletinPost);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            bulletinPostDto.setPublishTimeStr(bulletinPost.getPublish_time().format(formatter));
            bulletinPostDto.setCreatorName(bulletinPost.getCreator().getUsername());

            model.addAttribute("bulletinPost", bulletinPostDto);
            return "resourceManagement/bulletinDetail";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "無法載入，已自動返回公告列表");
            return "redirect:/resource/bulletin";
        }
    }

    @GetMapping("/resource_manage/insertBulletin")
    public String insertBulletin(
    ) {
        return "resourceManagement/insertBulletin";
    }

    @GetMapping("/resource/resources")
    public String resources(Model model
    ) {
        return "resourceManagement/resources";
    }

    @ModelAttribute
    public void addUserInfoToModel(HttpServletRequest request, Model model) {
        String token = null;

        // 從 Cookie 中取得 token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String loginId = jwtTokenProvider.getLoginIdFromToken(token);
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);
            model.addAttribute("loginId", loginId);
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }

}
