package com.example.demo.chat;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint(value = "/chat/{roomID}/{senderEmail}")

public class ChatWebSocketHandler {

    // 儲存 WebSocket 會話
    private static final Map<String, Session> webSocketClientMap  = new ConcurrentHashMap<>();


    // 當 WebSocket 連接開啟時
    @OnOpen
    public void onOpen(Session session, @PathParam("senderEmail") String senderEmail,@PathParam("roomID")String roomID) throws IOException {
        System.out.println("New connection opened: " + session.getId() + " with roomID: " + senderEmail );
        System.out.println(roomID);
        Session oldSession = webSocketClientMap.get(senderEmail);
        if(oldSession==null){
            webSocketClientMap.put(senderEmail, session);
            System.out.println(senderEmail + "加入webSocket " + webSocketClientMap.size());
        }else {
            System.out.println(senderEmail+"已登錄");
        }

    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("senderEmail") String senderEmail) {
        System.out.println("Message received: " + message);
        System.out.println(senderEmail);
        // 解析訊息為 Message 物件
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Message messageObj = objectMapper.readValue(message, Message.class);

            // 處理來自指定 Email 的訊息
            String receiveEmail = messageObj.getReceiveEmail();
            System.out.println("Received message from: " + senderEmail + " to phone: " + receiveEmail);
            // 根據接收者的 email 查找對應的 session
            Session targetSession = webSocketClientMap.get(receiveEmail);
            if (targetSession != null) {
                // 發送訊息到接收者
                targetSession.getBasicRemote().sendText(message);
                System.out.println("Message sent to: " + receiveEmail);
            } else {
                System.out.println("No session found for phone: " + receiveEmail);
                System.out.println("未上線");
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 當 WebSocket 連接關閉時
    @OnClose
    public void onClose(Session session) {
        // 清除會話
        webSocketClientMap.values().remove(session);
        System.out.println("Connection closed: " + session.getId());
    }

    // 當 WebSocket 發生錯誤時
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

}
