package utils.application;

import java.util.UUID;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import utils.*;

public class Manager implements Runnable {

    private Linda linda;

    private UUID reqUUID;
    private String pathname;
    private String search;
    private int bestvalue = Integer.MAX_VALUE; // lower is better
    private String bestresult;

    public Manager(Linda linda, String pathname, String search) {
        this.linda = linda;
        this.pathname = pathname;
        this.search = search;
        this.reqUUID = UUID.randomUUID();
    }

    private void addSearch(String search) {
        this.search = search;
        System.out.println("Search " + this.reqUUID + " for " + this.search);
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, this.reqUUID, String.class, Integer.class), new CbGetResult());
        linda.write(new Tuple(Code.Request, this.reqUUID, this.search));
    }

    private void loadData(String pathname) {
        try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
            stream.limit(10000).forEach(s -> linda.write(new Tuple(Code.Value, s.trim(),this.reqUUID)));

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForEndSearch() {
        Tuple t;
        int k=0;
        do {
            t = linda.tryTake(new Tuple(Code.Searcher, "done", this.reqUUID));
            k++;
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (t == null && k<=2500 );
        linda.take(new Tuple(Code.Request, this.reqUUID, String.class)); // remove query
        System.out.println("query done");
    }

    private class CbGetResult implements Callback {
        public void call(Tuple t) {  // [ Result, ?UUID, ?String, ?Integer ]
            String s = (String) t.get(2);
            Integer v = (Integer) t.get(3);
            if (v < bestvalue) {
                bestvalue = v;
                bestresult = s;
                System.out.println("New best (" + bestvalue + "): \"" + bestresult + "\"");
            }
            //System.out.println(linda.tryRead(new Tuple(Code.Result, reqUUID, String.class, Integer.class)));
            linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);

        }
    }

    public void run() {
        this.loadData(pathname);
        this.addSearch(search);
        this.waitForEndSearch();
        System.out.println("manager done");
    }
}
