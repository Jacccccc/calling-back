package com.example.demo.util.timer;

import lombok.Data;

import java.util.Date;
import java.util.LinkedList;

import static java.util.concurrent.locks.LockSupport.unpark;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/13 12:35
 **/
@Data
public class Wheel {
    Long tickDuration; //间隔，相当于时分秒
    int point = 0; //指向槽位的指针
    int tail = 1;
    Long[] min; //存放最近需要进行操作的任务的时间
    Wheel nextWheel; //下一轮
    Wheel prevWheel; //前一轮
    Task[] tasks; //每一个槽位，固定槽位数128
    Thread masterThread;
    public Wheel(Long tickDuration,Thread masterThread) {
        this.masterThread=masterThread;
        min=new Long[129];
        this.tickDuration = tickDuration;
        this.tasks = new Task[128];
        for (int i = 0; i < 128; i++) //哑节点，一用于分段锁，二方便链表的操作
            tasks[i] = new Task();
    }
    //添加到最小堆里
    synchronized void addToMin(Long time) {
        time=getBucketTime(time);
        min[tail] = time;
        fixUp();
        tail++;
        if (min[1].equals(time)) {unpark(masterThread);} //如果调整堆后位于堆顶(说明是第一个任务或者是新任务比其他任务时间更近)，需要重新计算睡眠时间
    }

    void fixUp() {
        int child = tail;
        int parent = tail >> 1; //获取父节点下标
        while (parent > 0 && min[child] < min[parent]) {
            swap(parent, child);
            child = parent;
            parent = child >> 1;
        }
    }

    void fixDown() {
        int parent = 1;
        int leftChild = parent << 1;
        int rightChild = (parent << 1) + 1;
        while (rightChild < tail) //先对左右孩子都有的情况做交换
        {
            //获取左右孩子较小的一个进行交换
            int minIndex = min[leftChild] > min[rightChild] ? rightChild : leftChild;
            if (min[parent] > min[minIndex]) {
                swap(parent, minIndex);
                parent = minIndex;
                leftChild = parent << 1;
                rightChild = (parent << 1) + 1;
            } else return;
        }
        //最后还要进行特殊情况判断，即最右父节点只有左孩子
        if (leftChild < tail && min[parent] > min[leftChild]) swap(parent, leftChild);
    }

    void swap(int h, int t) {
        Long temp = min[h];
        min[h] = min[t];
        min[t] = temp;
    }
    //获取任务对应的睡眠时间，任务的执行时间不一定等于睡眠到的时间
    //比如对于129，130的任务，它们的睡眠时间应该是128秒，最小单位必须是轮盘的单位，因为睡眠是以槽位为单位的。
    Long getBucketTime(Long executeTime){
        long internal=executeTime-System.currentTimeMillis()/1000;
        return System.currentTimeMillis()/1000+(internal/tickDuration)*tickDuration;
    }
    synchronized void delete(){
        swap(1,tail-1);
        min[tail-1]=null;
        tail--;
        fixDown();
    }
    Long getMin(){
        return min[1];
    }
}
