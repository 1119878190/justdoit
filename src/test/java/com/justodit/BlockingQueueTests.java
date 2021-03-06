package com.justodit;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//阻塞队列测试
public class BlockingQueueTests {



    public static void main(String[] args) {
        BlockingQueue queue = new ArrayBlockingQueue(10);//这个队列中最多只能存放10条数据
        new Thread(new producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();

    }
}

//生产者
class producer implements Runnable{

    private BlockingQueue<Integer> queue;

    public producer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            //生产者生产100个消息
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产" + queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

//消费者
class Consumer implements  Runnable{

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }


    @Override
    public void run() {
        try {
            while (true){
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费:" + queue.size());

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
