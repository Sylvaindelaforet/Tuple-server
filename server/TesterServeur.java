package server;

import utils.Callback;
import utils.Linda;
import utils.Tuple;
import utils.CallbackPrint;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import server.multi.LaunchMultiServer;

public class TesterServeur {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {
        LaunchMultiServer.main(new String[]{"8000", "//localhost:8000/MultiServeur"});
        LaunchMultiServer.main(new String[]{"8001", "//localhost:8001/MultiServeur1","//localhost:8000/MultiServeur"});
        LaunchMultiServer.main(new String[]{"8002", "//localhost:8002/MultiServeur2","//localhost:8000/MultiServeur","//localhost:8001/MultiServeur1"});


        // Le premier client est connecté au Multiserveur
        // Le deuxième client est connecté au Multiserveur
        // Le troisième client est connecté au MultiServeur1

        LindaClient lc1 = new LindaClient("//localhost:8000/MultiServeur");
        LindaClient lc2 = new LindaClient("//localhost:8000/MultiServeur");
        LindaClient lc3 = new LindaClient("//localhost:8001/MultiServeur1");


        System.out.println("Teste du write *******************");
        System.out.println("Write dans le client 1 connecté au multiserveur 1 le tuple 1,1");
        System.out.println("Write dans le client 1 connecté au multiserveur 1 le tuple 2,Hello");
        lc1.write(new Tuple(1,1));
        lc2.write(new Tuple(2, "Hello"));

        // Le troisième client demande un tuple Integer, String. Le mutliserveur1 doit donc propager la demande
        // Le resultat doit etre 2, "String"
        System.out.println("\n Teste de la propagation du read *******************");
        System.out.println("Le troisième client n'a pas de tuple de type int srting, il doit propager sa demande et lire 2 Hello  :");
        System.out.print(lc3.read(new Tuple(Integer.class, String.class)));
        System.out.println("\n Le troisième client n'a pas de tuple de type int int, il doit propager sa demande et lire 1 1  :");
        System.out.print(lc3.read(new Tuple(Integer.class, Integer.class))+"\n");
        System.out.println("\n Teste de la propagation du tryTake et tryRead *******************");
        System.out.println(" tryTake un tuple de type int int : " + lc3.tryTake(new Tuple(Integer.class, Integer.class)));
        System.out.println(" tryRead un tuple de type int int sauf que comme on l'a tryTake on ne doit pas l'avoir: " + lc3.tryRead(new Tuple(Integer.class, Integer.class)));

        Callback test = new CallbackPrint();

        lc3.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(String.class, Integer.class), test);
        System.out.println("Le troisième client a un eventRegister pour String et integer");
        Thread.sleep(200);
        System.out.println("Le premier client a écrit salut, 1 ");
        lc1.write(new Tuple("Salut",7));
        Thread.sleep(100);
        System.out.println("Le premier client éssaye de lire le tuple qu'il a mis aprés le eventRegister : " + lc1.tryRead(new Tuple(String.class,Integer.class)));

    }
}
