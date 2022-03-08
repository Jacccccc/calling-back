package com.example.demo.util.timer;

import lombok.Data;

import java.util.Date;

import static java.util.concurrent.locks.LockSupport.park;
import static java.util.concurrent.locks.LockSupport.parkUntil;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/13 12:26
 **/
@Data
public class TimeWheelTimer {
    final Wheel wheel;
    Long startTime;
    Thread masterThread;

    public TimeWheelTimer() {
        startTime=System.currentTimeMillis()/1000;
        masterThread = new Thread(this::run,"masterThread");
        wheel = new Wheel(1L,masterThread);
        masterThread.start();
    }

    /*添加定时任务时判断当前时间是否为第一次执行时间，若是则先执行一次再判断是否需要执行添加操作，
    这样的做法也可以防止在master线程对槽位进行操作时在相同槽位添加进任务，即可免去对槽位的加锁
    那么master线程和用户线程的竞争就消失了，但用户线程之间的竞争是任然存在的，因为两个用户线程的添加
    操作完全有可能打到同一个槽位上
    */

    public void addTimer(Task task){
        if(task.executeTime<=System.currentTimeMillis()/1000) {
            new Thread(task.getRunnable()).start();
            task.executeTime+=task.intervalTime;
            if(task.executeTimes==1) return;
            if(task.executeTimes!=-1) task.executeTimes--;
        }
        add(task);
    }
    //计算出当前时间点对应轮盘的指针位置，计算方法为：（当前时间-指针开始移动的时间）=>转换为轮盘对应的单位，再对轮盘槽位取模
    //比如开始时间是10秒，那么开始时时间轮盘都指向0，假如现在是20000秒，那么秒针应该指向（20000/1）%60=20，分针指向，（20000/60）%60=33
    int getSystemPoint(Wheel w){
        return Math.toIntExact(((System.currentTimeMillis() / 1000 - startTime) / (w.tickDuration)) % 128);
    }
    //获取最短睡眠时间，为空代表没有任务
    Long getMinSleepTime(){
        Wheel w=wheel;
        while (w!=null){
            if(w.getMin()==null) w=w.nextWheel;
            else return w.getMin();
        }
        return null;
    }

    void run() {
        while (true) {
            while (getMinSleepTime()==null){ park();}
            if (System.currentTimeMillis() / 1000 >= getMinSleepTime()) { //判断是否应该执行，要执行就证明最小堆的最小时间到了，这个时间所有任务都要执行，并将堆顶删除
                    changePoint(wheel);
            } else {
                parkUntil(getMinSleepTime() * 1000);}//睡眠至最近的槽位或者
        }
    }

    void changePoint(Wheel w) { //改变指针
        while(w!=null){
            w.point=getSystemPoint(w);
            doTaskOrLevelDown(w); //查看本槽位有没有需要执行或者降级的任务
            w=w.nextWheel;
        }
    }

    void doTaskOrLevelDown(Wheel w) {
        Task task = w.tasks[w.point].nextTask;
        if (task == null) return; //槽里什么都没有
        w.delete();
        while (task != null) {
            if (task.executeTime <= System.currentTimeMillis() / 1000)
                doTask(task);
            delete(task); //删除,会判断是否需要重新加入
            task = task.nextTask;
        }

    }

    void doTask(Task task) {
            new Thread(task.getRunnable()).start();
            task.executeTime += task.intervalTime;
            if (task.executeTimes != -1) task.executeTimes--;
    }

    void delete(Task task) {
        Task next = task.nextTask;
        Task prev=task.preTask;
        prev.nextTask = next;
        task.preTask=null;
        task.nextTask=null;
        if (task.executeTimes != 0) {add(task);} //执行次数不等于0，重新插入队列
        if (next != null) next.preTask = prev;
    }

    void add(Task task) {
        int index = Math.toIntExact((task.executeTime - System.currentTimeMillis() / 1000));
        Wheel w = wheel;
        while (index >= 128) {
            index >>= 7;
            if (w.nextWheel == null) {
                w.nextWheel = new Wheel(w.tickDuration * 128,masterThread);
                w.nextWheel.prevWheel = w;
            }
            w = w.nextWheel;
        }
        index=(index+getSystemPoint(w))%128;
        synchronized (w.tasks[index]) {
            addToWheel(w, task, index);
        }
    }

    void addToWheel(Wheel w, Task task, int index) {
        task.nextTask = w.tasks[index].nextTask;
        task.preTask = w.tasks[index];
        w.tasks[index].nextTask = task;
        if (task.nextTask != null) task.nextTask.preTask = task;
        else w.addToMin(task.executeTime); //链表第一个节点，需要进行进堆操作
    }
}
