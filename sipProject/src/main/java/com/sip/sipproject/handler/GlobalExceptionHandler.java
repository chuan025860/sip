package com.sip.sipproject.handler;

import com.sip.sipproject.exception.quantityException;
import com.sip.sipproject.exception.quantityExceptionResponseBody;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(quantityException.class)
    public String roomQuantity(Model model) {
        model.addAttribute("errorMsg","商品數量不足。請重新搜尋");
        return "sip/sipBookingError";
    }

    @ExceptionHandler(quantityExceptionResponseBody.class)
    @ResponseBody
    public String roomQuantityResponseBody() {
        return "quantityError";
    }
}
