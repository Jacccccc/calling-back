package com.example.demo.util;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import io.jsonwebtoken.*;

import java.util.Date;
import java.util.List;

/**
 * jwt工具类封装
 * @author asus
 *
 */
public class JwtUtil{


    //三十分钟过期
    private static final long EXPIRITION = 1000*60*30;
    //refresh token 一天过期
    private static final long REFRESH_EXPIRITION=1000*60*60*24;
    private static final String SECRET_KEY = "calling";

    /**
     * 加密生成token
     * @param id
     * @param roles
     * @param pattern
     * @return
     */
    public static String generateJsonWebToken(Long id, List<Role> roles,int pattern){
       long time=pattern==1?EXPIRITION:REFRESH_EXPIRITION;
        return Jwts.builder()
                .setHeaderParam("typ","jwt")
                .setHeaderParam("alg","HS256")
                .claim("id",id)
                .claim("roles",roles)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解密token获取用户信息
     * @param token
     * @return
     */
    public static Claims getJWT(String token){
        try {
            final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return claims;
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
        catch (UnsupportedJwtException u)
        {
            return null;
        }

    }
    public static boolean IsExpired(String token)
    {
        try {
            final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
        catch (UnsupportedJwtException u)
        {
            return false;
        }
    }
    public static boolean IsValidated(String token)
    {
        try {
            final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return true;
        } catch (ExpiredJwtException e) {
            return true;
        }
        catch (UnsupportedJwtException u)
        {
            return false;
        }
    }
}
