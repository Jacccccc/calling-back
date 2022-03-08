package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.CommentMapper;
import com.example.demo.dao.LikeMapper;
import com.example.demo.util.JsonData;
import com.example.demo.vo.CommentInfo;
import com.example.demo.vo.CommentVo;
import com.example.demo.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/9 14:50
 **/
@Service
public class CommentServiceImpl implements CommentService{
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    UserService userService;
    @Autowired
    LikeMapper likeMapper;
    @Autowired
    RedisTemplate redisTemplate;
    final static Set<String> two = new HashSet<>(Arrays.asList("old", "new"));
    final static String likeCountKey="likeCount:commentId:";
    final static String commentCountKey="commentCount:commentId:";
    final static String activeStatus="active:commentId:";

    private String hotPageKeyOfPost(Long page,Long postId){
        return "hotComment:postId:"+postId+":page:"+page;
    }
    private String hotPageKeyOfComment(Long page,Long commentId){
        return "hotComment:commentId:"+commentId+":page:"+page;
    }
    private String newPageKeyOfPost(Long last,Long postId){
        return "newComment:postId:"+postId+":last:"+last;
    }
    private String newPageKeyOfComment(Long last,Long commentId){
        return "newComment:commentId:"+commentId+":last:"+last;
    }

    /**
     * @description:获取id集合的评论
     * @param ids
     * @return: java.util.List<com.example.demo.vo.CommentVo>
     * @author: Jac
     * @time: 2022/2/26 11:33
     **/
    private List<CommentInfo> getCommentVoByIds(List<Long> ids){
        List<CommentVo> res=new ArrayList<>();
        for (Long id: ids){
            res.add(getCommentVoById(id));
        }
        List<CommentInfo> result=new ArrayList<>();
        for (CommentVo comment:res){
            CommentInfo commentInfo=new CommentInfo();
            commentInfo.setCommentVo(comment);
            result.add(commentInfo);
        }
        setCommentFields(result);
          return result;
    }

    /**
     * @description:获取指定id的评论
     * @param commentId
     * @return: com.example.demo.vo.CommentVo
     * @author: Jac
     * @time: 2022/2/26 11:35
     **/
    private CommentVo getCommentVoById(Long commentId){
           return commentMapper.findCommentById(commentId);

    }


    /**
     * @description:刷新评论的活跃时间
     * @param
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:47
     **/
    private void flushActiveTime(Long commentId){
        redisTemplate.opsForValue().set(activeStatus+commentId,"");
        redisTemplate.expire(activeStatus+commentId,25,TimeUnit.HOURS);
    }

    /**
     * @description:生成评论点赞状态的key
     * @param commentId
     * @param userId
     * @return: java.lang.String
     * @author: Jac
     * @time: 2022/2/26 11:20
     **/
    String getLikeStatusKey(Long commentId,Long userId){
        return "likeStatus:userId:"+userId+":commentId:"+commentId;
    }

    /**
     * @description:点赞或取消赞
     * @param userId
     * @param commentId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/26 11:21
     **/
    @Override
    public JsonData like(Long userId, Long commentId) {
        String likeStatus=getLikeStatusKey(commentId,userId);
        if(userId==null||commentId==null) return JsonData.buildError("error");
        flushActiveTime(commentId);
        String status= (String) redisTemplate.opsForHash().get(likeStatus,"new");
        if(status==null||!redisTemplate.expire(likeStatus,6, TimeUnit.MINUTES)){
            status= (String) likeState(userId,commentId).getData();
        }
        if(status==null) {
            likeMapper.insertLike(userId,commentId,0,1);
            status= (String) likeState(userId, commentId).getData();
            redisTemplate.opsForValue().increment(likeCountKey+commentId);
            return JsonData.buildSuccess();
        }
        if (status.equals("1")) {
            redisTemplate.opsForHash().put(likeStatus,"new","0");
            redisTemplate.opsForValue().increment(likeCountKey+commentId);
        }
        else {
            redisTemplate.opsForHash().put(likeStatus,"new","1");
            redisTemplate.opsForValue().decrement(likeCountKey+commentId);
        }
        return JsonData.buildSuccess();
    }

    /**
     * @description:填充评论的点赞数，评论数的字段
     * @param comments
     * @return: void
     * @author: Jac
     * @time: 2022/2/26 11:23
     **/
    @Override
    public void setCommentFields(List<CommentInfo> comments){
        for (CommentInfo comment : comments) {
            Long commentId = comment.getCommentVo().getId();
            comment.setLikeCounts((Integer) redisTemplate.opsForValue().get(likeCountKey + commentId));
            comment.setCommentCounts((Integer) redisTemplate.opsForValue().get(commentCountKey + commentId));
        }
    }

