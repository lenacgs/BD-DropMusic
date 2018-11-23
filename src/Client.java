import java.io.IOException;
import java.rmi.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class Client extends UnicastRemoteObject {
    private RMIServices RMI;
    private int port;
    private String RMIhost;
    private int RMIport;


    private Client() throws RemoteException { }

    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        Client client = new Client();
        client.RMIhost = args[0];
        client.RMIport = Integer.parseInt(args[1]);
        client.RMIConnection();
    }

    private void RMIConnection() {
        try {
            RMI = (RMIServices) LocateRegistry.getRegistry(RMIhost, RMIport).lookup("RMIServer");
            port = RMI.hello();
        } catch (RemoteException | NotBoundException exc) {
            retryRMIConnection();
        }
    }

    private void retryRMIConnection(){

        while(true){
            try {

                Thread.sleep(1000);
                RMI = (RMIServices) LocateRegistry.getRegistry(RMIhost, RMIport).lookup("RMIServer");
                port = RMI.hello();
                break;

            } catch (RemoteException | NotBoundException e) {
                System.out.print(".");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
