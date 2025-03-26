package com.example.chatserver.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//로그인 요청 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginReqDto {
    private String email;
    private String password;
}
