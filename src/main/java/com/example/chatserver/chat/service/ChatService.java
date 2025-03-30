package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    //메시지 저장하기
    public void sendMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        //보낸사람조회(senderEmail을 통해서 사용자 조회하기)
        Member sender = memberRepository.findByEmail(chatMessageReqDto.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        //메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        //사용자별로 읽음여부 저장(해당 chatRoom에 있는 사용자들을 조회해서 메시지를 읽었는지 안읽었는지 여부를 readStatus 데이터베이스에 저장)
        //발송자를 제외한 나머지는 읽지 않았다. 발송자는 메시지를 보내자마자 읽었다.
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant c : chatParticipants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender)) //참여자가 메시지를 발생한 사람이라면 읽음 여부 true로 저장
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    //그룹 채팅방 개설
    public void createGroupRoom(String roomName) {
        //채팅방을 만드는 사람은 참여자로 추가되어야 한다. chatRoom과 chatParticipant를 생성한다.

        //SecurityContext에서 멤버 찾아오기
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        //채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);

        //채팅참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    //그룹 채팅방 목록 조회
    public List<ChatRoomListResDto> getGroupChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y"); //그룹 채팅만 조회

        //entity -> dto
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for (ChatRoom c : chatRooms) {
            ChatRoomListResDto dto = ChatRoomListResDto
                    .builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    //그룹 채팅방 참여
    public void addParticipantToGroupChat(Long roomId) {
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        //member 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        //이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if(!participant.isPresent()) {
            addParticipantToRoom(chatRoom, member);
        }

    }

    //ChatParticipant 객체생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
}
