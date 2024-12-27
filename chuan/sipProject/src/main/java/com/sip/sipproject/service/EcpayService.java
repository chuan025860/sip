package com.sip.sipproject.service;


import com.sip.sipproject.dto.RequestBooking;
import com.sip.sipproject.dto.RequestRoom;
import com.sip.sipproject.integration.AllInOne;
import com.sip.sipproject.integration.domain.AioCheckOutALL;
import com.sip.sipproject.integration.ecpayOperator.EcpayFunction;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EcpayService {

    public String ecpayCheckForm(String orderID, Integer price, List<RequestRoom> selectedRooms) {

        AioCheckOutALL obj = new AioCheckOutALL();
        AllInOne all = new AllInOne("");
        obj.setMerchantID("3002599");
        obj.setMerchantTradeNo(orderID);
        SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formattedDate = date.format(new Date());  // 使用 format 方法來格式化當前日期
        obj.setMerchantTradeDate(formattedDate);  // 將格式化後的字串傳遞給 setMerchantTradeDate
        obj.setTotalAmount(String.valueOf(price));
        obj.setTradeDesc("SIP金流連接");
        // 商品名稱，將 selectedRooms 裡的房間名稱以 # 分隔
        StringBuilder itemNameBuilder = new StringBuilder();
        for (int i = 0; i < selectedRooms.size(); i++) {
            RequestRoom room = selectedRooms.get(i);
            // 假設每個 RequestRoom 物件有一個 getRoomName() 方法返回房間名稱
            itemNameBuilder.append(room.getProductName());

            // 如果不是最後一項，添加 # 符號分隔
            if (i < selectedRooms.size() - 1) {
                itemNameBuilder.append("#");
            }
        }
        // 商品名稱設定到 obj 中
        obj.setItemName(itemNameBuilder.toString());
        // 交易結果回傳網址，只接受 https 開頭的網站，可以使用 ngrok(https外掛)
        obj.setReturnURL("https://a228-211-23-244-90.ngrok-free.app/sip/sipIndex/ecPayCheckMacValue");
        obj.setNeedExtraPaidInfo("N");
        // 商店轉跳網址 (Optional)
        obj.setClientBackURL("http://localhost:8080/sip/sipIndex/ecPayOrderResultURL");
        obj.setOrderResultURL("http://localhost:8080/sip/sipIndex/ecPayOrderResultURL");
        String form = all.aioCheckOut(obj, null);
        return form;
    }
}