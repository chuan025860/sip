package com.sip.sipproject.security;

import com.sip.sipproject.bean.Customer;
import com.sip.sipproject.service.CustomerService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Autowired
    private CustomerService customerService;

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 生成 JWT token
    public String generateToken(Integer loginID, String customerName) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        String token = Jwts.builder()
                // claim 中的資料會被編碼進 JWT，但不會被加密，因此 token 是可以被解碼的。token 中存入的資訊應當謹慎，避免儲存敏感或機密的資料。
                .setSubject(String.valueOf(loginID))
                .claim("customerName", customerName) // 加入 customerName
                .setIssuedAt(currentDate)
                .setExpiration(expireDate) // 24小時有效期
                .signWith(key())
                .compact();
        return token;
    }
    private Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // 驗證 Token 並解碼，解析 loginId
    public String getLoginIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    // 驗證 Token 並解碼，解析 customerName
    public String getCustomerNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("customerName", String.class); //從 claims 取得 customerName
    }

    // 登出使用黑名單 後端排除Token
    public boolean invlaidate_Token(String token) {
        try {

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token 驗證失敗
            return false;
        }
    }

    // 驗證 Token 方法
    public boolean validateToken(String token) {
        try {
            //  setSigningKey(key()) 密鑰與 token 中簽名部分的密鑰不匹配，則解析失敗並丟出 JwtException
            //  parseClaimsJws  解析 token，若 token 已過期或簽名無效，也會拋出異常。
            Jwts.parser().setSigningKey(key()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token 驗證失敗
            return false;
        }
    }


}
