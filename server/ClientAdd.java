package server;

import utils.Tuple;

public class ClientAdd {
    public static void main(String[] args) {
        LindaClient lc1 = new LindaClient("//localhost:4000/LindaServer");

        lc1.write(new Tuple(1, 2, 3));
        lc1.write(new Tuple(1, 4, 7));
        lc1.write(new Tuple(1, 6, 9));
        lc1.write(new Tuple(1, 5, 1));

    }
}
