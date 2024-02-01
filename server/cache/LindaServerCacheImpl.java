package server.cache;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import core.CoreLinda;
import server.IRemoteCallback;
import server.LocalToRemoteCallback;
import utils.Callback;
import utils.Linda;
import utils.Tuple;

public class LindaServerCacheImpl extends UnicastRemoteObject implements LindaServerCache {

    private CoreLinda lindaCentralise;
    private boolean verbeux;
    private Map<Tuple,List<IRemoteCallback>> listeCallback;
    private String prefix;

    /**
     * Main pour lancer le serveur
     * @param args adresse serveur
     */
    public static void main(String[] args) {
        System.out.println("lancement du serveur");
        String s = "//localhost:4000/LindaServer";
        if (args !=null && args.length>0) {
            s = args[0];
        }
        try {
            LindaServerCache lindaServ = new LindaServerCacheImpl();
            Registry registry = LocateRegistry.createRegistry(4000);
            Naming.rebind(s.toString(), lindaServ);
            lindaServ.debug("LindaServer debugs : ");
            System.out.println("Serveur lancé à l'adresse : " + s);
        } catch (Exception e) {
            System.out.println("Échec du lancement à l'adresse : " + s);
            e.printStackTrace();
        }
    }

    public LindaServerCacheImpl() throws RemoteException{
        this.lindaCentralise = new CoreLinda();
        this.listeCallback = new ConcurrentHashMap<Tuple, List<IRemoteCallback>>();
        this.verbeux = false;
    }

    /** TODO : mettre le callback que si il n'y a pas déjà le même
     * @param t tuple
     * @throws RemoteException interface
     */
    public void write(Tuple t) throws RemoteException{
        if (verbeux) System.out.println(prefix + "write du tuple : " + t);
        this.lindaCentralise.write(t);
    }

    /** TODO vérifier blocage client et non serveur
     * @param template recherché
     * @return Tuple
     * @throws RemoteException interface
     */
    public Tuple take(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "prise du template : " + template);
        Tuple t = this.lindaCentralise.take(template);
        if (this.lindaCentralise.tryRead(t)==null){
            for (Tuple t0: listeCallback.keySet()) {
                if (t0.matches(t)) {
                    callListe(this.listeCallback.get(t0), t0);
                    this.listeCallback.remove(t0);
                }
            }
        }
        return t;
    }

    public Tuple read(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "read du template : " + template);
        return this.lindaCentralise.read(template);
    }

    public Tuple tryTake(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "tryTake du template : " + template);
        Tuple t = this.lindaCentralise.tryTake(template);
        if ( t != null && this.lindaCentralise.tryRead(t)==null){
            if (verbeux) System.out.println(prefix + "le dernier : " + template + " a été retiré donc appel des callback");
            for (Tuple t0 : listeCallback.keySet()) {
                if (t0.matches(t)) {
                    if (verbeux)
                        System.out.println(prefix + "appel des callback associé au template : " + template);
                    callListe(this.listeCallback.get(t0), t0);
                    this.listeCallback.remove(t0);
                    System.out.println(listeCallback);
                    if (verbeux) {System.out.println(prefix + "retrait des cb associé au template : " + template);}
                }
            }
        }
        return t;
    }

    public Tuple tryRead(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + " tryRead du template : " + template);
        return this.lindaCentralise.tryRead(template);
    }

    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + "takeAll du template : " + template);
        return this.lindaCentralise.takeAll(template);
    }

    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        if (verbeux) System.out.println(prefix + " readAll du template : " + template);
        return this.lindaCentralise.readAll(template);
    }

    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callbackRmt) throws RemoteException {
        LocalToRemoteCallback cb = new LocalToRemoteCallback(callbackRmt);
        this.lindaCentralise.eventRegister(mode, timing, template, cb);
    }

    public void debug(String prefix) throws RemoteException{
        System.out.println("Serveur mis en mode debug");
        this.prefix = prefix;
        this.verbeux=true;
        this.lindaCentralise.debug(prefix);
    }

    /** On s'enregistre pour que la suppression sur serveur supprime sur les cache
     * @param t tuple écrit dans le cache
     * @param cb le callback qui fait le retour
     * @throws RemoteException nécessaire pour RMI
     */
    @Override
    public void registerDel(Tuple t, IRemoteCallback cb) throws RemoteException {
        if (verbeux) {System.out.println(prefix + "registerDel appelé sur " + t+
                "et le callback " + cb);}
        System.out.println("dans registerDEl : liste callback = " + listeCallback);
        List<IRemoteCallback> lc = this.listeCallback.get(t);
        if (lc == null) {
            ArrayList<IRemoteCallback> alr = new ArrayList<IRemoteCallback>();
            alr.add(cb);
            this.listeCallback.put(t, alr);
        } else {
            lc.add(cb);
        }
    }

    /**
     * méthode statique pour appeler tous les callbacks d'une liste de callback
     * @param lb
     * @param t1
     */
    static private void callListe(List<IRemoteCallback> lb, Tuple t1) {
        for (IRemoteCallback cb : lb ){
            try {
                cb.call(t1);
            } catch (RemoteException err) {
                err.printStackTrace();
            }
        }
    }

}
