package org.chyunn_incident.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AssetController {
    //轉去資訊設備總表
    @GetMapping("/asset/select_ITAsset")
    public String into_select_ITAsset() {
        return "AssetManagement/select_ITAsset";
    }
}
