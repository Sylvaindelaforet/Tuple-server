package server.multi;

import java.rmi.RemoteException;

public class LaunchMultiServer {
    public static void main(String[] args)
    {
        int port = Integer.parseInt(args[0]);
        String s = args[1];
        MultiServeur ms = MultiServeur.launchServeur(s,port);
        try {
            if (args.length>2){
                for (int i =2;i<args.length;i++)
                {
                    ms.connecterServeur(args[i],true);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
