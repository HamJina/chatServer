package com.example.chatserver.chat.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//핸들러
//connect로 웹소켓 연결요청이 들어왔을 때 이를 처리할 클래스
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    //thread-safe: 동시에 여러 사용자들이 몰려 들어와서 서버에 커넥트를 맺어도 문제 없는 자료구조이다.
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    //연결이 되고 난후 Set자료구조에 사용자 정보 저장
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //session: 다양한 사용자 정보가 담김
        sessions.add(session); //자료구조에 사용자 정보 담기
        System.out.println("Connected: " + session.getId());
    }

    //사용자에게 메시지를 보내는 메소드
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload(); //메시지
        System.out.println("received message : " + payload);
        for(WebSocketSession s : sessions) {
            //모든 세션(사용자)에 메시지 보내기
            if(s.isOpen()){
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    //연결이 끊기면 메모리에서 정보 삭제
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("disconnected!!");
    }

}
