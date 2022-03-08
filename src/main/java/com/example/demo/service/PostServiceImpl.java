package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.CommentMapper;
import com.example.demo.dao.LikeMapper;
import com.example.demo.dao.PostMapper;
import com.example.demo.entity.Post;
import com.example.demo.util.JsonData;
import com.example.demo.vo.PostInfo;
import com.example.demo.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PostServiceImpl implements PostService{
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PostMapper postMapper;
    @Autowired
    LikeMapper likeMapper;
    @Autowired
    CommentMapper commentMapper;
   final static Set<String> two = new HashSet<>(Arrays.asList("old", "new"));
   final static String hotPageKey="hotPostPage:";
   final static String likeCountKey="likeCount:postId:";
   final static String seeCountKey="seeCount:postId:";
   final static String commentCountKey="commentCount:postId:";
   final static String newPostPage="newPost:lastPostId:";
   final static String activeStatus="active:postId:";

    /**
     * @description:刷新帖子的活跃时间
     * @param
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:47
     **/
    private void flushActiveTime(Long postId){
        redisTemplate.opsForValue().set(activeStatus+postId,"");
        redisTemplate.expire(activeStatus+postId,25,TimeUnit.HOURS);
    }

    /**
     * @description:将帖子的点赞数等字段进行填充
     * @param
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:47
     **/
    private void setPostFields(List<PostInfo> result){
        for (PostInfo post : result) {
            Long postId = post.getPost().getId();
            post.setLikeCounts((Integer) redisTemplate.opsForValue().get(likeCountKey + postId));
            post.setSeeCounts((Integer) redisTemplate.opsForValue().get(seeCountKey + postId));
            post.setCommentCounts((Integer) redisTemplate.opsForValue().get(commentCountKey + postId));
        }
    }

    /**
     * @description:定时刷新帖子热度的任务
     * @param
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:47
     **/
    @Override
    public void flushScore() {
        Set<String> activeKeys=redisTemplate.keys(activeStatus+"*");
        for (String key: activeKeys){
            Long postId= Long.valueOf(key.split(":")[2]);
            addScore(postId);
        }
    }

    /**
     * @description:获取指定id的帖子的postVO
     * @param id
     * @return: com.example.demo.vo.PostVo
     * @author: Jac
     * @time: 2022/2/24 16:48
     **/
    @Override
    public PostVo findPostVoById(Long id) {
      Object object= redisTemplate.opsForValue().get("postVo:postId:"+id);
      String json=JSONObject.toJSONString(object);
      PostVo postVo=JSONObject.parseObject(json,PostVo.class);
       if (postVo==null){
           postVo=postMapper.findPostVoById(id);
           redisTemplate.opsForValue().set("postVo:postId:"+id,postVo,30,TimeUnit.MINUTES);
       }
       return postVo;
    }

    /**
     * @description: 刷新指定id的帖子的热度
     * @param postId
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:49
     **/
    void addScore(Long postId){
       Integer likeCount= (Integer) redisTemplate.opsForValue().get(likeCountKey+postId);
       likeCount=likeCount==null?0:likeCount;
        Integer seeCount= (Integer) redisTemplate.opsForValue().get(seeCountKey+postId);
        seeCount=seeCount==null?0:seeCount;
        Integer commentCount= (Integer) redisTemplate.opsForValue().get(commentCountKey+postId);
        commentCount=commentCount==null?0:commentCount;
        Integer newScore=likeCount*2+seeCount+commentCount*3;
        postMapper.updateScoreById(Long.valueOf(newScore),postId);
    }

    /**
     * @description: 传入上一页最后一个帖子的的id，进行分页查找
     * @param postId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:32
     **/
    @Override
    public JsonData newPostsByPage(Long postId) {
        String key=newPostPage+postId;
        Object object= redisTemplate.opsForValue().get(key);
        String json=JSONObject.toJSONString(object);
        List<Long> result=JSONObject.parseArray(json,Long.class);
        if(object==null) {
            if(postId==0) result=postMapper.findFirstNewPostListByPage();
            else result=postMapper.findNewPostListByPage(postId);
            if(result.size()==10)
            redisTemplate.opsForValue().set(newPostPage+postId,result,10,TimeUnit.MINUTES);
        }
        return getPostByIds(result);
    }

    /**
     * @description: 查找热帖
     * @param
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:34
     **/
    @Override
    public JsonData hotPostsByPage(Long page) {
        String key=hotPageKey+page;
        Object object= redisTemplate.opsForValue().get(key);
        String json=JSONObject.toJSONString(object);
        List<Long> result=JSONObject.parseArray(json,Long.class);
        if(result==null){
        result=postMapper.findHotPagePost(page*10);
        if(result.size()==10)
        redisTemplate.opsForValue().set(key,result,24,TimeUnit.HOURS);
        }
        return getPostByIds(result);
    }

    /**
     * @description: 根据帖子id查找帖子内容
     * @param post_id
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:34
     **/
    @Override
    public JsonData PostContent(Long post_id) {
        String content= (String) redisTemplate.opsForValue().get("content:postId:"+post_id);
        if(content==null){
            content=postMapper.findPostById(post_id);
            if (content==null) return JsonData.buildError("没有该贴");
            redisTemplate.opsForValue().set("content:postId:"+post_id,content,10,TimeUnit.MINUTES);
        }
        redisTemplate.opsForValue().increment(seeCountKey+post_id);
        flushActiveTime(post_id);
        return JsonData.buildSuccess(content);
    }

    /**
     * @description: 获取用户对某个帖子的点赞状态
     * @param postId
     *@param userId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/24 16:50
     **/
    @Override
    public JsonData getLikeState(Long postId, Long userId) {
        String likeKey=getLikeStatusKey(postId,userId);
        String status= (String) redisTemplate.opsForHash().get(likeKey,"new");
        if(status==null){
        status= likeMapper.getLikeStatus(userId,postId,0);
        if(status!=null){
        HashMap<String,String> m=new HashMap<>();
        m.put("old",status);
        m.put("new",status);
        redisTemplate.opsForHash().putAll(likeKey,m);
        redisTemplate.expire(likeKey,6,TimeUnit.MINUTES);
        }
        }
        return JsonData.buildSuccess(status);
    }

    /**
     * @description: 发帖操作
     * @param post
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:35
     **/
    @Override
    public JsonData doPost(Post post,List<String> tags) {
        if(postMapper.addPost(post)==1) {
            new Thread(()->{
                for(String tag :tags){
                   redisTemplate.opsForSet().add(tag,post.getId());
                }
            }).start();
            return JsonData.buildSuccess();
        }
        return JsonData.buildError("发帖失败");
    }

    /**
     * @description:删帖操作
     * @param postId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:35
     **/
    @Override
    public JsonData deletePostById(Long postId) {
        return JsonData.buildSuccess(postMapper.deletePostById(postId));
    }

    /**
     * @description:获取id集合的帖子
     * @param ids
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/24 16:51
     **/
    @Override
    public JsonData getPostByIds(List ids) {
        List<PostVo> posts=new ArrayList<>();
        for (Object aLong : ids) {
            Long id = Long.valueOf(aLong.toString());
            posts.add(findPostVoById(id));
        }
        List<PostInfo> res=new ArrayList<>();
        for (PostVo postVo:posts) {
            PostInfo p=new PostInfo();
            p.setPost(postVo);
            res.add(p);
        }
        setPostFields(res);
        return JsonData.buildSuccess(res);
    }

    /**
     * @description:生成用户对帖子点赞状态的key
     * @param postId
     * @param userId
     * @return: java.lang.String
     * @author: Jac
     * @time: 2022/2/24 16:51
     **/
    String getLikeStatusKey(Long postId,Long userId){
        return "likeStatus:userId:"+userId+":postId:"+postId;
    }

    /**
     * @description:获取帖子点赞数量
     * @param postId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/24 16:52
     **/
    @Override
    public JsonData getLikeCount(Long postId) {
        return JsonData.buildSuccess(redisTemplate.opsForValue().get(likeCountKey+postId));
    }

    /**
     * @description:同步点赞状态的任务
     * @param pattern
     * @return: void
     * @author: Jac
     * @time: 2022/2/24 16:53
     **/
    @Override
    public void flushLikes(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        for (String key:keys){
            List<String> status=redisTemplate.opsForHash().multiGet(key,two);
            if(!status.get(0).equals(status.get(1))){
                redisTemplate.opsForHash().put(key,"old",status.get(0));
                String []elements=key.split(":");
                Long userId= Long.valueOf(elements[2]);
                Long postId= Long.valueOf(elements[4]);
                likeMapper.updateLike(userId,postId,0, Integer.valueOf(status.get(0)));
            }
        }
    }

    /**
     * @description:点赞/取消赞
     * @param userId
     * @param postId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:58
     **/
    @Override
    public JsonData like(Long userId,Long postId) {
         if(userId==null||postId==null) return JsonData.buildError("error");
        flushActiveTime(postId);
         String statusKey=getLikeStatusKey(postId,userId);
         String countKey=likeCountKey+postId;
         String status= (String) redisTemplate.opsForHash().get(statusKey,"new");
        if(status==null||!redisTemplate.expire(statusKey,6, TimeUnit.MINUTES)){
           status= (String) getLikeState(postId,userId).getData();
        }
        if(status==null) {
            likeMapper.insertLike(userId,postId,0,0);
            status= (String) getLikeState(postId,userId).getData();
            redisTemplate.opsForValue().increment(countKey);
            return JsonData.buildSuccess();
        }
        if (status.equals("1")) {
            redisTemplate.opsForHash().put(statusKey,"new","0");
            redisTemplate.opsForValue().increment(countKey);
        }
        else {
            redisTemplate.opsForHash().put(statusKey,"new","1");
            redisTemplate.opsForValue().decrement(countKey );
        }
        return JsonData.buildSuccess();
    }

    /**
     * @description:审核贴子并通过
     * @param postId
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:36
     **/
    @Override
    @Transactional
    public JsonData passPostByPostId(Long postId) {
        postMapper.reviewPostAndPass(postId);
        return JsonData.buildSuccess();
    }

    /**
     * @description:获取一页待审核的帖子
     * @param
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:37
     **/
    @Override
    public JsonData PostToReviewByPage(Long lastId) {
       List<Long> ids=postMapper.findPostToPass(lastId);
       return getPostByIds(ids);
    }

    /**
     * @description: 查看用户自己的帖子，分页
     * @param
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:38
     **/
    @Override
    public JsonData UserPostsByPage(Long userId) {
        List<Long> ids=postMapper.findPostListByUserId(userId);
        JsonData jsonData=getPostByIds(ids);
        return jsonData;
    }

    /**
     * @description:将帖子关小黑屋
     * @param id
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/8 21:39
     **/
    @Override
    public JsonData blockPostById(Long id) {
        return JsonData.buildSuccess(postMapper.updateStatusById(id,2));
    }

    /**
     * @description:发表对帖子的评论
     * @param userId
     * @param postId
     * @param content
     * @return: com.example.demo.util.JsonData
     * @author: Jac
     * @time: 2022/2/24 16:54
     **/
    @Override
    @Transactional
    public JsonData commentPost(Long userId, Long postId, String content) {
        flushActiveTime(postId);
        redisTemplate.opsForValue().increment(commentCountKey+postId);
      return JsonData.buildSuccess(commentMapper.addComment(userId,postId,0,0,content,new Timestamp(System.currentTimeMillis())));
    }
}
