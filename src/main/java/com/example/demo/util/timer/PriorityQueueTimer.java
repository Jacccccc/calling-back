package com.example.demo.util.timer;

import lombok.Data;

import static java.util.concurrent.locks.LockSupport.*;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/13 10:42
 **/

@Data
public class PriorityQueueTimer {
    final int DEFAULT_SIZE=128;
    int size;
    static Task[] tasks; //队列
    Thread masterThread; //进行任务的获取执行
    int tail=1; //下一个任务应该存放的位置
    volatile boolean interrupted=false;
    //TODO 启用线程池
    public PriorityQueueTimer(int size) {
        this.size = size;
        start();
    }
    void start(){
        masterThread=new Thread(()->work(),"masterThread");
        masterThread.start();
    }
    public PriorityQueueTimer() {
        start();
    }
    void work(){
        while (true){
            while (tail==1) {
                park();
            }
            if(getMin().executeTime<=System.currentTimeMillis()/1000){
                doTask();
            }
            else parkUntil(getMin().executeTime*1000);
        }
    }
    synchronized void doTask(){
        new Thread(tasks[1].getRunnable()).start();
        tasks[1].executeTime+=tasks[1].intervalTime;
        if (tasks[1].executeTimes != -1) //如果不是永久执行的任务
            tasks[1].executeTimes--; //剩余次数减一
        delete();
        fixDown(1); //调整堆
    }
    synchronized void fixUp() {
        int child = tail;
        int parent = tail >> 1; //获取父节点下标
        while (parent > 0 && tasks[child].executeTime < tasks[parent].executeTime) {
            swap(parent, child);
            child = parent;
            parent = child >> 1;
        }
    }

    //删除节点时，也需要调整堆顺序，从根到叶子节点
    synchronized void fixDown(int root) {
        int parent = root;
        int leftChild = parent << 1;
        int rightChild = (parent << 1) + 1;
        while (rightChild < tail) //先对左右孩子都有的情况做交换
        {
            //获取左右孩子较小的一个进行交换
            int minIndex = tasks[leftChild].executeTime > tasks[rightChild].executeTime ? rightChild : leftChild;
            if (tasks[parent].executeTime > tasks[minIndex].executeTime) {
                swap(parent, minIndex);
                parent = minIndex;
                leftChild = parent << 1;
                rightChild = (parent << 1) + 1;
            } else return;
        }
        //最后还要进行特殊情况判断，即最右父节点只有左孩子
        if (leftChild < tail && tasks[parent].executeTime > tasks[leftChild].executeTime) swap(parent, leftChild);
    }

    void swap(int h, int t) {
        Task temp = tasks[h];
        tasks[h] = tasks[t];
        tasks[t] = temp;
    }
    void resize(){ //扩容
        Task []temp=tasks;
        if(size==0) size=DEFAULT_SIZE;
        else size*=2;
        tasks=new Task[size];
        for(int i=1;i<tail;i++)
            tasks[i]=temp[i];
    }
    synchronized boolean add(Task task) {
        if (tail >=size ) resize();
        tasks[tail] = task;
        fixUp();
        tail++;
        if (task == tasks[1])
            unpark(masterThread);
        return true;
    }
    synchronized void delete() {
        if (tasks[1].executeTimes == 0) { //执行次数已到，删除节点
            tasks[1] = null; //help GC
            swap(1, tail - 1);
            tail--;
        }
    }

    Task getMin(){
        return tasks[1];
    }
}