    /**
     * @description:获取用户对评论的点赞状态
     * @param userId
     * @param commentId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/26 11:28
     **/
    @Override
    public JsonData likeState(Long userId, Long commentId) {
        String likeStatus=getLikeStatusKey(commentId,userId);
        String status= (String) redisTemplate.opsForHash().get(likeStatus,"new");
        if(status==null){
            status= likeMapper.getLikeStatus(userId,commentId,1);
            if(status!=null){
            HashMap<String,String> m=new HashMap<>();
            m.put("old",status);
            m.put("new",status);
            redisTemplate.opsForHash().putAll(likeStatus,m);
            redisTemplate.expire(likeStatus,6,TimeUnit.MINUTES);
            }
        }
        return JsonData.buildSuccess(status);
    }

    @Override
    public JsonData getMyComments(Long userId,Long page) {
        List<Long> ids=commentMapper.findCommentListByUserId(userId,page);
        return JsonData.buildSuccess(getCommentVoByIds(ids));
    }

    @Override
    public JsonData deleteComment(Long userId, Long commentId) {
        if(commentMapper.deleteCommentById(commentId,userId)!=0){
            flushActiveTime(commentId);
            redisTemplate.opsForValue().decrement(commentCountKey+commentId);
        }
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData addComment(Long id, Long userId, String content) {
        flushActiveTime(id);
        redisTemplate.opsForValue().increment(commentCountKey+id);
        commentMapper.addComment(userId,id,1,0,content,new Timestamp(System.currentTimeMillis()));
        return JsonData.buildSuccess();
    }

    /**
     * @description:获取帖子的热评，第page页
     * @param PostId
     * @param page
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/26 11:31
     **/
    @Override
    public JsonData getHotPostComments(Long PostId, Long page) {
        String key=hotPageKeyOfPost(page,PostId);
        Object object= redisTemplate.opsForValue().get(key);
        String json= JSONObject.toJSONString(object);
        List<Long> ids=JSONObject.parseArray(json,Long.class);
         if(ids==null){
         ids=commentMapper.HotCommentOfPost(PostId,page*10);
         if(ids.size()==10) redisTemplate.opsForValue().set(key,ids,24,TimeUnit.HOURS);
         }
         return JsonData.buildSuccess(getCommentVoByIds(ids));
    }

    /**
     * @description:获取帖子的新评，传入前一页的最后一个帖子的id，比这个id小的一定是比它旧的
     * @param PostId
     * @param lastCommentId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/26 11:31
     **/
    @Override
    public JsonData getNewPostComments(Long PostId, Long lastCommentId) {
        String key=newPageKeyOfPost(lastCommentId,PostId);
        Object object= redisTemplate.opsForValue().get(key);
        String json= JSONObject.toJSONString(object);
        List<Long> ids=JSONObject.parseArray(json,Long.class);
        if(ids==null){
            if(lastCommentId ==0)
                ids=commentMapper.findFirstCommentPage(PostId);
            else
            ids=commentMapper.newCommentOfPost(PostId,lastCommentId);
            if(ids.size()==10) redisTemplate.opsForValue().set(key,ids,10,TimeUnit.MINUTES);
        }
        return JsonData.buildSuccess(getCommentVoByIds(ids));
    }


    /**
     * @description:获取评论的新评，传入前一页的最后一个帖子的id，比这个id小的一定是比它旧的
     * @param commentId
     * @param lastCommentId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/26 11:31
     **/
    @Override
    public JsonData getNewCommentComments(Long commentId, Long lastCommentId) {
        String key=newPageKeyOfComment(lastCommentId,commentId);
        Object object= redisTemplate.opsForValue().get(key);
        String json= JSONObject.toJSONString(object);
        List<Long> ids=JSONObject.parseArray(json,Long.class);
        if(ids==null){
            ids=commentMapper.newCommentOfComment(lastCommentId,commentId);
            if(ids.size()==10) redisTemplate.opsForValue().set(key,ids,10,TimeUnit.MINUTES);
        }
        return JsonData.buildSuccess(getCommentVoByIds(ids));
    }

    @Override
    public JsonData getCommentTarget(Long id) {
       Long userId=commentMapper.findTargetById(id);
       return userService.getUserVoByUserId(userId);
    }
}
