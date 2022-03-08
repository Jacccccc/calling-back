package com.example.demo.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @description:
 * @author: Jac
 * @time: 2022/3/5 14:08
 **/

public class bucket {
    volatile long count; //当前容量
    volatile long nextTime; //下一次添加令牌的时间
    static final int internal=3; //添加令牌的间隔时间
    static final long maxCount=100;   //桶最大容量
    static final int per=1;
    private static final long COUNT_OFFSET;
    private static final long TIME_OFFSET;
    private static final Unsafe U;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
            Class<?> k = bucket.class;
            COUNT_OFFSET=U.objectFieldOffset(
                    (k.getDeclaredField("count")));
            TIME_OFFSET=U.objectFieldOffset(
                    (k.getDeclaredField("nextTime")));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    public bucket(){
        count=maxCount;
        nextTime=System.currentTimeMillis();
    }
    //-1拒绝，1放行
    public int get(){
        for (;;){
            while (count>0){
                if(U.compareAndSwapLong(this,COUNT_OFFSET,count,count-1)) return 1;
            }
            long t=System.currentTimeMillis()-nextTime;
            long preTime=nextTime;
            if(t>=0) {
                long c=(t/internal+1)*per;
                c=c>200?200:c;
                if(U.compareAndSwapLong(this,TIME_OFFSET,preTime,System.currentTimeMillis()+internal)) {
                    count=c;
                }
                continue;
            }
            return -1;
        }
    }

}
