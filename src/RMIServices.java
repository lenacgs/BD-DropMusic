import java.rmi.*;

public interface RMIServices extends Remote {
    int hello() throws RemoteException;
    int login(String username, String password) throws RemoteException;
    int register(String username, String password) throws RemoteException;
    boolean insertMusic(String username, String password, String title, float duration, String lyrics, int interpreter_id, int composer_id, int album_id) throws RemoteException;
}
