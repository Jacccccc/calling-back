package com.example.demo.Aspect;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.entity.Role;
import com.example.demo.util.JsonData;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

import java.util.List;


@Component
@Aspect
public class AuthAspect {

    @Pointcut("@annotation(com.example.demo.annotation.RequireRole)")
    public void doNothing() {}


    /**
     * @description: AOP进行权限校验
     * @param jt
     * @param ano
     * @return: java.lang.Object
     * @author: Jac
     * @time: 2022/2/8 21:23
     **/

    @Around("doNothing() && @annotation(ano)")
    public Object doAuth(ProceedingJoinPoint jt,RequireRole ano) throws Throwable {
        if(ano.value().equals("")) return  jt.proceed();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token=request.getHeader("jwt");
        if(token==null||"".equals(token)) return JsonData.buildError("您还未登录");
        if(!JwtUtil.IsValidated(token)) JsonData.buildError("拒绝访问");
        if(JwtUtil.IsExpired(token)) return JsonData.buildError("token过期");
        Claims claims= JwtUtil.getJWT(token);
        if(claims==null) return  JsonData.buildError("拒绝访问");
        request.setAttribute("id",claims.get("id"));
        List<Role>Roles= (List<Role>) claims.get("roles");
        Object[] object=jt.getArgs();
        JSONObject jsonObject= (JSONObject) object[0];
        JSONObject user=new JSONObject();
        user.put("id",claims.get("id",Long.class));
        jsonObject.put("user",user);
        object[0]=jsonObject;
        if(hasRole(Roles,ano.value())) return jt.proceed(object);
        return  JsonData.buildError("您没有权限");
    }

    /**
     * @description:判断是否具有权限
     * @param roles
     * @param role
     * @return: boolean
     * @author: Jac
     * @time: 2022/2/8 21:22
     **/
    public boolean hasRole(List<Role> roles,String role) {
        for(Object object:roles){
            // 将list中的数据转成json字符串
            String jsonObject=JSON.toJSONString(object);
            //将json转成需要的对象
            Role r= JSONObject.parseObject(jsonObject,Role.class);
            if(role.equals(r.getRoleName())) return true;
        }
        return false;
    }
}
