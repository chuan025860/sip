package org.chyunn_web.repository;

import org.chyunn_web.bean.RandomSampling_All;
import org.chyunn_web.bean.RandomSampling_IT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RandomSampling_All_Repository extends JpaRepository<RandomSampling_All,String> {

    @Query("SELECT r.asset_id FROM RandomSampling_All r WHERE r.state = false AND r.inventory_in_progress = true")
    List<String> findIdsByStateFalse();

    @Query("SELECT r.asset_id FROM RandomSampling_All r ")
    List<String> findIds();

    @Query("SELECT e FROM RandomSampling_All e WHERE e.state = false AND e.inventory_in_progress = true ")
    List<RandomSampling_All> findByRandomSampling();

    @Modifying
    @Query("DELETE FROM RandomSampling_All")
    void deleteAllSamplingData();
}
