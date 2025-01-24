package org.inventory.controller;
import org.inventory.bean.Inventory;
import org.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/invertory_index")
    public String into_index_inventory() {
        return "inventory/invertory_index";
    }

    @GetMapping("/invertory_select_location")
    public String into_select_location_inventory() {
        return "inventory/invertory_select_location";
    }

    @PostMapping("/invertory_search_location")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> into_search_location_inventory(@RequestParam String location,
                                                                              @RequestParam String stateString) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Boolean state = null;
        if (!stateString.isEmpty()) {
            state = Boolean.parseBoolean(stateString);
        }
        if (!location.isEmpty() && !stateString.isEmpty()) {
            List<Inventory> inventories = inventoryService.findByLocationAndState(location,state);
            response.put("code", 200);
            response.put("message", "success");
            data.put("inventories", inventories);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else if (!location.isEmpty() && stateString.isEmpty()) {
            List<Inventory> inventories = inventoryService.findByLocation(location);
            response.put("code", 200);
            response.put("message", "success");
            data.put("inventories", inventories);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else if (location.isEmpty() && !stateString.isEmpty()) {
            response.put("code", 404);
            response.put("message", "未設定此條件");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/inventory")
    public String into_select_inventory(@RequestParam(required = false) String final_property_id, Model model) {
        model.addAttribute("propertyID", final_property_id);
        return "inventory/inventory_select";
    }

    @PostMapping("/inventory/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> into_search(@RequestParam String final_property_id) {
        System.out.println(final_property_id);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Inventory inventory = inventoryService.getInventoryById(final_property_id);
        if (inventory != null) {
            response.put("code", 200);
            response.put("message", "success");
            data.put("inventory", inventory);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/inventory/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update(@RequestParam String final_property_id,
                                                                @RequestParam String name,
                                                                @RequestParam String location,
                                                                @RequestParam String remarks,
                                                                @RequestParam String custodian,
                                                                @RequestParam String change_record) {
        System.out.println(final_property_id);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();


        try {
            Inventory inventory = inventoryService.getInventoryById(final_property_id);
            if (inventory != null) {
                inventory.setName(name);
                inventory.setLocation(location);
                inventory.setRemarks(remarks);
                inventory.setCustodian(custodian);
                inventory.setChange_record(change_record);
                inventory.setIs_updated(true);
                Optional<Inventory> updatedInventory = inventoryService.insertInventory(inventory);
                if (updatedInventory.isPresent()) {
                    response.put("code", 200);
                    response.put("message", "success");
                    data.put("inventory", updatedInventory.get());
                    response.put("data", data);
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("code", 404);
                response.put("message", "錯誤");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("code", 500);
        response.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PutMapping("/inventory/updateState")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update_state(@RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            Inventory inventory = inventoryService.getInventoryById(final_property_id);
            if (inventory != null) {
                    if (inventory.getState()){
                    inventory.setState(false);
                }else {
                    inventory.setState(true);
                }
                Optional<Inventory> updatedInventory = inventoryService.insertInventory(inventory);
                if (updatedInventory.isPresent()) {
                    response.put("code", 200);
                    response.put("message", "success");
                    response.put("state", updatedInventory.get().getState());
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("code", 404);
                response.put("message", "錯誤");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("code", 500);
        response.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
