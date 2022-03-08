package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.util.JsonData;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RefreshTokenController {


    @RequestMapping("RefreshToken")
    public JsonData refreshToken(@RequestHeader("jwt")String jwt,@RequestHeader("refresh") String refresh) {
        if(!JwtUtil.IsValidated(jwt)) return JsonData.buildError("拒绝访问"); //过期的token无效，则拒绝访问
        if(!JwtUtil.IsExpired(jwt)) return JsonData.buildError("您的Token未过期"); //防止未过期用户恶意刷新token
        if(!JwtUtil.IsValidated(refresh)) return JsonData.buildError("拒绝访问");
        if(JwtUtil.IsExpired(refresh)) return JsonData.buildError("请登录"); //refresh token过期了，必须登录；
        return createNewTokens(jwt);
    }

    /*
    *@param jwt 旧的token
     */
    private JsonData createNewTokens(String jwt){
        System.out.println(jwt);
        Claims claims=JwtUtil.getJWT(jwt);
        Long id=claims.get("id",Long.class);
        List<Role> roles= (List<Role>) claims.get("roles");
        String token=JwtUtil.generateJsonWebToken(id,roles,1); //刷新token,过期时间半小时
        String refreshToken=JwtUtil.generateJsonWebToken(id,roles,0); //刷新refresh——token，过期时间一天
        Map<String,String> tokens=new LinkedHashMap<>();
        tokens.put("jwt",token);
        tokens.put("refresh",refreshToken);
        return JsonData.buildSuccess(tokens);
    }
}
