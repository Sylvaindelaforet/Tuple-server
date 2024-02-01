package server.cache;

import utils.Linda;
import utils.Tuple;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import server.IRemoteCallback;

/**
 * sert à enlever du cache à l'appel du call
 */
public class CacheRemoteCallback extends UnicastRemoteObject implements IRemoteCallback {

    Linda cache;
    boolean debug;

    /**
     * @param cache cache du serveur
     * @param debug print quand le callback est appelé
     */
    public CacheRemoteCallback(Linda cache, boolean debug) throws RemoteException{
        this.cache = cache;
        this.debug = debug;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        if (debug) {System.out.println("Debug callback : Tuple :  " + t + " retiré du cache par le serveur");}
        cache.tryTake(t);
    }
}
