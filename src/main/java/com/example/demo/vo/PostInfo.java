package com.example.demo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/23 18:00
 **/
@Data
public class PostInfo implements Serializable {
    PostVo post;
    Integer likeCounts=0;
    Integer seeCounts=0;
    Integer commentCounts=0;
    final String likeState="1";

    public void setLikeCounts(Integer likeCounts) {
        if(likeCounts!=null)
        this.likeCounts = likeCounts;
    }

    public void setSeeCounts(Integer seeCounts) {
        if(seeCounts!=null)
        this.seeCounts = seeCounts;
    }

    public void setCommentCounts(Integer commentCounts) {
        if(commentCounts!=null)
        this.commentCounts = commentCounts;
    }

}
