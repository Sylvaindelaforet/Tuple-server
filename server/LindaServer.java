package server;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import core.CoreLinda;
import utils.Linda;
import utils.Tuple;

public class LindaServer extends UnicastRemoteObject implements ILindaServer {

    CoreLinda serveurCentralise;
    boolean verbeux;
    String prefix;

    /**
     * Main pour lancer le serveur
     * @param args adresse serveur
     */
    public static void main(String[] args) {
        System.out.println("lancement du serveur");
        String s = "//localhost:4000/LindaServer";
        if (args.length>0) {
            s = args[0];
        }
        try {
            ILindaServer lindaServ = new LindaServer();
            Registry registry = LocateRegistry.createRegistry(4000);
            Naming.rebind(s.toString(), lindaServ);
            lindaServ.debug("lindaserver : ");
            System.out.println("Serveur lancé à l'adresse : " + s);
        } catch (Exception e) {
            System.out.println("Échec du lancement à l'adresse : " + s);
            e.printStackTrace();
        }

    }

    public LindaServer() throws RemoteException{
        serveurCentralise = new CoreLinda();
        this.verbeux = false;
    }

    public void write(Tuple t) throws RemoteException{
        if (verbeux) System.out.println(prefix + "write du tuple : " + t);
        this.serveurCentralise.write(t);
    }

    /** TODO vérifier blocage client et non serveur
     * @param template recherché
     * @return Tuple
     * @throws RemoteException interface
     */
    public Tuple take(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "prise du template : " + template);
        return this.serveurCentralise.take(template);
    }

    public Tuple read(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "read du template : " + template);
        return this.serveurCentralise.read(template);
    }

    public Tuple tryTake(Tuple template,boolean propagation) throws RemoteException {
        if (verbeux) System.out.println(prefix + "tryTake du tuple : " + template);
        return this.serveurCentralise.tryTake(template);
    }

    public Tuple tryRead(Tuple template,boolean propagation) throws RemoteException {
        if (verbeux) System.out.println(prefix + " tryRead du template : " + template);
        return this.serveurCentralise.tryRead(template);
    }

    public Collection<Tuple> takeAll(Tuple template, boolean propagation) throws RemoteException {
        if (verbeux) System.out.println(prefix + "takeAll du template : " + template);
        return this.serveurCentralise.takeAll(template);
    }

    public Collection<Tuple> readAll(Tuple template, boolean propagation) throws RemoteException {
        if (verbeux) System.out.println(prefix + " readAll du template : " + template);
        return this.serveurCentralise.readAll(template);
    }

    @Override
    public void connecterServeur(String adressePerso, boolean b) throws RemoteException {

    }

    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callbackRmt, boolean propagation) throws RemoteException {
        LocalToRemoteCallback cb = new LocalToRemoteCallback(callbackRmt);
        this.serveurCentralise.eventRegister(mode, timing, template, cb);
    }

    public void debug(String prefix) throws RemoteException{
        System.out.println("Serveur mis en mode debug");
        this.prefix = prefix;
        this.verbeux=true;
        this.serveurCentralise.debug(prefix);
    }

}
