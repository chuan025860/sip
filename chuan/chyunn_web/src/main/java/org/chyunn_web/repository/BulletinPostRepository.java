package org.chyunn_web.repository;


import org.chyunn_web.bean.Resource.BulletinPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BulletinPostRepository extends JpaRepository<BulletinPost,Integer> {
    List<BulletinPost> findTop5ByOrderByPublishTimeDesc();

    @Query("SELECT b.id FROM BulletinPost b")
    List<Integer> findAllPostIds();

    @Query("SELECT bp FROM BulletinPost bp WHERE bp.endTime >= CURRENT_TIMESTAMP ORDER BY bp.publishTime DESC")
    Page<BulletinPost> findAllOrderByCreatedAtDesc(Pageable pageable);
}
