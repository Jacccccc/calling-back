package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMapper {

    @Select("select status from likes where target_id=#{targetId} and user_id=#{userId} and type=#{type}" )
    String getLikeStatus(Long userId,Long targetId,Integer type);

    @Insert("insert into likes(user_id,target_id,status,type) values(#{userId},#{targetId},#{status},#{type})")
    Integer insertLike(Long userId,Long targetId,Integer status,Integer type);

    @Update("update likes set status=#{status} where target_id=#{targetId} and user_id=#{userId} and type=#{type}")
    Integer updateLike(Long userId,Long targetId,Integer type,Integer status);
}
