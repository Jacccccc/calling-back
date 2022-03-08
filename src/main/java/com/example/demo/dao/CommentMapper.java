package com.example.demo.dao;

import com.example.demo.entity.Comment;
import com.example.demo.util.JsonData;
import com.example.demo.vo.CommentVo;
import com.example.demo.vo.UserVo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface CommentMapper {
    /*
    *查询一个用户发表的评论
     */
    @Select("select id from comment where user_id=#{userId} limit #{page*10},10")
    List<Long> findCommentListByUserId(Long userId,Long page);

    /*
     *查询一个帖子的评论的id
     */
    @Select("select id from comment where target_id=#{postId} and type=0 order by score desc,id asc limit #{page},10")
    List<Long> HotCommentOfPost(Long postId,Long page);


    @Select("select id from comment where target_id=#{postId} and type=0 order by id desc limit 10")
    List<Long> findFirstCommentPage(Long postId);

    /*
     *查询一个评论的评论
     */
    List<Comment> findCommentListByCommentId();

    /*
     *发表评论
     */
    @Insert("insert into comment(user_id,target_id,type,status,content,create_time) values(#{userId},#{targetId},#{type},#{status},#{content},#{createTime}) ")
    int addComment(Long userId, Long targetId, Integer type, Integer status, String content, Timestamp createTime);

    /*
     *删除一条评论
     * 并不是真正的删除，仅仅将评论的状态设为删除态
     */
    @Update("update comment set status=1 where id=#{id} and user_id=#{userId}")
    int deleteCommentById(Long id,Long userId);

    /*
     *拉黑一条评论
     * 将评论的状态设为拉黑态
     */
    int blockCommentById(Long id);
    /*
     查询一条评论的回复，第一次查询
     */
     @Select("select id from comment where target_id=#{commentId} and type=1 order by id desc limit 10")
     List<Long> findFirstCommentPageOfcomment(Long commentId);
    /*
       查询一条评论的回复，根据上一页最后一条评论查询
    */
     @Select("select id,content,user_id, create_time as createTime from comment where target_id=#{commentId} and id<#{lastCommentId} and type=1 order by id desc limit 10")
     @Results({
             @Result(id=true,property = "id",column = "id"),
             @Result(property = "content",column = "content"),
             @Result(property = "createTime",column = "create_time"),
             @Result(property = "userVo",column = "user_id",
                     javaType = UserVo.class,
                     one = @One(select = "com.example.demo.dao.UserMapper.findUserVoById"))
     })
     List<CommentVo> findCommentPageOfcommentByPostId(Long commentId, Long lastCommentId);

    @Select("select id,content,user_id, create_time  from comment where id=#{commentId} and type=0 ")
    @Results({
            @Result(id=true,property = "id",column = "id"),
            @Result(property = "content",column = "content"),
            @Result(property = "createTime",column = "create_time"),
            @Result(property = "userVo",column = "user_id",
                    javaType = UserVo.class,
                    one = @One(select = "com.example.demo.dao.UserMapper.findUserVoById"))
    })
     CommentVo findCommentById(Long commentId);

    @Select("select id from comment where target_id=#{postId} and id<#{lastCommentId} and type=0 order by id desc limit 10")
    List<Long> newCommentOfPost(Long postId, Long lastCommentId);



    @Select("select id from comment where id<#{lastCommentId} and type=1 order by id desc limit 10")
    List<Long> newCommentOfComment(Long lastCommentId, Long commentId);
    @Select("select user_id from comment where id = (select target_id from comment where id=#{id}")
    Long findTargetById(Long id);
}
