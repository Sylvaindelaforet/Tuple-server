package utils;

public class CallbackPrint implements Callback {

    public void call(Tuple t) {
        System.out.println("Called CallBack on tuple : " + toString());
    }

}

