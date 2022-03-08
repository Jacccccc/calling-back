package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.JsonData;
import com.example.demo.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface UserService {


    JsonData login(User user);
    JsonData register(User user);
    JsonData getUserInfo(Long id);

    JsonData getUserVoByUserId(Long userId);
}
