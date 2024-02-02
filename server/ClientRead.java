package server;

import utils.Tuple;

public class ClientRead {
    public static void main(String[] args) {
        InnerClientRead thread1 = new InnerClientRead();
        InnerClientRead thread2 = new InnerClientRead();
        InnerClientRead thread3 = new InnerClientRead();
        InnerClientRead thread4 = new InnerClientRead();
        InnerClientRead thread5 = new InnerClientRead();
        InnerClientRead thread6 = new InnerClientRead();
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();

        try {
            Thread.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread6.start();
    }

    public static class InnerClientRead extends Thread {
        @Override
        public void run() {
            LindaClient lc1 = new LindaClient("//localhost:4000/LindaServer");
            
            System.out.println(lc1.take(new Tuple(1, 2, 3)));
            System.out.println(lc1.take(new Tuple(1, 4, 7)));
            System.out.println(lc1.take(new Tuple(1, 6, 9)));
            System.out.println(lc1.take(new Tuple(1, 7, 1)));
        }
    }

}
