package com.example.demo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/8 23:23
 **/
@Data
@NoArgsConstructor
public class PostVo implements Serializable {
    Long id;
    String title;
    UserVo user;
    String createTime;
    public void setCreateTime(Object o){
        if(o.getClass().equals(String.class))
            this.createTime = (String) o;
        else this.createTime = new SimpleDateFormat("MM-dd").format(o);
    }

}
