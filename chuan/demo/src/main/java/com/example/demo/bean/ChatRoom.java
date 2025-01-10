package com.example.demo.bean;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;
@Data
@Entity
@Table(name = "Chatroom")
public class ChatRoom  {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roomID;
    private String user1;
    private String user2;
}
