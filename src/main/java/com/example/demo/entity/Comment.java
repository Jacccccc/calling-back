package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
    private Long id;
    private User user;
    private List<Comment> comments;
    private Integer status;
    private Integer type;
    private String content;
    private Timestamp createTime;
}
