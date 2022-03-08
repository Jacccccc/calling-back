package com.example.demo.dao;

import com.example.demo.entity.User;
import com.example.demo.vo.UserVo;
import com.example.demo.vo.UserInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;


@CacheNamespace()
@Repository
public interface UserMapper {

    @Update("update user set avatar=#{avatar} where id=#{id}")
    int updateAvatar(Long id,String avatar);

    /*
     *根据id查询用户
     */
    @Select("select * from user where id=#{id}")
    User findUserById(@Param("id") Long id);

    /*
     *根据id查询用户
     */
    @Select("select id,username,avatar from user where id=#{id}")
    UserVo findUserVoById(@Param("id") Long id);
    /*
     *根据id查询用户
     */
    @Select("select * from user where id=#{id}")
    UserInfo findUserInfoById(@Param("id") Long id);
    /*
     *根据邮箱查询用户
     */
    @Select("select * from user where email=#{email}")
    User findUserByMail(@Param("email") String email);

    @Insert("insert into user(username,password,create_time,email,last_active,avatar,status,salt)" +
            "values(#{userName},#{passWord},#{createTime},#{email},#{lastActive},#{avatar},#{status},#{salt})")
    int insertUser(User user);
    /*
     *修改用户信息
     */
    int modifyUserById(Long id);

    /*
     *删除用户
     */
    int deleteUserById(Long id);

    /*
    *拉黑用户
     */
    int blockUserById(Long id);
}
