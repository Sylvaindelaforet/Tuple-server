package server.multi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import server.IRemoteCallback;
import utils.Linda;
import utils.Tuple;

public interface MultiLindaServer extends Remote {


    public void connecterServeur(String dest, boolean retour) throws RemoteException;

    public void write(Tuple t) throws RemoteException;

    public Tuple take(Tuple template) throws RemoteException;

    public Tuple read(Tuple template) throws RemoteException;

    public Tuple tryTake(Tuple template) throws RemoteException;

    public Tuple tryRead(Tuple template) throws RemoteException;

    public Collection<Tuple> takeAll(Tuple template, boolean propagation) throws RemoteException;

    public Collection<Tuple> readAll(Tuple template, boolean propagation) throws RemoteException;

    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callback) throws RemoteException;

    public void debug(String prefix) throws RemoteException;
}