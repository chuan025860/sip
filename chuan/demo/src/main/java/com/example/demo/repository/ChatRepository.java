package com.example.demo.repository;

import com.example.demo.bean.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ChatRepository extends CrudRepository<ChatRoom, UUID> {
}
