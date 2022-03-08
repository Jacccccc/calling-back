package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.util.JsonData;
import com.example.demo.vo.PostVo;

import java.util.List;

public interface PostService {

    void  flushScore();
    PostVo findPostVoById(Long id);
    //按最新顺序查找一页帖子
    JsonData newPostsByPage(Long postId);
    //按最热顺序查找一页帖子
    JsonData hotPostsByPage(Long page);
    //帖子详情
    JsonData PostContent(Long postId);
    //发帖
    JsonData doPost(Post post, List<String> tags);
    //删帖
    JsonData deletePostById(Long postId);
    //审贴并通过
    JsonData passPostByPostId(Long postId);
    //分页，获取待审核的帖子
    JsonData PostToReviewByPage(Long lastId);
    //获得用户的所有帖子按分页
    JsonData UserPostsByPage(Long userId);
    //拉黑帖子
    JsonData blockPostById(Long postId);
    //点赞
    JsonData like(Long userId,Long postId);
    //评论
    JsonData commentPost(Long userId,Long postId,String content);

    JsonData getLikeState(Long postId, Long userId);

    JsonData getPostByIds(List ids);

    JsonData getLikeCount(Long postId);

    void flushLikes(String pattern);
}
