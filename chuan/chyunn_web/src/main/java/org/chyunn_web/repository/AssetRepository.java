package org.chyunn_web.repository;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.Inventory_Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssetRepository extends JpaRepository<Inventory_Equipment,String> {
    @Query("SELECT i FROM Inventory_Equipment i " +
            "ORDER BY CASE i.location " +
            "    WHEN '204資訊' THEN 1 " +
            "    WHEN '217資訊機房' THEN 2 " +
            "    WHEN '102茶水室' THEN 3 " +
            "    WHEN '103會議室' THEN 4 " +
            "    WHEN '104控制室' THEN 5 " +
            "    WHEN '105洽談室' THEN 6 " +
            "    WHEN '106營業' THEN 7 " +
            "    WHEN '108茶水室' THEN 8 " +
            "    WHEN '110儲藏室' THEN 9 " +
            "    WHEN '114人事總務' THEN 10 " +
            "    WHEN '115總經理' THEN 11 " +
            "    WHEN '116會議室' THEN 12 " +
            "    WHEN '117財會' THEN 13 " +
            "    WHEN '118檔案室' THEN 14 " +
            "    WHEN '201' THEN 15 " +
            "    WHEN '202洽談室' THEN 16 " +
            "    WHEN '203運動室' THEN 17 " +
            "    WHEN '206哺乳室' THEN 18 " +
            "    WHEN '207茶水室' THEN 19 " +
            "    WHEN '208文書室' THEN 20 " +
            "    WHEN '209共享' THEN 21 " +
            "    WHEN '213董事長' THEN 22 " +
            "    WHEN '216稽核' THEN 23 " +
            "    WHEN '218會議室' THEN 24 " +
            "    WHEN '安明-A棟1樓' THEN 25 " +
            "    WHEN '安明-A棟2樓' THEN 26 " +
            "    WHEN '安明-B棟1樓' THEN 27 " +
            "    WHEN '安明-B棟2樓' THEN 28 " +
            "    WHEN '安明-土資廠' THEN 29 " +
            "    WHEN '安明-1F機房' THEN 30 " +
            "    WHEN '安明-司機休息室' THEN 31 " +
            "    WHEN '安明-守衛室' THEN 32 " +
            "    WHEN '安明-會議室' THEN 33 " +
            "    WHEN '安明-停車場' THEN 34 " +
            "    WHEN '安明-中庭' THEN 35 " +
            "    WHEN '安明-外勞宿舍' THEN 36 " +
            "    WHEN '安明-保修廠' THEN 37 " +
            "    WHEN '安明-倉庫' THEN 38 " +
            "    WHEN '高雄_辦公室1樓' THEN 39 " +
            "    WHEN '高雄_辦公室2樓' THEN 40 " +
            "    WHEN '顯宮段' THEN 41 " +
            "    ELSE 999 " +  // 未匹配的資料會被排到最後
            "END")
    Page<Inventory_Equipment> findAll(Pageable pageable);

}
