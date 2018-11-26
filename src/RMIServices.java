import java.rmi.*;

public interface RMIServices extends Remote {
    int hello() throws RemoteException;
    int login(String username, String password) throws RemoteException;
    int register(String username, String password) throws RemoteException;
}
