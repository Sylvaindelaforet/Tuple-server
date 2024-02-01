package server.cache;

import utils.Callback;
import utils.Linda;
import utils.Tuple;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collection;

import core.CoreLinda;
import server.IRemoteCallback;
import server.RemoteCallback;


public class LindaClientCache implements Linda {

    public Linda cache;
    private LindaServerCache lindaServ;
    private boolean verbeux;
    private IRemoteCallback cbClient;


    public LindaClientCache (String adresseServeur) {
        this.cache = new CoreLinda();
        this.verbeux = false;
        try {
            this.cbClient = new CacheRemoteCallback(this.cache, verbeux);
        } catch (Exception ex) {
            System.err.println("Echec de l'instanciation du cb pour le client");
            ex.printStackTrace();
        }
        try {
            if (verbeux) System.out.println("Connexion au serveur " + adresseServeur + " ...");
            this.lindaServ = (LindaServerCache) Naming.lookup(adresseServeur);
            if (verbeux) System.out.println("Connecté au serveur :" + adresseServeur);
        } catch (Exception ex) {
            if (verbeux) System.out.println("La connexion a échoué");
            ex.printStackTrace();
        }
    }

    /** Initializes the Linda implementation.
     *  the URI of the server is : //localhost:4000/LindaServer
     */
    public LindaClientCache() {
        this.cache = new CoreLinda();
        this.verbeux = false;
        try {
            this.cbClient = new CacheRemoteCallback(this.cache, verbeux);
        } catch (Exception ex) {
            System.err.println("Echec de l'instanciation du cb pour le client");
            ex.printStackTrace();
        }
        String s = "//localhost:4000/LindaServer";
        try {
            if (verbeux) System.out.println("Connexion au serveur " + s + " ...");
            this.lindaServ = (LindaServerCache) Naming.lookup(s);
            if (verbeux) System.out.println("Connecté au serveur :" + s);
        } catch (Exception ex) {
            if (verbeux) System.out.println("La connexion a échoué");
            ex.printStackTrace();
        }
    }


    /**
     * on écrit dans le cache et dans le serveur et on envoie le callback au serveur
     * @param t à write
     */
    @Override
    public void write(Tuple t) {
        try {
            this.lindaServ.write(t);
            if(verbeux){System.out.println("prefix" + t + " écrit au serveur");}
            this.lindaServ.registerDel(t,cbClient);
            if(verbeux){System.out.println("registerdel exécuté");}
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.cache.write(t);
    }

    @Override
    public Tuple take(Tuple template) {
        Tuple t = null;
        try {
            t = this.lindaServ.take(template);
            if(verbeux){System.out.println("prefix" + t + " pris au serveur");}
        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return t;
    }

    @Override
    public Tuple read(Tuple template) {
        Tuple t = this.cache.tryRead(template);
        if (t==null) {
            try {
                t = this.lindaServ.read(template);
                this.cache.write(t);
                this.lindaServ.registerDel(t,cbClient);
                if(verbeux){System.out.println("prefix" + t + " lu du serveur");}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        Tuple t = null;
        try {
            t = this.lindaServ.tryTake(template);
            if(verbeux) {System.out.println("prefix" + t + " pris au serveur");}
        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return t;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        Tuple t = this.cache.tryRead(template);
        if (t == null) {
            try {
                t = lindaServ.tryRead(template);
                if (t!=null) {
                    this.cache.write(t);
                    this.lindaServ.registerDel(t, cbClient);
                    if(verbeux){System.out.println("prefix" + t + " lu du serveur");}
                } else {
                    if(verbeux){System.out.println(template + " n'a pas été trouvé sur le serveur");}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        Collection<Tuple> c = null;
        try {
            c = this.lindaServ.takeAll(template);
        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return c;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        Collection<Tuple> ct =null;
        try {
            ct = this.lindaServ.readAll(template);
            for (Tuple t: ct) {
                // pour s'assurer qu'il n'y soit qu'une fois
                this.cache.tryTake(t);
                this.cache.write(t);
                this.lindaServ.registerDel(t,cbClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ct;
    }

    /* tout au-dessus a été relu mais à tester */

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            IRemoteCallback callbackRemote = new RemoteCallback(callback);
            lindaServ.eventRegister(mode, timing, template, callbackRemote);
        }
        catch (Exception I) {
            I.printStackTrace();
        }
    }

    @Override
    public void debug(String prefix) {
        this.verbeux = true;
        try {
            this.cbClient = new CacheRemoteCallback(this.cache, verbeux);
        } catch (Exception ex) {
            System.err.println("Echec de l'instanciation du cb pour le client");
            ex.printStackTrace();
        }
    }
}