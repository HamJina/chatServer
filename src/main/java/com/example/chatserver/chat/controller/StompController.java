package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompController {

    //메시지 전달을 위한 객체
    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;

    //방법1. MessageMapping(수신)과 SendTo(topic에 메시지 전달)한꺼번에 처리
    /*@MessageMapping("/{roomId}") //클라이언트에서 특정 publish/roomId형태로 메시지를 발행시 MessageMapping수신
    @SendTo("/topic/{roomId}") //해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 메시지 전송
    //@DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용
    public String sendMessage(@DestinationVariable Long roomId, String message){
        System.out.println(message);

        return message;
    }*/

    //방법2. MessageMapping 어노테이션만 활용(좀 더 유연한 방법)
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageReqDto chatMessageReqDto){
        System.out.println(chatMessageReqDto.getMessage());
        chatService.sendMessage(roomId, chatMessageReqDto); //메시지 데이터베이스에 저장하기
        //구족자들에게 메시지 발행
        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReqDto); //메시지 발행
    }
}
