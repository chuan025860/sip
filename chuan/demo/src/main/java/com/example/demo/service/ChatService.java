package com.example.demo.service;

import com.example.demo.bean.ChatRoom;
import com.example.demo.bean.Customer;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String CHAT_ROOM_PREFIX = "chatroom:"; // 用于标识聊天室的键前缀

    // 存储聊天室到 Redis
    public void saveChatRoom(ChatRoom chatRoom) {
        String roomID = chatRoom.getRoomID().toString();
        redisTemplate.opsForValue().set(CHAT_ROOM_PREFIX + roomID, chatRoom);
    }

    // 从 Redis 获取聊天室
    public ChatRoom getChatRoom(UUID roomID) {
        return (ChatRoom) redisTemplate.opsForValue().get(CHAT_ROOM_PREFIX + roomID.toString());
    }
//    @Autowired
//    ChatRepository chatRepository;
//
//    public boolean insert(ChatRoom chatRoom) {
//        try {
//            chatRepository.save(chatRoom);
//            return true; // 成功保存時回傳 true
//        } catch (Exception e) {
//            e.printStackTrace(); // 可選：處理異常並記錄錯誤
//            return false; // 發生錯誤時回傳 false
//        }
//    }
//
//    public ChatRoom findByID(UUID loginID) {
//        Optional<ChatRoom> optional = chatRepository.findById(loginID);
//        if (optional.isPresent()) {
//            ChatRoom chatRoom = optional.get();
//            return chatRoom;
//        } else {
//            return null;
//        }
//    }
}
