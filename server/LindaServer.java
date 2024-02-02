package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import utils.Linda;
import utils.Tuple;

public interface LindaServer extends Remote {

    public void write(Tuple t) throws RemoteException;
    
    public Tuple take(Tuple template) throws RemoteException;

    public Tuple read(Tuple template) throws RemoteException;

    public Tuple tryTake(Tuple template, boolean propagation) throws RemoteException;
    
    public Tuple tryRead(Tuple template, boolean propagation) throws RemoteException;

    public Collection<Tuple> takeAll(Tuple template, boolean propagation)  throws RemoteException;

    public Collection<Tuple> readAll(Tuple template, boolean propagation)  throws RemoteException;

    public void connecterServeur(String adressePerso, boolean b) throws RemoteException;

    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callback, boolean propagation) throws RemoteException;

    public void debug(String prefix) throws RemoteException;
}


