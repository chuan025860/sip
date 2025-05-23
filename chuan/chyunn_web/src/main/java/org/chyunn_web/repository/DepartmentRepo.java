package org.chyunn_web.repository;

import org.chyunn_web.bean.admin.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<Department, Integer> {
    @Query("""
              SELECT DISTINCT d
              FROM Department d
                LEFT JOIN FETCH d.subUnits su
                LEFT JOIN FETCH su.subSubUnits ssu
                LEFT JOIN FETCH ssu.positions p3
                LEFT JOIN FETCH su.positions p2
                LEFT JOIN FETCH d.positions p1
              ORDER BY d.id
            """)
    List<Department> findAllWithTree();
}
