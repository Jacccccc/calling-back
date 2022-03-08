package com.example.demo.util.timer;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @description:
 * @author: Jac
 * @time: 2022/2/9 20:04
 **/

@Data
@NoArgsConstructor
public class Task {
    private Runnable runnable;//要执行的任务
    Long intervalTime=null;//执行的间隔时间，以秒为单位
    Long executeTime=null;//任务要执行的时间点，以秒为单位
    Task nextTask=null;//下一个任务
    Task preTask=null;//前一个任务
    Long executeTimes=-1L;//任务的执行次数，默认为-1，永久执行
    public Task(Runnable runnable,Long executeTime,Long executeTimes,Long intervalTime) {
        this.runnable=runnable;
        this.executeTime=executeTime;
        this.executeTimes=executeTimes;
        this.intervalTime=intervalTime;
    }
}
