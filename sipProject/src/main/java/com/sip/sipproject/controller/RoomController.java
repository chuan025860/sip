package com.sip.sipproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sip.sipproject.bean.*;
import com.sip.sipproject.dto.RequestRoom;
import com.sip.sipproject.dto.RoomQuantityPriceByDateDTO;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class RoomController {

    @Autowired
    private RoomService roomService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    RoomPictureService roomPictureService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RoomQuantityByDateService roomQuantityByDateService;

    @GetMapping("/room/insterRoom")
    public String intserRoom(@RequestParam("hotelID") Integer hotelID, HttpSession session, Model model) {
        if (session.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("hotelID", hotelID);
        return "room/insterRoom";
    }

    @PostMapping("/room/inster")
    @ResponseBody
    public String roomInster(@RequestParam("productImages") List<MultipartFile> productImages,
                             @RequestParam("requestData") String requestData) throws IOException, ParseException {

        //設定圖片允許的最大數量
        int maxAllowedImages = 20;
        if (productImages.size() > maxAllowedImages) {
            return "ExceededMaxLimit";
        }
        // 解析 JSON 數據
        ObjectMapper objectMapper = new ObjectMapper();
        RequestRoom requestRoom = objectMapper.readValue(requestData, RequestRoom.class);
        Room room = requestRoom.getRoom();
        RoomQuantityPriceByDate roomQuantityPriceByDate = requestRoom.getRoomQuantityPriceByDate();
        Hotel hotel = hotelService.findByHotelId(requestRoom.getHotelID());
        roomService.insterRoom(hotel, room, roomQuantityPriceByDate, requestRoom.getDateStart(), requestRoom.getDateEnd(), productImages);
        return "Y";
    }

    @GetMapping("/room/findAllRoom")
    public String findAllRoomByHotelId(HttpSession session, Model model, @RequestParam("hotelID") String hotelID) {
        if (session.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        List<Room> rooms = roomService.findRoomsByhotel(hotelService.findByHotelId(Integer.parseInt(hotelID)));
        model.addAttribute("rooms", rooms);
        model.addAttribute("hotelID", hotelID);
        return "room/findRoom";
    }

    @GetMapping("/room/updateroom")
    public String updateroom(HttpSession session, Model model, @RequestParam("hotelID") String hotelID, @RequestParam("productID") String productID) {
        if (session.getAttribute("loginId") == null || hotelID == null && productID == null) {
            return "redirect:/hotel/login";
        }
        Room room = roomService.findByProductID(productID);
        model.addAttribute("room", room);
        model.addAttribute("hotelID", hotelID);
        return "room/updateRoom";
    }

    @PutMapping("/room/update")
    @ResponseBody
    public String updateRoom(@RequestBody RequestRoom requestRoom) throws IOException {
        requestRoom.getRoom();
        Hotel hotel = hotelService.findByHotelId(requestRoom.getHotelID());
        roomService.updateRoom(hotel, requestRoom.getRoom());
        return "Y";
    }

    @DeleteMapping("/room/picturedelete")
    @ResponseBody
    public String picturedelete(@RequestParam("imgIDs") List<Integer> imgIDs) {
        System.out.println(imgIDs);
        if (imgIDs != null && !imgIDs.isEmpty()) {
            for (Integer imgID : imgIDs) {
                roomPictureService.deletePicture(imgID);
            }
            return "Y";
        } else {
            return "N";
        }
    }

    @PutMapping("/room/picture")
    @ResponseBody
    public String updateRoomlPicture(@RequestParam("productImages") List<MultipartFile> productImages, @RequestParam("productID") String productID) throws
            IOException {
        if (productImages.isEmpty()) {
            return "N";
        }

        // 當前圖片的數量
        int currentImageCount = roomPictureService.countByRoom(roomService.findByProductID(productID));
        //設定圖片允許的最大數量
        int maxAllowedImages = 20;
        if (currentImageCount + productImages.size() > maxAllowedImages) {
            return "ExceededMaxLimit";
        }
        roomPictureService.updateRoomPicture(productImages, productID);
        return "Y";
    }

    //轉去庫存數量
    @GetMapping("/room/roomquantity")
    public String findOrderDetail(HttpSession session, @RequestParam("hotelID") String hotelID, @RequestParam("productID") String productID, Model model) {
        if (session.getAttribute("loginId") == null || hotelID == null && productID == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("productID", productID);
        String productName = roomService.findByProductID(productID).getProductName();
        model.addAttribute("productName", productName);
        model.addAttribute("hotelID", hotelID);
        return "room/roomQuantity";
    }

    //AJAX回傳庫存數量
    @GetMapping("/room/quantity")
    @ResponseBody
    public List<RoomQuantityPriceByDateDTO> findRoomQuantity(@RequestParam("productID") String productID) {
        Room room = roomService.findByProductID(productID);
        List<RoomQuantityPriceByDate> roomQuantityPriceByDates = roomQuantityByDateService.findRoomQuantityByRoom(room);
        //回傳roomQuantityPriceByDates  room導致會序列化
        //使用DTO或JsonIgnore  DTO裝入ID、quantity、price、date、discountPrice
        List<RoomQuantityPriceByDateDTO> roomQuantityPriceByDateDTOS = new ArrayList<>();
        for (RoomQuantityPriceByDate r : roomQuantityPriceByDates) {
            RoomQuantityPriceByDateDTO dto = new RoomQuantityPriceByDateDTO();
            dto.setRoomQuantityID(r.getRoomQuantityID());
            dto.setQuantityByDate(r.getQuantityByDate());
            dto.setPrice(r.getPrice());
            dto.setDate(r.getDate());
            dto.setDiscountPrice(r.getDiscountPrice());
            roomQuantityPriceByDateDTOS.add(dto);
        }
        return roomQuantityPriceByDateDTOS;
    }

    @PutMapping("/room/updatePriceAndQuantity")
    @ResponseBody
    public ResponseEntity<Object> updateRoomPriceAndQuantity(
            @RequestParam("selectedDate") String selectedDate,
            @RequestParam("productID") String productID,
            @RequestParam("quantity") String quantity,
            @RequestParam("price") String price,
            @RequestParam(name = "discountPrice", defaultValue = "null") String discountPrice,
            @RequestParam(name = "discountOptions", defaultValue = "null") String discountOptions) {
        String[] dateParts = selectedDate.split(" - ");
        Room room = roomService.findByProductID(productID);
        String dateStart = convertToNewFormat(dateParts[0]);
        String dateEnd = convertToNewFormat(dateParts[1]);
        System.out.println("修改格式後: " + dateStart + " - " + dateEnd);
        roomQuantityByDateService.updateRoomQuantity(room, dateStart, dateEnd, price, quantity, discountOptions, discountPrice);
        List<RoomQuantityPriceByDate> roomQuantityPriceByDates = roomQuantityByDateService.findRoomQuantityByRoom(room);
        List<RoomQuantityPriceByDateDTO> roomQuantityPriceByDateDTOS = new ArrayList<>();
        for (RoomQuantityPriceByDate r : roomQuantityPriceByDates) {
            RoomQuantityPriceByDateDTO dto = new RoomQuantityPriceByDateDTO();
            dto.setRoomQuantityID(r.getRoomQuantityID());
            dto.setQuantityByDate(r.getQuantityByDate());
            dto.setPrice(r.getPrice());
            dto.setDate(r.getDate());
            dto.setDiscountPrice(r.getDiscountPrice());
            roomQuantityPriceByDateDTOS.add(dto);
        }
        return new ResponseEntity<>(roomQuantityPriceByDateDTOS, HttpStatus.OK);
    }

    //  啟用/停用房間
    @PutMapping("/room/updateRoomState")
    @ResponseBody
    public String updateRoomState(@RequestBody RequestRoom requestData) {
        Room room = roomService.findByProductID(requestData.getProductID());
        roomService.updateRoomState(room);
        return "Y";
    }

    @GetMapping("/room/previewRoom")
    public String previewRoom(HttpSession session, Model model, @RequestParam("hotelID") String hotelID, @RequestParam("productID") String productID) {
        if (session.getAttribute("loginId") == null || hotelID == null && productID == null) {
            return "redirect:/hotel/login";
        }
        Room room = roomService.findByProductID(productID);
        double discountPercentage = 1;
        room.setDiscountPercentage(discountPercentage);
        model.addAttribute("room", room);
        model.addAttribute("hotelID", hotelID);
        return "room/previewRoom";
    }

    //日期轉換方法
    private static String convertToNewFormat(String originalDate) {
        String[] parts = originalDate.split("年|月|日");
        if (parts.length == 3) {
            return parts[0] + "-" + String.format("%02d", Integer.parseInt(parts[1])) + "-" + String.format("%02d", Integer.parseInt(parts[2]));
        } else {
            return originalDate;
        }
    }

}

