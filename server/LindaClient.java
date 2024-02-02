package server;

import utils.Callback;
import utils.Tuple;
import utils.Linda;

import java.rmi.Naming;
import java.util.Collection;


/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    /* on se connecte au serveur à la création du client et on garde la co tant que l'objet existe */
    private LindaServer lindaServ;
    private boolean verbeux = true;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
            if (verbeux)
                System.out.println("Connexion au serveur " + serverURI + " ...");
            this.lindaServ = (LindaServer) Naming.lookup(serverURI);
            if (verbeux)
                System.out.println("Connecté au serveur :" + serverURI);
        } catch (Exception ex) {
            System.out.println("La connexion a échoué");
            ex.printStackTrace();
        }
    }

    /** Initializes the Linda implementation.
     *  the URI of the server is : //localhost:4000/LindaServer
     */
    public LindaClient() {
        new LindaClient("//localhost:4000/LindaServer");
    }

    /* ici on appelle les méthodes sur l'objet à distance */
    public void write(Tuple t) {
        try {
            lindaServ.write(t);
        }catch (Exception I)
        {
            I.printStackTrace();
        }

    }

    public Tuple take(Tuple template){
        try {
            return lindaServ.take(template);
        }catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;
    }

    public Tuple read(Tuple template){
        try {
            return lindaServ.read(template);
        }
        catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;
    }

    public Tuple tryTake(Tuple template){
        try {
            return lindaServ.tryTake(template,true);
        }
        catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;

    }

    public Tuple tryRead(Tuple template){
        try {
            return lindaServ.tryRead(template,true);
        }
        catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;

    }

    public Collection<Tuple> takeAll(Tuple template){
        try {
            return lindaServ.takeAll(template,true);
        }catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;

    }

    public Collection<Tuple> readAll(Tuple template){
        try {
            return lindaServ.readAll(template,true);
        }
        catch (Exception I)
        {
            I.printStackTrace();
        }
        return null;
    }

    /* callback est un callbackRemote obligatoirement */
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, Callback callback){
        try {
            IRemoteCallback callbackremote = new RemoteCallback(callback);
            lindaServ.eventRegister(mode, timing, template, callbackremote,true);
        }
        catch (Exception I) {
            I.printStackTrace();
        }
    }

    public void debug(String prefix){
        this.verbeux = true;
        if (verbeux)
            System.out.println("Client mis en mode debug avec le prefix : " + prefix);
    }

}