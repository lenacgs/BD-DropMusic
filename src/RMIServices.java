import java.rmi.*;

public interface RMIServices extends Remote {
    int hello() throws RemoteException;
}
