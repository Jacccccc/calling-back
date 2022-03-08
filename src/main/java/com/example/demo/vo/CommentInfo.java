package com.example.demo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/26 11:24
 **/
@Data
@NoArgsConstructor
public class CommentInfo {
    Integer likeCounts=0;
    Integer commentCounts=0;
    String likeState="1";
    CommentVo commentVo;
    public void setLikeCounts(Integer likeCounts) {
        if(likeCounts!=null)
            this.likeCounts = likeCounts;
    }


    public void setCommentCounts(Integer commentCounts) {
        if(commentCounts!=null)
            this.commentCounts = commentCounts;
    }
}
