package server.multi;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import core.CoreLinda;
import server.IRemoteCallback;
import server.LocalToRemoteCallback;
import server.ILindaServer;
import utils.Linda;
import utils.Tuple;

import static java.lang.Integer.parseInt;

/* TODO : take doit bloquer le client et pas le serveur */
public class MultiServeur extends UnicastRemoteObject implements ILindaServer {

    private CoreLinda localLinda;
    private ArrayList<ILindaServer> serveursConectes;
    private String adressePerso;

    private boolean verbeux = true;

    /** Main pour lancer le serveur
     * @param args [0] adresse serveur, args [1] port du serveur
     *             args[2 ... n] adresses autres serveurs
     */
    public static void main(String[] args) {
        int port = 4000;
        String s = "//localhost:4000/LindaServeur";
        if (args.length>1) {
            s = args[0];
            port = parseInt(args[1]);
        }
        MultiServeur ms = launchServeur(s,port);
        for(int i=2;i<args.length;i++){
            try {
                ms.connecterServeur(args[i], true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** création de l'objet Multi-serveur utilisé dans launchServeur
     * @param adresse adresse sur laquelle est le serveur
     * @throws RemoteException interface
     */
    private MultiServeur(String adresse) throws RemoteException{
        this.adressePerso = adresse;
        this.localLinda = new CoreLinda();
        this.serveursConectes = new ArrayList<>();
        this.verbeux = false;
    }

    /** sous fonction pour lancer le serveur
     * @param adresse adresse serveur en 0
     */
    public static MultiServeur launchServeur(String adresse, int port) {
        MultiServeur multiServeur = null;
        System.out.println("lancement du serveur");
        try {
            multiServeur = new MultiServeur(adresse);
            Registry registry = LocateRegistry.createRegistry(port);
            Naming.rebind(adresse, multiServeur);
            System.out.println("Serveur lancé à l'adresse : " + adresse);
        } catch (Exception e) {
            System.out.println("Échec du lancement du serveur");
            e.printStackTrace();
        }
        return (MultiServeur) multiServeur;
    }

    /** Se connecte au serveur d'adresse donnée
     * @param dest adresse serveurs
     * @param retour si l'autre serveur doit se connecter aussi
     */
    public void connecterServeur(String dest, boolean retour) throws RemoteException{
        System.out.println("Connexion au serveur " + dest + " ...");
        try {
            ILindaServer ls = (ILindaServer) Naming.lookup(dest);
            this.serveursConectes.add(ls);
            System.out.println("Connecté au serveur " + dest);
            if (retour)
                ls.connecterServeur(this.adressePerso, false);
        } catch (Exception ex) {
            System.out.println("La connexion au serveur " + dest + " a échoué");
            ex.printStackTrace();
        }
    }

    /** Écrit sur place
     * @param t Tuple à écrire
     * @throws RemoteException interface
     */
    public void write(Tuple t) throws RemoteException{
        if (verbeux) {
            System.out.println("Écriture du Tuple :" + t);
        }
        this.localLinda.write(t);
    }

    /** take ici puis si rien sur autre serveurs, si rien trouvé se bloque ici
     * TODO : tester marche + blocage seulement client
     * @param template que l'on cherche
     * @return Tuple qui match
     * @throws RemoteException interface
     */
    public Tuple take(Tuple template) throws RemoteException{
        Tuple solution = this.tryTake(template,true);
        if (solution == null) {
            solution = this.localLinda.take(template);
        }
        return solution;
    }

    /** read ici puis si rien sur autre serveurs, si rien trouvé se bloque ici
     * TODO : tester marche + blocage seulement client
     * @param template que l'on cherche
     * @return Tuple qui match
     * @throws RemoteException interface
     */
    public Tuple read(Tuple template) throws RemoteException{
        Tuple solution = this.tryRead(template, true);
        if (solution == null) {
            solution = this.localLinda.read(template);
        }
        return solution;
    }

    /** tryTake sur le serveur local, mais s'il n'y a rien, cherche à take chez les voisins
     * @param template Tuple à prendre
     * @return retour le Tuple pris
     * @throws RemoteException interface
     */
    public Tuple tryTake(Tuple template, boolean propagation) throws RemoteException{
        Tuple solution = this.localLinda.tryTake(template);
        if (propagation)
        {
            if (solution == null) {
                for (ILindaServer s: serveursConectes) {
                    solution = s.tryTake(template,false);
                    if (solution != null) {
                        break;
                    }
                }
            }
        }
        return solution;
    }

    /** tryRead sur place, si non concluent tryRead sur les autres
     * @param template Tuple à read
     * @return le tuple read
     * @throws RemoteException interface
     */
    public Tuple tryRead(Tuple template,boolean propagation) throws RemoteException{
        if (verbeux) System.out.println("Recherche sur place de : " + template);
        Tuple solution = this.localLinda.tryRead(template);
        if (propagation)
        {
            if (solution == null) {
                if (verbeux) System.out.println("Non trouvé sur place : " + template);
                for (ILindaServer s: serveursConectes) {
                    if (verbeux) System.out.println("Recherche de : " + template +  " sur le serveur : " + s);
                    solution = s.tryRead(template,false);
                    if (solution != null) {
                        if (verbeux) System.out.println("A lu : " + template + " sur le serveur : " + s);
                        break;
                    }
                    if (verbeux) System.out.println(template + "non trouvé sur " + s);
                }
            }
        }
        return solution;
    }


    /** takeAll sur tous les serveurs
     * @param template Tuple à prendre
     * @param propagation true pour chercher sur les autres
     * @return retourne l'ensemble de ce qui a été retiré
     * @throws RemoteException interface
     */
    public Collection<Tuple> takeAll(Tuple template, boolean propagation) throws RemoteException{
        if (verbeux) System.out.println("TakeAll : " + template);
        Collection<Tuple> c = this.localLinda.takeAll(template);
        if (propagation) {
            for (ILindaServer s: serveursConectes) {
                if (verbeux) System.out.println("takeAll : " + template + "sur le serveur :" + s);
                try {
                    c.addAll(s.takeAll(template,false));
                    if (verbeux) System.out.println("pris : " + c + "sur" + s);
                } catch (Exception e) {
                    if (verbeux) System.out.println("Échec de takeAll " + template + " sur le serveur" + s);
                    e.printStackTrace();
                }
            }
        }
        return c;
    }

    /** readAll sur tous les serveurs
     * @param template recherché
     * @param propagation true pour chercher sur les autres
     * @return collection matchant template
     * @throws RemoteException interface
     */
    public Collection<Tuple> readAll(Tuple template, boolean propagation) throws RemoteException{
        if (verbeux) System.out.println("readAll : " + template);
        Collection<Tuple> c = this.localLinda.readAll(template);
        if (propagation) {
            for (ILindaServer s: serveursConectes) {
                if (verbeux) System.out.println("readAll : " + template + "sur le serveur : " + s);
                try {
                    c.addAll(s.readAll(template,false));
                    if (verbeux) System.out.println("tout lu : " + c + " sur " + s);
                } catch (Exception e) {
                    if (verbeux) System.out.println("Erreur lecture de " + template + " sur le serveur : "+ s);
                    e.printStackTrace();
                }
            }
        }
        return c;
    }

    /** TODO :
     * @param mode jsp
     * @param timing jsp
     * @param template jsp
     * @param callbackRmt jsp
     * @throws RemoteException interface
     */
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, IRemoteCallback callbackRmt, boolean propagation) throws RemoteException{
        LocalToRemoteCallback cb = new LocalToRemoteCallback(callbackRmt);
        this.localLinda.eventRegister(mode, timing, template, cb);
        if (propagation)
        {
            for (ILindaServer s: serveursConectes)
            {
                s.eventRegister(mode,timing,template, callbackRmt,false);
            }
        }

    }

    public void debug(String prefix) throws RemoteException{
        this.verbeux = true;
        this.localLinda.debug(prefix);
    }

}
