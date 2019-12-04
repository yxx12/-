package thread;

public class Demo1 {
    public static void main(String[] args) {
        Demo2 demo2 = new Demo2();
        Thread thread = new Thread(demo2, "demo");
        Thread thread2 = new Thread(demo2, "demo2");
        thread.start();
        thread2.start();

    }
}

class Demo2 implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }
}

class Demo3 {
    public static void main(String[] args) {
        Demo4 demo4 = new Demo4();
        //开启四条线程做同样的事
        new Thread(demo4, "售票窗口一 ").start();
        new Thread(demo4, "售票窗口二 ").start();
        new Thread(demo4, "售票窗口三 ").start();
        new Thread(demo4, "售票窗口四 ").start();
        new Thread(demo4,"小五").start();
    }
}

class Demo4 implements Runnable {

    int i = 1000;

    @Override
    public void run() {
        while (true) {

            synchronized (Demo4.class) {

                if (i <= 0) {
                    break;
                }
                System.out.println(Thread.currentThread().getName() +
                        "卖了第 " + i + " 票");
                i--;
            }
        }
    }
}