package com.example.demo.controller;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @RequestMapping("login")
    public JsonData login(@RequestBody User front_user,@RequestHeader("jwt") String jwt){
        if(!jwt.equals("")) return JsonData.buildError("请勿重复登录");
        return userService.login(front_user);
    }
    @PostMapping("register")
    public JsonData register(@RequestBody User front_user)
    {
         return userService.register(front_user);
    }

    @RequestMapping("userInfo")
    @RequireRole(value = "normal")
    public JsonData userInfo(@RequestBody JSONObject user)
    {
        return userService.getUserInfo(user.getObject("user",User.class).getId());
    }
}
