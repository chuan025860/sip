package org.chyunn_web.repository;

import org.chyunn_web.bean.Resource.EditorImageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EditorImageUsageRepository extends JpaRepository<EditorImageUsage,Integer> {
    @Query("SELECT e.imagePath FROM EditorImageUsage e")
    List<String> findAllImagePaths();
}
