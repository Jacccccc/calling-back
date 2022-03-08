package com.example.demo.dao;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.vo.PostVo;
import com.example.demo.vo.UserVo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PostMapper {
    /*
    *查询用户的发帖
    */
    @Select("select id from post where user_id=#{id}")
    List<Long> findPostListByUserId(Long id);

    @Select("select id,title,create_time as createTime,user_id from post where id=#{id}")
    @Results({
            @Result(id=true,property = "id",column = "id"),
            @Result(property = "title",column = "title"),
            @Result(property = "user",column = "user_id",
                    javaType = UserVo.class,
                    one = @One(select = "com.example.demo.dao.UserMapper.findUserVoById"))
    })
    PostVo findPostVoById(Long id);
    /*
     *查找帖最新10条贴子
     */
    @Select("select id from post where status=1 order by id desc limit 10")
    List<Long> findFirstNewPostListByPage();
    /*
     *查找10条贴子，以id开始的最新十条
     * @param id 分页查询点击下一页时，传入的上一页的最后的id，因为后插入的帖子总是最新的
     */
    @Select("select id from post where id<#{id} and status=1 order by id desc limit 10")
    List<Long> findNewPostListByPage(Long id);
    /*
     *发帖
     */
    @Insert("insert into post(create_time,content,user_id,title) values(#{createTime},#{content},#{user.id},#{title})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int addPost(Post post);

    /*
    *删帖
     */
    @Update("update post set status=3 where id=#{id}")
    int deletePostById(Long id);

    /*
    *修改帖子内容
     */
    @Update("update post set content=#{content},modify_time=#{modifyTime} where id=#{id}")
    int modifyPostById(Long id, String content, Timestamp modifyTime);

    /*
    *审贴并且通过
     */
    @Update("update post set status=1 where id=#{id}")
    int reviewPostAndPass(Long id);

    /*
     *审贴并且不通过
     */
    @Update("update post set status=2 where id=#{id}")
    int reviewPostAndNotPass(Long id);

    @Select("select content from post where id=#{id}")
    String findPostById(Long id);

    @Update("update post set score=#{score}+score where id=#{id}")
    int updateScoreById(Long score,Long id);

    @Select("select id from post where status =1 order by score desc limit #{count},10")
    List<Long> findHotPagePost(Long count);

    @Select("select count(1) from likes where target_id=#{id} and type=1")
    int getLikeCount(Long id);
    @Update("update post set status=2 where id={i}")
    int updateStatusById(Long id, int i);
    @Select("select id from post where target_id<#{id} and status=0 limit 10")
    List<Long> findPostToPass(Long id);
}
