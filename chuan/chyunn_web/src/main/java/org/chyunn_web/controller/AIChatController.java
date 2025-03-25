package org.chyunn_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AIChatController {
    @GetMapping("aichat/aichat")
    public String AIChat(
    ) {
        return "aichat/aichat";
    }
}
