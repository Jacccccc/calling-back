package com.example.demo.service;

import com.example.demo.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/18 17:14
 **/
@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public JsonData searchPostByTag(List<String> tags) {
        Set<Long> ids=null;
        if(tags.size()==1)
        ids=redisTemplate.opsForSet().members(tags.get(0));
        else while (ids==null&&tags.size()>0){
            ids=redisTemplate.opsForSet().intersect(tags);
            if(ids==null) tags.remove(tags.size()-1);
        }
        if(ids==null) return JsonData.buildError("抱歉，没有相关帖子");
        return JsonData.buildSuccess(ids);
    }
}
