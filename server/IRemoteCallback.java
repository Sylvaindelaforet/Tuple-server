package server;

import utils.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteCallback extends Remote {
    void call(Tuple t) throws RemoteException;
}
