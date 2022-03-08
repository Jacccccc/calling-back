package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.entity.User;
import com.example.demo.service.CommentService;
import com.example.demo.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("comment")
public class CommentController {
    @Autowired
    CommentService commentService;

    @RequestMapping("post/hot")
    public JsonData getPostHotComment(@RequestBody JSONObject object){
        Long postId=object.getLong("id");
        Long page=object.getLong("page");
        return  commentService.getHotPostComments(postId,page);
    }

    @RequestMapping("post/new")
    public JsonData getPostNewComment(@RequestBody JSONObject object){
        Long postId=object.getLong("id");
        Long lastCommentId=object.getLong("lastCommentId");
        return  commentService.getNewPostComments(postId,lastCommentId);
    }


    @RequestMapping("comment/new")
    public JsonData getNewCommentOfComment(@RequestBody JSONObject object){
        Long commentId=object.getLong("id");
        Long lastCommentId=object.getLong("lastCommentId");
        return  commentService.getNewCommentComments(commentId,lastCommentId);
    }

    @RequestMapping("likeState")
    @RequireRole("normal")
    public JsonData getLikeState(@RequestBody JSONObject object){
        Long commentId=object.getLong("id");
        Long userId=object.getObject("user",User.class).getId();
        return  commentService.likeState(userId,commentId);
    }

    @RequestMapping("like")
    @RequireRole("normal")
    public JsonData Like(@RequestBody JSONObject object){
        Long commentId=object.getLong("id");
        Long userId=object.getObject("user",User.class).getId();
        return  commentService.like(userId,commentId);
    }

    @RequestMapping("likeCount")
    @RequireRole("normal")
    public JsonData getLikeCount(@RequestBody JSONObject object){
        Long commentId=object.getLong("id");
        Long userId=object.getObject("user",User.class).getId();
        return  commentService.like(userId,commentId);
    }

    @PostMapping("addComment")
    @RequireRole("normal")
    public  JsonData addComment(@RequestBody JSONObject object) {
        Long id=object.getObject("id",Long.class);
        Long userId=object.getObject("user",User.class).getId();
        String content=object.getObject("content",String.class);
        return commentService.addComment(id,userId,content);
    }

    @PostMapping("target")
    public  JsonData getTarget(@RequestBody JSONObject object) {
        Long id=object.getObject("id",Long.class);
        return commentService.getCommentTarget(id);
    }
}
