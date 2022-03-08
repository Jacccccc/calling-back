package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long id;
    private String userName;
    private String passWord;
    private String email;
    private Timestamp createTime;
    private Timestamp lastActive;
    private String avatar;
    private Integer status;
    private String salt;
    private List<Role> roles; //用户的角色
    private List<Post> posts; //用户的帖子
    private List<Comment> comments;  //用户发表的评论
}
