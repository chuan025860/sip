package org.chyunn_web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.chyunn_web.bean.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 生成 JWT token
    public String generateToken(String loginID, String userName, List<UserRole> roles) {

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        // 萃取出角色名稱的 List<String>
        List<String> roleNames = roles.stream()
                .map(r -> r.getId().getRole())
                .collect(Collectors.toList());
        for (String roleName : roleNames) {
            System.out.println(roleName+"------------------------------------------");
        }
        String token = Jwts.builder()
                // claim 中的資料會被編碼進 JWT，但不會被加密，因此 token 是可以被解碼的。token 中存入的資訊應當謹慎，避免儲存敏感或機密的資料。
                .setSubject(String.valueOf(loginID))
                .claim("userName", userName)
                .claim("roles", roleNames)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
        return token;
    }

    private Key key() {
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

    // 驗證 Token 並解碼，解析
    public String getUserNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userName", String.class); //從 claims 取得 userName
    }

    // 驗證 Token 並解碼，解析角色列表
    public List<String> getUserRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())  // 驗證簽名的密鑰
                .build()
                .parseClaimsJws(token)  // 解碼 Token
                .getBody();

        // 從 claims 中提取 roles，並將其轉換為 List<String>
        return claims.get("roles", List.class);  // roles 是一個 List<String>，因此直接使用 List.class
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

    public long getTokenRemainingTime(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())  // 確保用相同的密鑰來解析
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expirationDate = claims.getExpiration();  // 取得過期時間
        long currentTime = System.currentTimeMillis(); // 當前時間
        long remainingTime = expirationDate.getTime() - currentTime; // 計算剩餘時間

        return Math.max(remainingTime, 0);  // 確保不會是負數
    }

}
