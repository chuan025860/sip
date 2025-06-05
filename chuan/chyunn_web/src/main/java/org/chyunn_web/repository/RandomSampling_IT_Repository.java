package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.RandomSampling_IT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RandomSampling_IT_Repository extends JpaRepository<RandomSampling_IT,String> {

    @Query("SELECT r.final_property_id FROM RandomSampling_IT r WHERE r.state = false AND r.inventory_in_progress = true")
    List<String> findIdsByStateFalse();

    @Query("SELECT r.final_property_id FROM RandomSampling_IT r ")
    List<String> findIds();

    @Query("SELECT e FROM RandomSampling_IT e WHERE e.state = false AND e.inventory_in_progress = true ")
    List<RandomSampling_IT> findByRandomSampling();

    @Modifying
    @Query("DELETE FROM RandomSampling_IT")
    void deleteAllSamplingData();
}
