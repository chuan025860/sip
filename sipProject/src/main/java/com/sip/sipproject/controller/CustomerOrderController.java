package com.sip.sipproject.controller;

import com.sip.sipproject.bean.OrderItem;
import com.sip.sipproject.bean.OrderTable;
import com.sip.sipproject.dto.OrderDTO;
import com.sip.sipproject.dto.OrderItemDTO;
import com.sip.sipproject.dto.RequestOrder;
import com.sip.sipproject.exception.quantityExceptionResponseBody;
import com.sip.sipproject.security.JwtTokenProvider;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CustomerOrderController {
    @Autowired
    CustomerService customerService;
    @Autowired
    OrderTableService orderTableService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RoomQuantityByDateService roomQuantityByDateService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    //全域Model
    @ModelAttribute
    public void addAttributes(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 从 token 中提取用户名并添加到模型
            String customerLoginName = jwtTokenProvider.getCustomerNameFromToken(token);
            Integer customerLoginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
            model.addAttribute("customerLoginName", customerLoginName);
            model.addAttribute("customerLoginId", customerLoginId);
        }
    }

    //轉去過期訂單
    @GetMapping("/customer/index/expiredOrders")
    public String expiredOrders(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        Date currentDate = new Date();
        List<OrderTable> orderTables = orderTableService.findOrderByCustomerAndStatus(currentDate, "過期", customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("過期");
        }
        model.addAttribute("orderTables", orderTables);
        return "customerOrder/expiredOrders";
    }

    //過期訂單-搜尋過期訂單
    @PostMapping("/customer/index/expired/selectorder")
    @ResponseBody
    public List<OrderDTO> selectExpiredOrder(@RequestBody RequestOrder requestOrder, @CookieValue(value = "Authorization", required = false) String token) {
        String[] dateParts = requestOrder.getSelectedDate().split(" - ");
        if (dateParts.length == 2) {
            String startDate = convertToNewFormat(dateParts[0]);
            String endDate = convertToNewFormat(dateParts[1]);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date checkInDate = dateFormat.parse(startDate);
                Date checkOutDate = dateFormat.parse(endDate);
                List<OrderTable> orderTables = orderTableService.findExpiredOrderDateByCustomer(checkInDate, checkOutDate, customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
                // 建立一個用於儲存訂單和訂單項目的DTO列表
                List<OrderDTO> orderDTOS = new ArrayList<>();
                for (OrderTable orderTable : orderTables) {
                    orderTable.setOrderStatus("過期");
                    List<OrderItemDTO> orderItemDTOS = OrderItemDTO.convertToOrderItemDTOList(orderTable.getOrderItems());
                    OrderDTO orderDTO = new OrderDTO(orderTable.getOrderID(), orderTable.getLastName(), orderTable.getFirstName(), orderTable.getCheckInDate(), orderTable.getCheckOutDate(), orderTable.getOrderPrice(), orderTable.getPayment(), orderTable.getOrderStatus(), orderItemDTOS);
                    orderDTOS.add(orderDTO);
                }
                return orderDTOS;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    //轉去進行中訂單
    @GetMapping("/customer/index/activeOrders")
    public String activeOrders(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        Date currentDate = new Date();
        List<OrderTable> orderTables = orderTableService.findOrderByCustomerAndStatus(currentDate, "進行中", customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("進行中");
        }
        model.addAttribute("orderTables", orderTables);
        return "customerOrder/activeOrders";
    }

    //轉去即將到來訂單
    @GetMapping("/customer/index/upcomingOrders")
    public String upcomingOrders(Model model, @CookieValue(value = "Authorization", required = false) String token) {

        // 明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Date comingDay = java.sql.Date.valueOf(tomorrow);
        List<OrderTable> orderTables = orderTableService.upcomingOrdersByCustomer(comingDay, customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("有效");
        }
        model.addAttribute("orderTables", orderTables);
        return "customerOrder/upcomingOrders";
    }

    //轉去取消的訂單
    @GetMapping("/customer/index/findCanceledOrders")
    public String findCanceledOrders(Model model, @CookieValue(value = "Authorization", required = false) String token) {
        Date currentDate = new Date();
        List<OrderTable> orderTables = orderTableService.findOrderByCustomerAndStatus(currentDate, "canceled", customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        for (OrderTable orderTable : orderTables) {
            orderTable.setOrderStatus("取消");
        }
        model.addAttribute("orderTables", orderTables);
        return "customerOrder/canceledOrders";
    }

    //轉去訂單明細
    @GetMapping("/customer/index/orderdetail")
    public String findOrderDetail(@CookieValue(value = "Authorization", required = false) String token, @RequestParam("orderID") String orderID, Model model) {

        Date currentDate = new Date(); // 當前日期
        OrderTable orderTable = orderTableService.findOrderByOrderID_customer(orderID,customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        if (orderTable.getCheckInDate().after(currentDate) && !orderTable.getOrderStatus().equals("canceled")) {
            // 如果入住日期在當前日期之後，視為未來訂單，轉去可更新頁面
            orderTable.setOrderStatus("有效");
            model.addAttribute("orderTable", orderTable);
            return "customerOrder/upcomingOrderDetail";
        } else {
            // 轉去無法更新頁面
            orderTable.setOrderStatus(determineOrderStatus(orderTable));
            model.addAttribute("orderTable", orderTable);
            return "customerOrder/orderDetail";
        }
    }

    //轉去更新頁面
    @PostMapping("/customer/index/intoUpdateOrder")
    public String updateOrderDetail(@CookieValue(value = "Authorization", required = false) String token, @RequestParam("orderID") String orderID, Model model) {
        OrderTable orderTable = orderTableService.findOrderByOrderID_customer(orderID,customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        model.addAttribute("orderTable", orderTable);
        return "customerOrder/updateOrderDetail";
    }

    //更新訂單
    @PutMapping("/customer/index/updateOrder")
    @ResponseBody
    public String updateOrderDetail_2(@CookieValue(value = "Authorization", required = false) String token,
                                      @RequestParam("firstName") String firstName,
                                      @RequestParam("lastName") String lastName,
                                      @RequestParam("orderID") String orderID,
                                      @RequestParam("email") String email
    ) {
        OrderTable orderTable = orderTableService.findOrderByOrderID_customer(orderID,customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        orderTable.setFirstName(firstName);
        orderTable.setLastName(lastName);
        orderTable.setEmail(email);
        OrderTable newOrderTable = orderTableService.updateOrder(orderTable);
        if (newOrderTable != null) {
            return newOrderTable.getOrderID();
        }
        return "N";
    }

    //取消訂單
    @PutMapping("/customer/index/canceledOrder")
    @ResponseBody
    @Transactional
    public String canceledOrder(@CookieValue(value = "Authorization", required = false) String token,
                                @RequestParam("orderID") String orderID
    ) {
        OrderTable orderTable = orderTableService.findOrderByOrderID_customer(orderID,customerService.findByID(Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token))));
        for (OrderItem orderItem : orderTable.getOrderItems()) {
            // 還原房間數量
            boolean roomsRestored = roomQuantityByDateService.restoreRoomQuantity(orderItem.getRoom(), orderItem.getQuantity(), orderTable.getCheckInDate(), orderTable.getCheckOutDate());
            if (!roomsRestored) {
                throw new quantityExceptionResponseBody("quantityError"); // 觸發事務回滾
            }
        }
        //設定訂單狀態 -取消狀態
        orderTable.setOrderStatus("canceled");
        OrderTable newOrderTable = orderTableService.updateOrder(orderTable);
        if (newOrderTable != null) {
            return newOrderTable.getOrderID();
        } else {
            throw new RuntimeException("quantityError"); // 觸發事務回滾
        }
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


}
