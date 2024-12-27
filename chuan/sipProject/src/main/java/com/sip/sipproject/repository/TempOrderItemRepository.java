package com.sip.sipproject.repository;

import com.sip.sipproject.bean.TempOrderItem;
import com.sip.sipproject.bean.TempOrderTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TempOrderItemRepository extends JpaRepository<TempOrderItem, String> {
}
