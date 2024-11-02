package com.sip.sipproject.controller;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.dto.*;
import com.sip.sipproject.exception.quantityExceptionResponseBody;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class OrderController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private OrderTableService orderTableService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RoomService roomService;

    @Autowired
    IndexService IndexService;
    @Autowired
    CustomerService customerService;

    //轉去搜尋訂單
    @GetMapping("/hotel/order")
    public String findOrder(HttpSession httpSession, @RequestParam("hotelID") Integer hotelID, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("hotelID", hotelID);
        return "order/findOrder";
    }

    //用來檢查字串是否為 null 或空
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    //確定訂單狀態
    private String determineOrderStatus(OrderTable orderTable) {
        Date currentDate = new Date(); // 當前日期
        if (orderTable.getOrderStatus().equals("canceled")) {
            return "取消";
        } else if (currentDate.before(orderTable.getCheckInDate())) {
            return "有效";//如果入住日期還沒到，狀態顯示「有效」。
        } else if (currentDate.after(orderTable.getCheckOutDate())) {
            return "過期";//如果退房日期已過，狀態顯示「過期」。
        } else {
            return "進行中";//如果當前日期在入住和退房日期之間，狀態顯示「進行中」。
        }
    }

    @PostMapping("/hotel/selectorder")
    @ResponseBody
    public List<OrderDTO> selectOrder(@RequestBody RequestOrder requestOrder) {
        //用id、hotel搜尋  避免搜尋到其他飯店order
        if (isNullOrEmpty(requestOrder.getStatus()) && isNullOrEmpty(requestOrder.getSelectedDate())) {
            OrderTable orderTable = orderTableService.findOrderByOrderID_hotel(requestOrder.getOrderID(), hotelService.findByHotelId(requestOrder.getHotelID()));
            orderTable.setOrderStatus(determineOrderStatus(orderTable));
            List<OrderItemDTO> orderItemDTOS = OrderItemDTO.convertToOrderItemDTOList(orderTable.getOrderItems());
            OrderDTO orderDTO = new OrderDTO(orderTable.getOrderID(), orderTable.getLastName(), orderTable.getFirstName(), orderTable.getCheckInDate(), orderTable.getCheckOutDate(), orderTable.getOrderPrice(), orderTable.getPayment(), orderTable.getOrderStatus(), orderItemDTOS);
            List<OrderDTO> orderDTOS = new ArrayList<>();
            orderDTOS.add(orderDTO);
            return orderDTOS;
        }//用日期搜尋
        else if (isNullOrEmpty(requestOrder.getOrderID()) && isNullOrEmpty(requestOrder.getStatus())) {
            String[] dateParts = requestOrder.getSelectedDate().split(" - ");
            if (dateParts.length == 2) {
                String startDate = convertToNewFormat(dateParts[0]);
                String endDate = convertToNewFormat(dateParts[1]);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date checkInDate = dateFormat.parse(startDate);
                    Date checkOutDate = dateFormat.parse(endDate);
                    List<OrderTable> orderTables = orderTableService.findOrderDate(checkInDate, checkOutDate, hotelService.findByHotelId(requestOrder.getHotelID()));
                    // 建立一個用於儲存訂單和訂單項目的DTO列表
                    List<OrderDTO> orderDTOS = new ArrayList<>();
                    for (OrderTable orderTable : orderTables) {
                        orderTable.setOrderStatus(determineOrderStatus(orderTable));
                        List<OrderItemDTO> orderItemDTOS = OrderItemDTO.convertToOrderItemDTOList(orderTable.getOrderItems());
                        OrderDTO orderDTO = new OrderDTO(orderTable.getOrderID(), orderTable.getLastName(), orderTable.getFirstName(), orderTable.getCheckInDate(), orderTable.getCheckOutDate(), orderTable.getOrderPrice(), orderTable.getPayment(), orderTable.getOrderStatus(), orderItemDTOS);
                        orderDTOS.add(orderDTO);
                    }
                    return orderDTOS;
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }//用訂單狀態搜尋
        else if (isNullOrEmpty(requestOrder.getOrderID()) && isNullOrEmpty(requestOrder.getSelectedDate())) {
            Date currentDate = new Date(); // 當前日期
            //資料篩選訂單狀態，不取回該飯店的訂單再用程式分類。有效減少不必要的資料傳輸量和程式運行時的計算資源消耗。
            List<OrderTable> orderTables = orderTableService.findOrderByHotelAndStatus(currentDate, requestOrder.getStatus(), hotelService.findByHotelId(requestOrder.getHotelID()));
            // 建立一個用於儲存訂單和訂單項目的DTO列表
            List<OrderDTO> orderDTOS = new ArrayList<>();
            for (OrderTable orderTable : orderTables) {
                orderTable.setOrderStatus(determineOrderStatus(orderTable));
                List<OrderItemDTO> orderItemDTOS = OrderItemDTO.convertToOrderItemDTOList(orderTable.getOrderItems());
                OrderDTO orderDTO = new OrderDTO(orderTable.getOrderID(), orderTable.getLastName(), orderTable.getFirstName(), orderTable.getCheckInDate(), orderTable.getCheckOutDate(), orderTable.getOrderPrice(), orderTable.getPayment(), orderTable.getOrderStatus(), orderItemDTOS);
                orderDTOS.add(orderDTO);
            }
            return orderDTOS;
        }
        return null;
    }

    //轉去新增訂單
    @GetMapping("/hotel/insertOrder")
    public String insertOrder(HttpSession httpSession, @RequestParam("hotelID") Integer hotelID, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("hotelID", hotelID);
        return "order/insertOrder";
    }

    //找出商品
    @PostMapping("/hotel/findRoom")
    @ResponseBody
    public List<RoomDTO> findrRoom(@RequestParam(value = "hotelID") Integer hotelID, HttpSession httpSession,
                                   @RequestParam(value = "checkDate") String checkDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] dateParts = checkDate.split(" - ");
        // 預設後臺搜尋數量大於等於1
        Integer quantity = 1;
        Integer UndiscountedTotalpPrice;
        Integer totalRoomPrice;
        Integer minQuantity;
        if (dateParts.length == 2) {
            String checkInDate = convertToNewFormat(dateParts[0]);
            String checkOutDate = convertToNewFormat(dateParts[1]);
            LocalDate localDateStart = LocalDate.parse(checkInDate);
            LocalDate localDateEnd = LocalDate.parse(checkOutDate);
            //startDateAsDate :開始日期
            //adjustedEndDateAsDate:調整後的結束日期
            Date startDateAsDate = java.sql.Date.valueOf(localDateStart);
            Date adjustedEndDateAsDate = java.sql.Date.valueOf(localDateEnd.minusDays(1));
            Date adjustedCheckOutDate = java.sql.Date.valueOf(localDateEnd);
            long dateDifference = ChronoUnit.DAYS.between(localDateStart, localDateEnd.minusDays(1)) + 1;
            List<Room> rooms = roomService.findRoom(hotelService.findByHotelId(hotelID), startDateAsDate, adjustedEndDateAsDate, dateDifference);
            for (Room room : rooms) {
                UndiscountedTotalpPrice = IndexService.UndiscountedTotalpPrice(room.getProductID(), quantity, startDateAsDate, adjustedEndDateAsDate);
                totalRoomPrice = IndexService.totalRoomPrice(room.getProductID(), quantity, startDateAsDate, adjustedEndDateAsDate);
                minQuantity = IndexService.findMinimumQuantityByDate(room.getProductID(), startDateAsDate, adjustedEndDateAsDate);
                //折扣百分比
                double discountPercentage = (double) totalRoomPrice / UndiscountedTotalpPrice;
                //把總價裝進room
                room.setDiscountPercentage(discountPercentage);
                room.setPrice(totalRoomPrice);
                room.setMinQuantity(minQuantity);
            }
            List<RoomDTO> roomDTOs = RoomDTO.findRoom(rooms);
            for (RoomDTO roomDTO : roomDTOs) {
                roomDTO.setDateStart(startDateAsDate);
                roomDTO.setDateEnd(adjustedCheckOutDate);
            }
            httpSession.setAttribute("checkinDay", checkInDate);
            httpSession.setAttribute("checkoutDay", checkOutDate);
            return roomDTOs;
        }
        return null;
    }

    //送出下一步-訂單資料-1
    @PostMapping("/hotel/insterOrderStep1")
    @ResponseBody
    public String insterOrderStep1(HttpSession httpSession, @RequestBody RequestBooking requestBooking, Model model) {
        if (httpSession.getAttribute("loginId") == null) {
            return "redirect:/hotel/login";
        }
        httpSession.setAttribute("backstageRequestBooking", requestBooking);
        return "Y";
    }

    //送出下一步-訂單資料-2
    @GetMapping("/hotel/insterOrderStep2")
    public String insterOrderStep2(@RequestParam(value = "hotelID") Integer hotelID, HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        RequestBooking requestBooking = (RequestBooking) httpSession.getAttribute("backstageRequestBooking");
        Integer totalPrice = requestBooking.getTotalPrice();
        List<RequestRoom> selectedRooms = requestBooking.getSelectedRooms();
        String checkinDay = (String) httpSession.getAttribute("checkinDay");
        String checkoutDay = (String) httpSession.getAttribute("checkoutDay");
        // 設定model
        model.addAttribute("selectedRooms", selectedRooms);
        model.addAttribute("checkinDay", checkinDay);
        model.addAttribute("checkoutDay", checkoutDay);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("hotelID", hotelID);
        return "order/insertOrder2";
    }

    @PostMapping("/hotel/insterOrderStep3")
    @ResponseBody
    @Transactional
    public String insterOrderStep3(@Param(value = "email") String email,
                                   @Param(value = "lastName") String lastName,
                                   @Param(value = "firstName") String firstName,
                                   @Param(value = "tel") String tel,
                                   @Param(value = "checkinDay") String checkinDay,
                                   @Param(value = "checkoutDay") String checkoutDay,
                                   @Param(value = "totalPeople") Integer totalPeople,
                                   @Param(value = "totalPrice") Integer totalPrice,
                                   @Param(value = "paymentMethod") String paymentMethod,
                                   HttpSession httpSession, Model model) throws ParseException {

        RequestBooking requestBooking = (RequestBooking) httpSession.getAttribute("backstageRequestBooking");
        List<RequestRoom> selectedRooms = requestBooking.getSelectedRooms();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = dateFormat.format(currentDate);
        OrderTable orderTable = new OrderTable();
        Set<OrderItem> orderItems = new HashSet<>();
        IdGenerator forOrder = new IdGenerator();
        IdGenerator forOrderItem = new IdGenerator();
        Customer customer = customerService.findCustomerByEmail(email);
        if (customer == null) {
            return "emailError";
        }
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
            System.out.println(requestRoom.getQuantity() + "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
            //檢查庫存並更新數量
            Boolean updateRoomQuantity = IndexService.checkAndUpdateRoomQuantity(requestRoom.getProductID(), requestRoom.getQuantity(), startDateAsDate, adjustedEndDateAsDate);
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
            httpSession.removeAttribute("checkinDay");
            httpSession.removeAttribute("checkoutDay");
            httpSession.removeAttribute("backstageRequestBooking");
            String orderID = orderTable.getOrderID();
            return orderID;
        }
        return "Error";
    }

    //轉去進行中訂單
    @GetMapping("/hotel/activeOrders")
    public String activeOrders(@RequestParam(value = "hotelID") Integer hotelID, HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        Date currentDate = new Date();
        List<OrderTable> orderTables = orderTableService.findOrderByHotelAndStatus(currentDate, "進行中", hotelService.findByHotelId(hotelID));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("進行中");
        }
        model.addAttribute("hotelID", hotelID);
        model.addAttribute("orderTables", orderTables);
        return "order/activeOrders";
    }

    //轉去即將到來訂單
    @GetMapping("/hotel/upcomingOrders")
    public String upcomingOrders(@RequestParam(value = "hotelID") Integer hotelID, HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        // 明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Date comingDay = java.sql.Date.valueOf(tomorrow);
        List<OrderTable> orderTables = orderTableService.upcomingOrders(comingDay, hotelService.findByHotelId(hotelID));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("有效");
        }
        model.addAttribute("hotelID", hotelID);
        model.addAttribute("orderTables", orderTables);
        return "order/upcomingOrders";
    }

    //轉去訂單明細
    @GetMapping("/hotel/orderdetail")
    public String findOrderDetail(@RequestParam(value = "hotelID") Integer hotelID, @RequestParam("orderID") String orderID, HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null || orderID == null) {
            return "redirect:/hotel/login";
        }
        OrderTable orderTable = orderTableService.findOrderByOrderID_hotel(orderID,hotelService.findByHotelId(hotelID));
        orderTable.setOrderStatus(determineOrderStatus(orderTable));
        model.addAttribute("hotelID", hotelID);
        model.addAttribute("orderTable", orderTable);
        return "order/orderDetail";
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
