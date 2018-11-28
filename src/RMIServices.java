import java.rmi.*;
import java.util.ArrayList;

public interface RMIServices extends Remote {
    int hello() throws RemoteException;
    int login(String username, String password) throws RemoteException;
    int register(String username, String password) throws RemoteException;
    boolean insertMusic(String username, String password, String title, float duration, String lyrics, int interpreter_id, int composer_id, int album_id) throws RemoteException;
    boolean insertAlbum(String title, int year, String description, String genre, int[] musicIDs, String publisher) throws RemoteException;
    ArrayList<String[]> getPlaylists(String username) throws RemoteException;
    boolean newPlaylist(String username, String name, boolean priv, int [] musicIDs) throws RemoteException;
    public ArrayList<String[]> getPlaylist(String name, String username) throws RemoteException;
    boolean writeReview(String username, int album, String review, int rate) throws RemoteException;
}
