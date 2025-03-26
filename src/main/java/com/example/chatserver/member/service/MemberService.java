package com.example.chatserver.member.service;

import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberListResDto;
import com.example.chatserver.member.dto.MemberLoginReqDto;
import com.example.chatserver.member.dto.MemberSaveReqDto;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    public Member create(MemberSaveReqDto memberSaveReqDto) {
        //이미 가입되어 있는 이메일 인증
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }

        //Builder를 사용하여 member엔티티 조립하기
        Member newMember = Member.builder()
                .name(memberSaveReqDto.getName())
                .email(memberSaveReqDto.getEmail())
                .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
                .build();
        Member member = memberRepository.save(newMember);
        return member;
    }

    //로그인
    public Member login(MemberLoginReqDto memberLoginReqDto) {
        //이메일을 이용하여 멤버 조회하기
        Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));

        //요청으로 들어온 이메일과 비밀번호가 일치하는지 확인
        if(!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())) {
            //비밀번호가 일치하지 않는 경우 예외 발생
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }


    public List<MemberListResDto> findAll() {
        List<Member> members = memberRepository.findAll();

        //entity -> dto
        List<MemberListResDto> memberListResDtos = new ArrayList<>();
        for (Member m : members) {
            MemberListResDto memberListResDto = new MemberListResDto();
            memberListResDto.setId(m.getId());
            memberListResDto.setEmail(m.getEmail());
            memberListResDto.setName(m.getName());
            memberListResDtos.add(memberListResDto);
        }
        return memberListResDtos;
    }
}
