package com.example.demo.service;

import com.example.demo.dao.RoleMapper;
import com.example.demo.dao.UserMapper;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.JsonData;
import com.example.demo.util.JwtUtil;
import com.example.demo.vo.UserInfo;
import com.example.demo.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * @description:通过前端传来的用户进行登录
     * @param front_user
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:25
     **/
    @Override
    public JsonData login(User front_user) {
        User user = userMapper.findUserByMail(front_user.getEmail());
        if(user==null) return JsonData.buildError("用户不存在");
        String pass=CommonUtil.MD5(front_user.getPassWord()+user.getSalt());
        if(!pass.equals(user.getPassWord())) return  JsonData.buildError("密码错误");
        user.setRoles(roleMapper.findRoleListByUserId(user.getId()));
        String token= JwtUtil.generateJsonWebToken(user.getId(),user.getRoles(),1);
        String refreshToken=JwtUtil.generateJsonWebToken(user.getId(),user.getRoles(),0);
        Map<String,Object> Data = new LinkedHashMap<>();
        String maxRole="normal";
        List<Role> roles=user.getRoles();
        for (Role role : roles) {
            if (role.getRoleName().equals("root")) {
                maxRole = "root";
                break;
            }
            if(role.getRoleName().equals("editor")) maxRole = "editor";
        }
        Data.put("maxRole",maxRole);
        Data.put("jwt",token);
        Data.put("refresh",refreshToken);
        Data.put("userName",user.getUserName());
        Data.put("avatar",user.getAvatar());
        return JsonData.buildSuccess(Data);
    }

    /**
     * @description: 注册
     * @param user
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:26
     **/
    @Transactional
    @Override
    public JsonData register(User user) {

        if(userMapper.findUserByMail(user.getEmail())!=null) return JsonData.buildError("账号已经被注册");
        user.setCreateTime( new Timestamp(System.currentTimeMillis()));
        user.setSalt(new Date().toString());
        user.setPassWord(CommonUtil.MD5(user.getPassWord()+user.getSalt()));
        userMapper.insertUser(user);
        user=userMapper.findUserByMail(user.getEmail());
        userMapper.updateAvatar(user.getId(),CommonUtil.MD5(String.valueOf(user.getId())));
        roleMapper.insertUserRole(user.getId(),3L);
        return JsonData.buildSuccess("注册成功！");
    }

    /**
     * @description:获取用户的信息
     * @param id
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:26
     **/
    @Override
    public JsonData getUserInfo(Long id) {
        UserInfo user=userMapper.findUserInfoById(id);
        if (user==null) return JsonData.buildError("获取失败");
         user.setRoles(roleMapper.findRoleListByUserId(id));
        return JsonData.buildSuccess(user);
    }

    @Override
    public JsonData getUserVoByUserId(Long userId) {
        return JsonData.buildSuccess(getUserVo(userId));
    }
    private UserVo getUserVo(Long userId){
        UserVo userVo= (UserVo) redisTemplate.opsForValue().get("userId:");
        if (userVo==null) userVo=userMapper.findUserVoById(userId);
        return userVo;
    }
}
