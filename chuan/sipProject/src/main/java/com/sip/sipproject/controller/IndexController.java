package com.sip.sipproject.controller;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.dto.RequestBooking;
import com.sip.sipproject.dto.RequestRoom;
import com.sip.sipproject.dto.RoomPictureDTO;
import com.sip.sipproject.exception.quantityException;
import com.sip.sipproject.exception.quantityExceptionResponseBody;
import com.sip.sipproject.security.JwtTokenProvider;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@Controller
public class IndexController {
    @Autowired
    CustomerService customerService;
    @Autowired
    OrderTableService orderTableService;
    @Autowired
    TempOrderTableService tempOrderTableService;
    @Autowired
    IndexService indexService;
    @Autowired
    RoomPictureService roomPictureService;
    @Autowired
    RoomService roomService;
    @Autowired
    HotelService hotelService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    EcpayService ecpayService;

    //全域Model
    @ModelAttribute
    public void addAttributes(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 從 token 中提取使用者名稱
            String customerLoginName = jwtTokenProvider.getCustomerNameFromToken(token);

            model.addAttribute("customerLoginName", customerLoginName);
            model.addAttribute("userLoggedIn", true);
        } else {
            model.addAttribute("userLoggedIn", false);
        }
    }

    @GetMapping("/sipIndex")
    public String goIndex(HttpSession httpSession, Model model) {
        Integer quantity = 1;
        Integer totalPeople = 2;
        // 預設房間數量、人數
        // 檢查 quantity、totalPeople 是否為 null 存進Session
        if (httpSession.getAttribute("quantity") == null) {
            httpSession.setAttribute("quantity", quantity);
        }
        // 檢查 totalPeople 是否為 null
        if (httpSession.getAttribute("totalPeople") == null) {
            httpSession.setAttribute("totalPeople", totalPeople);
        }
        //Session springboot預設30分鐘刪除

        return "sip/sipIndex";
    }

    @GetMapping("/sipIndex/select")
    public String indexSelect(
            @RequestParam("city") String city,
            @RequestParam("checkinDay") String dateStart,
            @RequestParam("checkoutDay") String dateEnd,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("totalPeople") Integer totalPeople
            , Model model, HttpSession httpSession) throws ParseException {
        System.out.println("起始日期" + dateStart);
        System.out.println("結束日期" + dateEnd);
        // 如果 checkoutDay 是空的，返回首頁
        if (dateEnd == null || dateEnd.trim().isEmpty()) {
            return "redirect:/sipIndex";  // 返回首頁
        }
        // 轉型成日期
        // 將結束日期減少一天 ex:7/1-7/3 需要搜尋7/1-7/2房間數量
        LocalDate startDate = LocalDate.parse(dateStart);
        LocalDate endDate = LocalDate.parse(dateEnd).minusDays(1);
        // 模糊搜尋找出city，日期區間有開放的房間，房間數量大於搜尋總數
        List<Room> rooms = indexService.selectIndexRoom(city, startDate, endDate, quantity, totalPeople);
        //LocalDate 轉換為 Date 類型
        Date startDateAsDate = java.sql.Date.valueOf(startDate);
        Date adjustedEndDateAsDate = java.sql.Date.valueOf(endDate);
        for (Room room : rooms) {
            Integer UndiscountedTotalpPrice = indexService.UndiscountedTotalpPrice(room.getProductID(), quantity, startDateAsDate, adjustedEndDateAsDate);
            Integer totalRoomPrice = indexService.totalRoomPrice(room.getProductID(), quantity, startDateAsDate, adjustedEndDateAsDate);
            //折扣百分比
            double discountPercentage = (double) totalRoomPrice / UndiscountedTotalpPrice;
            //總價、折扣百分比 裝進room
            room.setDiscountPercentage(discountPercentage);
            room.setPrice(totalRoomPrice);
        }
        // 排序 rooms，按價格從高到低
        rooms.sort(Comparator.comparing(Room::getPrice).reversed());
        // 添加房間信息到 model
        model.addAttribute("rooms", rooms);
        httpSession.setAttribute("city", city);
        httpSession.setAttribute("checkinDay", dateStart);
        httpSession.setAttribute("checkoutDay", dateEnd);
        httpSession.setAttribute("quantity", quantity);
        httpSession.setAttribute("totalPeople", totalPeople);
        return "sip/sipSelect";
    }

    @GetMapping("/sipIndex/findPicture")
    @ResponseBody
    public List<RoomPictureDTO> findPicture(@Param(value = "productID") String productID) {
        Room room = roomService.findByProductID(productID);
        List<RoomPicture> roomImages = roomPictureService.findPictureByRoom(room);
        List<RoomPictureDTO> roomPictureDTOS = new ArrayList<>();
        for (RoomPicture roomPicture : roomImages) {
            // 創建一個新的 DTO 並設置 imgID
            RoomPictureDTO roomPictureDTO = new RoomPictureDTO();
            roomPictureDTO.setImgID(roomPicture.getImgID());  // 將 RoomPicture 中的 imgID 設置到 DTO
            // 將 DTO 加入列表
            roomPictureDTOS.add(roomPictureDTO);
        }
        return roomPictureDTOS;
    }

    @PostMapping("/sipIndex/booking")
    public String indexBooking(@Param(value = "productID") String productID, Model model, HttpSession httpSession) {
        //在迴圈 抓出Session的進出日期
        LocalDate localDateStart = LocalDate.parse((String) httpSession.getAttribute("checkinDay"));
        LocalDate localDateEnd = LocalDate.parse((String) httpSession.getAttribute("checkoutDay")).minusDays(1);
        // LocalDate 轉換為 Date 類型
        //startDateAsDate :開始日期
        //adjustedEndDateAsDate:調整後的結束日期
        Date startDateAsDate = java.sql.Date.valueOf(localDateStart);
        Date adjustedEndDateAsDate = java.sql.Date.valueOf(localDateEnd);
        Integer UndiscountedTotalpPrice;
        Integer totalRoomPrice;
        Integer minQuantity;
        //每點擊下一步都需 重新檢查 房間每個日期的數量  RoomQuantityPriceByDate-table
        Room room = indexService.checkRoomQuantity(productID, (Integer) httpSession.getAttribute("quantity"), startDateAsDate, adjustedEndDateAsDate);
        if (room != null) {
            UndiscountedTotalpPrice = indexService.UndiscountedTotalpPrice(productID, (Integer) httpSession.getAttribute("quantity"), startDateAsDate, adjustedEndDateAsDate);
            totalRoomPrice = indexService.totalRoomPrice(productID, (Integer) httpSession.getAttribute("quantity"), startDateAsDate, adjustedEndDateAsDate);
            minQuantity = indexService.findMinimumQuantityByDate(productID, startDateAsDate, adjustedEndDateAsDate);
            System.out.println("Roomprice:" + UndiscountedTotalpPrice);
            System.out.println("RoomDiscountPrice:" + totalRoomPrice);
            double discountPercentage = (double) totalRoomPrice / UndiscountedTotalpPrice;
            //把總價、折扣百分比、房間最小數量  裝進room
            room.setDiscountPercentage(discountPercentage);
            room.setPrice(totalRoomPrice);
            room.setMinQuantity(minQuantity);
        } else {
            //庫存不足時的處理
            System.out.println("庫存不足，無法處理預訂: 產品ID " + productID + ", 日期 " + localDateStart + ", 需要数量 " + httpSession.getAttribute("quantity"));
            //IllegalStateException在非法或不適當的時間呼叫方法時產生的訊號。程式沒有處於請求操作所要求的適當狀態。
            //用@ControllerAdvice 來處理Exception
            throw new quantityException("庫存不足，無法處理預訂: 產品ID" + productID + ", 日期 " + localDateStart);
        }

        //列出飯店其他種類數量大於1的剩餘房間  ex:使用者選取的商品 列出其餘商品
        List<Room> otherRooms = indexService.selectOtherRooms(room.getHotel(), localDateStart, localDateEnd);
        for (Room otherRoom : otherRooms) {
            UndiscountedTotalpPrice = indexService.UndiscountedTotalpPrice(otherRoom.getProductID(), (Integer) httpSession.getAttribute("quantity"), startDateAsDate, adjustedEndDateAsDate);
            totalRoomPrice = indexService.totalRoomPrice(otherRoom.getProductID(), (Integer) httpSession.getAttribute("quantity"), startDateAsDate, adjustedEndDateAsDate);
            minQuantity = indexService.findMinimumQuantityByDate(otherRoom.getProductID(), startDateAsDate, adjustedEndDateAsDate);
            //折扣百分比
            double discountPercentage = (double) totalRoomPrice / UndiscountedTotalpPrice;
            //把總價、折扣百分比、房間最小數量  otherRoom
            otherRoom.setDiscountPercentage(discountPercentage);
            otherRoom.setPrice(totalRoomPrice);
            otherRoom.setMinQuantity(minQuantity);
        }
        otherRooms.remove(room);
        room.getHotel().setHotelIntroduction(room.getHotel().getHotelIntroduction().replace("\n", "<br/>"));
        model.addAttribute("room", room);
        model.addAttribute("otherRooms", otherRooms);
        return "sip/sipBooking1";
    }

    @PostMapping("/sipIndex/requestBooking")
    @ResponseBody
    public String indexBooking2(@RequestBody RequestBooking requestBooking, HttpSession httpSession, @CookieValue(value = "Authorization", required = false) String token) {
        //判斷是否已經登入
        if (token != null && jwtTokenProvider.validateToken(token)) {
            httpSession.setAttribute("requestBooking", requestBooking);
            return "Y";
        } else {
            return "N";
        }
    }

    @GetMapping("/sipIndex/booking2")
    public String getIndexBooking2(Model model, HttpSession httpSession) {
        if (httpSession.getAttribute("requestBooking") == null) {
            return "redirect:/sipIndex";
        }
        RequestBooking requestBooking = (RequestBooking) httpSession.getAttribute("requestBooking");
        Integer totalPrice = requestBooking.getTotalPrice();
        List<RequestRoom> selectedRooms = requestBooking.getSelectedRooms();
        String hotelName = selectedRooms.isEmpty() ? "" : roomService.findByProductID(selectedRooms.get(0).getProductID()).getHotel().getHotelName();
        Integer totalPeople = (Integer) httpSession.getAttribute("totalPeople");
        String dateStart = (String) httpSession.getAttribute("checkinDay");
        String dateEnd = (String) httpSession.getAttribute("checkoutDay");
        model.addAttribute("hotelName", hotelName);
        model.addAttribute("totalPeople", totalPeople);
        model.addAttribute("selectedRooms", selectedRooms);
        model.addAttribute("checkinDay", dateStart);
        model.addAttribute("checkoutDay", dateEnd);
        model.addAttribute("totalPrice", totalPrice);
        return "sip/sipBooking2";
    }


    @PostMapping("/sipIndex/booking3")
    @ResponseBody
    @Transactional
    public String indexBooking4(@Param(value = "email") String email,
                                @Param(value = "lastName") String lastName,
                                @Param(value = "firstName") String firstName,
                                @Param(value = "tel") String tel,
                                @Param(value = "checkinDay") String checkinDay,
                                @Param(value = "checkoutDay") String checkoutDay,
                                @Param(value = "totalPeople") Integer totalPeople,
                                @Param(value = "totalPrice") Integer totalPrice,
                                @Param(value = "paymentMethod") String paymentMethod,
                                HttpSession httpSession, @CookieValue(value = "Authorization", required = false) String token) throws ParseException {

        RequestBooking requestBooking = (RequestBooking) httpSession.getAttribute("requestBooking");
        List<RequestRoom> selectedRooms = requestBooking.getSelectedRooms();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = dateFormat.format(currentDate);
        OrderTable orderTable = new OrderTable();
        Set<OrderItem> orderItems = new HashSet<>();
        IdGenerator forOrder = new IdGenerator();
        IdGenerator forOrderItem = new IdGenerator();
        Customer customer = customerService.findByID((Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        Hotel hotel = roomService.findByProductID(selectedRooms.get(0).getProductID()).getHotel();
        for (RequestRoom requestRoom : selectedRooms) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemID(forOrderItem.generateId());
            orderItem.setRoom(roomService.findByProductID(requestRoom.getProductID()));
            orderItem.setQuantity(requestRoom.getQuantity());
            orderItem.setOrderItemPrice(requestRoom.getOrderItemPrice());
            orderItems.add(orderItem);
        }
        orderTable.setOrderID(forOrder.generateId());
        orderTable.setCustomer(customer);
        orderTable.setHotel(hotel);
        orderTable.setEmail(email);
        orderTable.setLastName(lastName);
        orderTable.setFirstName(firstName);
        orderTable.setTel(tel);
        orderTable.setOrderTime(currentTime);
        orderTable.setOrderPrice(totalPrice);
        orderTable.setUpdateTime(currentTime);
        orderTable.setPayment(paymentMethod);
        orderTable.setNumPeople(totalPeople);
        orderTable.setCheckInDate(dateFormat.parse(checkinDay));
        orderTable.setCheckOutDate(dateFormat.parse(checkoutDay));

        for (RequestRoom requestRoom : selectedRooms) {
            LocalDate localDateStart = LocalDate.parse(checkinDay);
            LocalDate localDateEnd = LocalDate.parse(checkoutDay).minusDays(1);
            Date startDateAsDate = java.sql.Date.valueOf(localDateStart);
            Date adjustedEndDateAsDate = java.sql.Date.valueOf(localDateEnd);
            //檢查庫存並更新數量
            Boolean updateRoomQuantity = indexService.checkAndUpdateRoomQuantity(requestRoom.getProductID(), requestRoom.getQuantity(), startDateAsDate, adjustedEndDateAsDate);
            if (updateRoomQuantity != true) {
                //庫存不足時的處理
                System.out.println("庫存不足，無法處理預訂: 產品ID " + requestRoom.getProductID() + ", 日期 " + localDateStart + ", 需要数量 " + requestRoom.getQuantity());
                //IllegalStateException在非法或不適當的時間呼叫方法時產生的訊號。程式沒有處於請求操作所要求的適當狀態。
                //用@ControllerAdvice 來處理Exception
                throw new quantityExceptionResponseBody("庫存不足，無法處理預訂: 產品ID" + requestRoom.getProductID() + ", 日期 " + localDateStart);
            }
        }
        //更新數量完成建立訂單
        if (orderTableService.intserOrder(orderTable, orderItems)) {
            System.out.println("success");
            httpSession.removeAttribute("requestBooking");
            String orderID = orderTable.getOrderID();
            return orderID;
        } else {
            return "Error";
        }

    }

    @GetMapping("/sipIndex/orderDetail")
    public String orderDetail(@RequestParam("orderID") String orderID, @CookieValue(value = "Authorization", required = false) String token, Model model) {
        if (token == null) {
            return "redirect:/customer/login";  // 轉到登錄頁面
        }
        OrderTable orderTable = orderTableService.findOrderByOrderID_customer(orderID, customerService.findByID((Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token)))));
        model.addAttribute("orderTable", orderTable);
        return "sip/sipBooking3";
    }

    @PostMapping("/sipIndex/ECPay")
    @ResponseBody
    @Transactional
    public String ECPay(@Param(value = "email") String email,
                        @Param(value = "lastName") String lastName,
                        @Param(value = "firstName") String firstName,
                        @Param(value = "tel") String tel,
                        @Param(value = "checkinDay") String checkinDay,
                        @Param(value = "checkoutDay") String checkoutDay,
                        @Param(value = "totalPeople") Integer totalPeople,
                        @Param(value = "totalPrice") Integer totalPrice,
                        @Param(value = "paymentMethod") String paymentMethod,
                        HttpSession httpSession, @CookieValue(value = "Authorization", required = false) String token) throws ParseException {
        String orderID = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        RequestBooking requestBooking = (RequestBooking) httpSession.getAttribute("requestBooking");
        List<RequestRoom> selectedRooms = requestBooking.getSelectedRooms();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormatForOrderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentTime = dateFormatForOrderTime.format(currentDate);
        TempOrderTable tempOrderTable = new TempOrderTable();
        Set<TempOrderItem> tempOrderItems = new HashSet<>();
        IdGenerator forOrderItem = new IdGenerator();
        Customer customer = customerService.findByID((Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        Hotel hotel = roomService.findByProductID(selectedRooms.get(0).getProductID()).getHotel();
        for (RequestRoom requestRoom : selectedRooms) {
            TempOrderItem tempOrderItem = new TempOrderItem();
            tempOrderItem.setOrderItemID(forOrderItem.generateId());
            tempOrderItem.setRoom(roomService.findByProductID(requestRoom.getProductID()));
            tempOrderItem.setQuantity(requestRoom.getQuantity());
            tempOrderItem.setOrderItemPrice(requestRoom.getOrderItemPrice());
            tempOrderItems.add(tempOrderItem);
        }
        tempOrderTable.setOrderID(orderID);
        tempOrderTable.setCustomer(customer);
        tempOrderTable.setHotel(hotel);
        tempOrderTable.setEmail(email);
        tempOrderTable.setLastName(lastName);
        tempOrderTable.setFirstName(firstName);
        tempOrderTable.setTel(tel);
        tempOrderTable.setOrderTime(currentTime);
        tempOrderTable.setOrderPrice(totalPrice);
        tempOrderTable.setUpdateTime(currentTime);
        tempOrderTable.setPayment(paymentMethod);
        tempOrderTable.setNumPeople(totalPeople);
        tempOrderTable.setCheckInDate(dateFormat.parse(checkinDay));
        tempOrderTable.setCheckOutDate(dateFormat.parse(checkoutDay));
        for (RequestRoom requestRoom : selectedRooms) {
            LocalDate localDateStart = LocalDate.parse(checkinDay);
            LocalDate localDateEnd = LocalDate.parse(checkoutDay).minusDays(1);
            Date startDateAsDate = java.sql.Date.valueOf(localDateStart);
            Date adjustedEndDateAsDate = java.sql.Date.valueOf(localDateEnd);
            //檢查庫存並更新數量
            Boolean updateRoomQuantity = indexService.checkAndUpdateRoomQuantity(requestRoom.getProductID(), requestRoom.getQuantity(), startDateAsDate, adjustedEndDateAsDate);
            if (updateRoomQuantity != true) {
                //庫存不足時的處理
                System.out.println("庫存不足，無法處理預訂: 產品ID " + requestRoom.getProductID() + ", 日期 " + localDateStart + ", 需要数量 " + requestRoom.getQuantity());
                //IllegalStateException在非法或不適當的時間呼叫方法時產生的訊號。程式沒有處於請求操作所要求的適當狀態。
                //用@ControllerAdvice 來處理Exception
                throw new quantityExceptionResponseBody("庫存不足，無法處理預訂: 產品ID" + requestRoom.getProductID() + ", 日期 " + localDateStart);
            }
        }
        //更新數量完成建立訂單 開始建立ECpay
        if (tempOrderTableService.intserOrder(tempOrderTable, tempOrderItems)) {
            System.out.println("success");
            String aioCheckOutALLForm = ecpayService.ecpayCheckForm(orderID, totalPrice, selectedRooms);
            System.out.println(aioCheckOutALLForm);
            httpSession.removeAttribute("requestBooking");
            return aioCheckOutALLForm;
        } else {
            return "Error";
        }
    }

    //綠界回傳確認碼 需開ngrok外掛https 才能接收
    @PostMapping("/sipIndex/ecPayCheckMacValue")
    @ResponseBody
    @Transactional
    public String ecPayCheckMacValue(@RequestParam("MerchantTradeNo") String merchantTradeNo,
                                     @RequestParam("RtnCode") int rtnCode) {
        TempOrderTable tempOrderTable = tempOrderTableService.findByOrderId(merchantTradeNo);
        //交易成功
        if (rtnCode == 1) {
            System.out.println(merchantTradeNo);
            System.out.println(rtnCode);
            Set<TempOrderItem> tempOrderItems = tempOrderTable.getTempOrderItems();
            Set<OrderItem> orderItems = new HashSet<>();
            OrderTable orderTable = tempOrderTableService.convertTempOrderToOrder(tempOrderTable);
            for (TempOrderItem tempOrderItem : tempOrderItems) {
                OrderItem orderItem = tempOrderTableService.convertTempOrderItemToOrderItem(tempOrderItem);
                orderItem.setOrderTable(orderTable);
                orderItems.add(orderItem);
            }
            orderTable.setOrderItems(orderItems);
            orderTableService.intserOrder(orderTable, orderItems);
            tempOrderTableService.deleteOrder(tempOrderTable);
            System.out.println("交易成功，建立訂單，刪除臨時訂單");
            return "1|OK";

        } else {
            System.out.println("交易失敗，釋出訂單，還原商品數量");
            Set<TempOrderItem> tempOrderItems = tempOrderTable.getTempOrderItems();

            //checkout 日期並轉換為 LocalDate
            Date checkoutDate = tempOrderTable.getCheckOutDate();
            LocalDate checkoutLocalDate = checkoutDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // 扣除一天
            LocalDate adjustedCheckoutLocalDate = checkoutLocalDate.minusDays(1);

            // 將 adjustedCheckoutLocalDate 轉換回 Date
            Date adjustedCheckOutDate = Date.from(adjustedCheckoutLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            for (TempOrderItem tempOrderItem : tempOrderItems) {
                //檢查庫存並還原數量
                Boolean restoreRoomQuantity = indexService.checkAndRestoreRoomQuantityByDate(tempOrderItem.getRoom().getProductID(), tempOrderItem.getQuantity(), tempOrderTable.getCheckInDate(), adjustedCheckOutDate);
                if (restoreRoomQuantity) {
                    System.out.println("商品數量還原成功");
                    tempOrderTableService.deleteOrder(tempOrderTable);
                } else {
                    System.out.println("商品數量還原失敗");
                }
            }
            return "0|FAIL";
        }
    }

    @PostMapping("/sipIndex/ecPayOrderResultURL")
    public String ecPayOrderResultURL(@RequestParam("MerchantTradeNo") String merchantTradeNo, HttpServletResponse response) {
        OrderTable orderTable = orderTableService.findByOrderID(merchantTradeNo);
        Customer customer = orderTable.getCustomer();
        String token = jwtTokenProvider.generateToken(customer.getLoginID(), customer.getCustomerName());
        // 將 token 放入 Cookie 中
        ResponseCookie cookie = ResponseCookie.from("Authorization", token)
                .httpOnly(true)  // 防止 JS 訪問
                .secure(true)    // 僅在 HTTPS 連接中傳送
                .path("/")       // 設置 cookie 的路徑
                .maxAge(3600)    // 設定過期時間為 1 小時
                .build();
        // 設置響應頭，將 cookie 返回給前端
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        if (orderTable != null) {
            return "redirect:/sipIndex/orderDetail?orderID=" + merchantTradeNo;
        }
        System.out.println("訂單不存在");
        return null;
    }

    @PostMapping("/sipIndex/get")
    @ResponseBody
    public void deleteExpiredOrders() {
        TempOrderTable tempOrderTable = tempOrderTableService.findByOrderId("f7c45f18aeab4c298d05");
//        Set<TempOrderItem> tempOrderItems = new HashSet<>();
//        TempOrderItem tempOrderItem=new TempOrderItem();
//        tempOrderItem.setTempOrderTable(tempOrderTable);
//        tempOrderItem.setRoom(roomService.findByProductID("6a61ee71-501c-4584-944a-e1caf2072423"));
//        tempOrderItem.setQuantity(1);
//        tempOrderItem.setOrderItemPrice(2000);
//        tempOrderItem.setOrderItemID(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20));
//        tempOrderItems.add(tempOrderItem);
//        tempOrderTableService.intserOrder(tempOrderTable,tempOrderItems);
//        System.out.println("test");

        System.out.println(tempOrderTable.getTempOrderItems().size());
        System.out.println("test2");
//        for (OrderItem orderItem : orderItems) {
//            System.out.println(orderItem.getOrderItemID()+"---------------------------------------------");
//        }

    }


}
