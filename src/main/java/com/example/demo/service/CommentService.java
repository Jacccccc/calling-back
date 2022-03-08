package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.util.JsonData;
import com.example.demo.vo.CommentInfo;
import com.example.demo.vo.CommentVo;

import java.util.List;

public interface CommentService {
    JsonData like(Long userId,Long commentId);
    void setCommentFields(List<CommentInfo> comments);

    JsonData getHotPostComments(Long PostId, Long page);

    JsonData getNewPostComments(Long PostId, Long lastCommentId);


    JsonData getNewCommentComments(Long commentId,Long lastCommentId);

    JsonData getMyComments(Long userId,Long page);

    JsonData deleteComment(Long userId,Long commentId);

    JsonData likeState(Long userId,Long commentId);

    JsonData addComment(Long id, Long userId, String content);

    JsonData getCommentTarget(Long id);
}
