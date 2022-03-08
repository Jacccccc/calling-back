package com.example.demo.vo;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String userName;
    private String mail;
    private String createTime;
    private String avatar;
    private List<Role> roles; //用户的角色
    public void setCreateTime(Object createTime) {
        if(createTime.getClass().equals(String.class))
            this.createTime =(String) createTime;
        else this.createTime = new SimpleDateFormat("MM-dd").format(createTime);
    }
}
