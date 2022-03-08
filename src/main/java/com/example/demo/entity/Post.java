package com.example.demo.entity;

import com.example.demo.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post implements Serializable {
    private Long id;
    private Timestamp createTime;
    private String content;
    private UserVo user;
    private Integer status;
    private String title;
    private Double score;
    private Timestamp modifyTime;
}
