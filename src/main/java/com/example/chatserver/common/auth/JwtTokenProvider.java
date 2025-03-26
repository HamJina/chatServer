package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

//토큰 생성 필터
@Component //싱글톤 객체로 생성
public class JwtTokenProvider {

    private final String secretKey;
    private final int expiration;
    private Key SECRET_KEY;


    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey,@Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        //암호화 알고리즘을 사용하여 시크릿키 암호화하기
        SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    //토큰 생성하기
    public String createToken(String email, String role) {
        //payload -> email, role값 활용
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date(); //현재시간
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) //발행시간
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) //만료일자(밀리초 단위로 셋팅)
                .signWith(SECRET_KEY)
                .compact();
        return token;
    }
}
