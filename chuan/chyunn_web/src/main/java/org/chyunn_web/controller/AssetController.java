package org.chyunn_web.controller;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AssetController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    AssetService assetService;
    //轉去資訊設備總表
    @GetMapping("/asset/select_ITAsset")
    public String into_select_ITAsset() {
        return "assetManagement/select_ITAsset";
    }

    //查找所有事件
    @PostMapping("/asset/select_ITAsset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> select_incident(
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "100") int size, // 每頁顯示多少筆資料
            @RequestHeader("Authorization") String token
    ) {
        if (jwtTokenProvider.validateToken(token)) {
            Pageable pageable = PageRequest.of(page, size);
            // 分頁查詢資料
            Page<Inventory_Equipment> inventoryEquipmentPage = assetService.findAll(pageable);
            System.out.println(inventoryEquipmentPage.getSize());
            List<Inventory_EquipmentDto> inventoryEquipmentDtos = new ArrayList<>();
            for (Inventory_Equipment Inventory_Equipment : inventoryEquipmentPage.getContent()) {
                Inventory_EquipmentDto inventoryEquipmentDto = assetService.convertInventory_EquipmentDto(Inventory_Equipment);

                inventoryEquipmentDtos.add(inventoryEquipmentDto);
            }
            // 回傳分頁結果
            Map<String, Object> response = new HashMap<>();
            response.put("content", inventoryEquipmentDtos); // 事件內容
            response.put("totalPages", inventoryEquipmentPage.getTotalPages()); // 總頁數
            response.put("totalElements", inventoryEquipmentPage.getTotalElements()); // 總筆數
            response.put("currentPage", page); // 當前頁數
            return ResponseEntity.ok(response); // 返回成功的 response
        } else {
            // 如果 token 驗證失敗，返回 401 未授權錯誤
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Token 無效或過期");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    //
    @PostMapping("asset/getPropertyDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPropertyDetails(
            @RequestParam String id,
            @RequestHeader("Authorization") String token
    ) {
        System.out.println(id);
        if (jwtTokenProvider.validateToken(token)) {
           Inventory_Equipment inventoryEquipment= assetService.getPropertyDetails(id);
            // 回傳分頁結果
            Map<String, Object> response = new HashMap<>();
            response.put("data", inventoryEquipment); // 事件內容
            return ResponseEntity.ok(response); // 返回成功的 response
        } else {
            // 如果 token 驗證失敗，返回 401 未授權錯誤
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Token 無效或過期");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
