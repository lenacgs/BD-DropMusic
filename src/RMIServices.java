import java.rmi.*;
import java.sql.ResultSet;
import java.util.ArrayList;

public interface RMIServices extends Remote {
    int hello() throws RemoteException;
    int login(String username, String password) throws RemoteException;
    int register(String username, String password) throws RemoteException;
    boolean insertMusic(String username, String password, String title, float duration, String lyrics, int interpreter_id, int composer_id, int album_id) throws RemoteException;
    int insertAlbum(String title, int year, String description, String genre, int interpreter_id, String publisher) throws RemoteException;
    ArrayList<String[]> getPlaylists(String username) throws RemoteException;
    boolean newPlaylist(String username, String name, boolean priv, int [] musicIDs) throws RemoteException;
    ArrayList<String[]> getPlaylist(String name, String username) throws RemoteException;
    boolean writeReview(String username, int album, String review, int rate) throws RemoteException;
    boolean changePerks(String username) throws RemoteException;
    int createComposer(String name, String description) throws RemoteException;
    String[] searchComposer(String name) throws RemoteException;
    String[] searchInterpreter(String name) throws RemoteException;
    String[] searchAlbum(String name, int interpreter_id) throws RemoteException;
    String[] searchAlbum(String name) throws RemoteException;
    String[] getAlbumInfo(int id) throws RemoteException;
    String[] searchMusic(String name) throws RemoteException;
    int createBand(String name, String description, String date) throws RemoteException;
    int createArtist(String name, String description, String date) throws RemoteException;
}
