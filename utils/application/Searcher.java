package utils.application;

import utils.*;
import java.util.Arrays;
import java.util.UUID;

public class Searcher implements Runnable {

    private Linda linda;
    private boolean finalized;
    private Tuple tv;



    public Searcher(Linda linda) {
        this.linda = linda; this.finalized=false;
    }

    public void run() {
        System.out.println("Ready to do a search");
        linda.write(new Tuple(Code.Searcher, true));
        //int k=0;
        while (true) {
            //k++;
            Tuple treq = linda.read(new Tuple(Code.Request, UUID.class, String.class));
            UUID reqUUID = (UUID) treq.get(1);
            String req = (String) treq.get(2);
            System.out.println("Looking for: " + req);
            //On récupère le text pour chercher si il ya
            boolean solFound=false;
            while ((tv = linda.tryTake(new Tuple(Code.Value, String.class,reqUUID))) != null) {
                String val = (String) tv.get(1);
                int iter=0;
                int distmin = 0;
                solFound=false;
                String S="";
                for (String s : val.split(" ")){
                    if (iter==0){
                        distmin=getLevenshteinDistance(req, s);
                        if (distmin < 10) {
                            solFound=true;
                            S=s;
                        }
                    }
                    else{
                        int dist = getLevenshteinDistance(req, s);
                        if (dist < 10) { // arbitrary
                            if (dist<distmin){
                                solFound=true;
                                distmin=dist;
                                S=s;
                            }
                        }
                    }
                    iter++;
                }
                if (solFound){
                    linda.write(new Tuple(Code.Result, reqUUID, S, distmin));
                    break;
                }
            }

            linda.write(new Tuple(Code.Searcher, "done", reqUUID));

        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!finalized) {
            linda.take(new Tuple(Code.Searcher, true));
            linda.write(tv);
        }
    }

    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

}

