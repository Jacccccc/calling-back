package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.service.PostService;
import com.example.demo.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
     PostService postService;

    @PostMapping("add")
    @RequireRole("normal")
    public JsonData addPost(@RequestBody JSONObject object) {
        Post post=object.toJavaObject(Post.class);
        post.setCreateTime(new Timestamp(System.currentTimeMillis()));
        List<String> tags=object.getObject("tags",List.class);
        return postService.doPost(post,tags);
    }

    @RequestMapping("new")
    public JsonData getNewPosts(@RequestBody JSONObject object) {
        Long id=object.getObject("lastPostId",Long.class);
        return  postService.newPostsByPage(id);
    }

    @RequestMapping("detail")
    public JsonData getPostDetail(@RequestBody JSONObject object){
        Long id=object.getObject("id",Long.class);
        return postService.PostContent(id);
    }

    @RequestMapping("delete")
    @RequireRole("normal")
    public JsonData deletePost(@RequestBody JSONObject object){
        Long id=object.getObject("id",Long.class);
        Long postUserId=object.getObject("userId",Long.class);
       if (!id.equals(postUserId)) return JsonData.buildError("没有删除权限");
        return postService.deletePostById(id);
    }

    @RequestMapping("like")
    @RequireRole("normal")
    public  JsonData like(@RequestBody JSONObject object) {
        Long id=object.getObject("id",Long.class);
        Long userId=object.getObject("user",User.class).getId();
        return postService.like(userId,id);
    }

    @PostMapping("addComment")
    @RequireRole("normal")
    public  JsonData addComment(@RequestBody JSONObject object) {
        Long id=object.getObject("id",Long.class);
        Long userId=object.getObject("user",User.class).getId();
        String content=object.getObject("content",String.class);
        return postService.commentPost(userId,id,content);
    }

    @RequestMapping("hot")
    public  JsonData getHotPostByPage(@RequestBody JSONObject object) {
        Long page=object.getObject("page",Long.class);
        return  postService.hotPostsByPage(page);
    }

    @RequestMapping("likeState")
    @RequireRole("normal")
    public JsonData getLikeState(@RequestBody JSONObject object){
        Long postId=object.getLong("id");
        Long userId=object.getObject("user",User.class).getId();
        return postService.getLikeState(postId,userId);
    }

    @RequestMapping("likeCount")
    public JsonData getLikeCount(@RequestBody JSONObject object){
        Long postId=object.getLong("id");
        return postService.getLikeCount(postId);
    }

    @RequestMapping("idsPost")
    public JsonData getPostByIds(@RequestBody JSONObject object){
        List ids=object.getObject("ids",List.class);
        System.out.println(ids);
        return postService.getPostByIds(ids);
    }
}
