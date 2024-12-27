package com.sip.sipproject.service;


import com.sip.sipproject.bean.*;
import com.sip.sipproject.repository.OrderTableRepository;
import com.sip.sipproject.repository.TempOrderTableRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TempOrderTableService {

    @Autowired
    TempOrderTableRepository tempOrderTableRepository;

    public Boolean intserOrder(TempOrderTable tempOrderTable, Set<TempOrderItem> tempOrderItems) {
        if (tempOrderTable != null && tempOrderItems != null) {
            //一對多  多對一設定
            tempOrderTable.setTempOrderItems(tempOrderItems);
            for (TempOrderItem tempOrderItem : tempOrderItems) {
                tempOrderItem.setTempOrderTable(tempOrderTable);
            }
            tempOrderTableRepository.save(tempOrderTable);
            return true;
        }
        return false;
    }

    public TempOrderTable findByOrderId(String orderID) {
        Optional<TempOrderTable> optional = tempOrderTableRepository.findById(orderID);
        if (optional.isPresent()) {
            TempOrderTable tempOrderTable = optional.get();
            return tempOrderTable;
        } else {
            return null;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public OrderTable convertTempOrderToOrder(TempOrderTable tempOrderTable) {
        // 自動映射
        return modelMapper.map(tempOrderTable, OrderTable.class);
    }
    public OrderItem convertTempOrderItemToOrderItem(TempOrderItem tempOrderItem) {
        // 自動映射
        return modelMapper.map(tempOrderItem, OrderItem.class);
    }

    public boolean deleteOrder(TempOrderTable tempOrderTable) {
        try {
            tempOrderTableRepository.delete(tempOrderTable);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 每10分鐘執行一次該方法
    @Scheduled(fixedRate = 600000) // 每600000毫秒（即10分鐘）執行一次
    @Transactional
    public void checkExpiredOrders() {
        List<TempOrderTable> tempOrderTables = tempOrderTableRepository.findAll();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (TempOrderTable tempOrderTable : tempOrderTables) {
            try {
                Date orderTime = dateFormat.parse(tempOrderTable.getOrderTime());
                Date currentTime = new Date(); // 當前時間
                long timeDifferenceInMillis = currentTime.getTime() - orderTime.getTime();
                // 20 分鐘 = 20 * 60 * 1000 毫秒
                if (timeDifferenceInMillis > 20 * 60 * 1000) {
                    // 超過 20 分鐘，執行刪除操作
                    System.out.println("訂單超過 20 分鐘，準備刪除。");
                    // 調用刪除方法（假設您有刪除方法）
                    tempOrderTableRepository.delete(tempOrderTable);
                } else {
                    System.out.println("訂單尚未超過 20 分鐘。");
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
