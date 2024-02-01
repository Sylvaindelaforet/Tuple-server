package server;

import utils.Callback;
import utils.Tuple;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Callback transmis au serveur par le client qui conserve le callback du client pour l'appeler ici
 */
public class RemoteCallback extends UnicastRemoteObject implements IRemoteCallback {

    Callback cbclient;
    
    public RemoteCallback(Callback cb) throws RemoteException {
        this.cbclient=cb;
    }

    public void call(Tuple t) throws RemoteException {
        this.cbclient.call(t);
    }
}
