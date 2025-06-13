package org.chyunn_web.repository;

import org.chyunn_web.bean.admin.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepo extends JpaRepository<Position,   Integer> {
}
