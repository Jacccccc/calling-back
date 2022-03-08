package com.example.demo.config;

import com.example.demo.service.PostService;
import com.example.demo.util.timer.Task;
import com.example.demo.util.timer.TimeWheelTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @description:
 * @author: Jac
 * @time: 2022/2/14 20:07
 **/
@Configuration
public class TimerConfig {
    @Autowired
    PostService postService;
    @Bean
    TimeWheelTimer initTimer(List<Task> tasks){
        TimeWheelTimer t= new TimeWheelTimer();
        for (Task task : tasks) t.addTimer(task);
        return t;
    }
    @Bean
    List<Task> initTask(){
        List<Task> tasks=new ArrayList<>();
        tasks.add(flushScore());
        tasks.add(flushLikes());
        return tasks;
    }

    Task flushScore(){
        Long flushTime=System.currentTimeMillis()/(1000*86400)*86400+57600; //获取后一天的十二点钟
        Task task=new Task(()->{postService.flushScore();},flushTime,-1L,60*60*24L);
        return task;
    }
    Task flushLikes(){
        Long flushTime=System.currentTimeMillis()/1000;
        Task task=new Task(()->{System.out.println("----------------------execute flush at----------"+new Date(System.currentTimeMillis()));postService.flushLikes("likeStatus:userId:*:postId:*");},flushTime,-1L,60*5L);
        return task;
    }
}
