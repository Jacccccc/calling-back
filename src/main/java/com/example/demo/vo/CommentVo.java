package com.example.demo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/9 15:01
 **/
@Data
@NoArgsConstructor
public class CommentVo implements Serializable {
    private Long id;
    private UserVo userVo;
    private String content;
    private String createTime;
    public void setCreateTime(Object createTime) {
        if(createTime.getClass().equals(String.class))
        this.createTime =(String) createTime;
        else this.createTime = new SimpleDateFormat("MM-dd").format(createTime);
    }
}
