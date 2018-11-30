import java.rmi.*;
import java.sql.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;


public class Server extends UnicastRemoteObject implements RMIServices {
    private Connection connection = null; //connection to the database
    private int port = 7001;
    private static RMIServices RMIServices;
    int clientPort = 7000;

    public Server () throws RemoteException, InterruptedException {
        if (connectDatabase()) System.out.println("Database is ready!!");
    }

    public static void main(String[] args) {
        try {
            RMIServices = new Server();
            createRegistry();
        } catch (RemoteException | InterruptedException exc) {
            exc.printStackTrace();
        }
    }

    private static void createRegistry() throws RemoteException, InterruptedException { //creates registry of new RMIServer on port 7000
        int port = 7000;
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("RMIServer", RMIServices);
            System.out.println("RMIServer is up!");
        } catch (AccessException exc) {
            System.out.println("AccessException: " + exc.getMessage());
        }
    }

    public int hello() throws RemoteException {
        clientPort++;
        System.out.println("New client has connected!");
        return clientPort;
    }

    public int login(String username, String password) throws RemoteException {
        String query = "SELECT username, editor FROM user WHERE username=\"" + username +"\" AND password=\"" + password + "\";";
        ResultSet res = DBQuery(query);
        int privileges = 0;

        try {
            if (res.next()) { //significa que o user existe
                System.out.println("O user existe");
                privileges = res.getInt(2);
            }
            else { //res não tem nada = o user não existe
                return privileges;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return privileges;

    }

    public ArrayList<String[]> getPlaylists(String username) {
        String query = "SELECT name, user_username FROM playlist WHERE private=false OR user_username=\""+username+"\";";
        ArrayList<String[]> message = new ArrayList<>();

        ResultSet res = DBQuery(query);
        int i=0;

        try {
            while (res.next()) {
                String [] obj = new String[2];
                obj[0] = res.getString("name");
                obj[1] = res.getString("user_username");

                message.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return message;
    }

    public boolean newPlaylist(String username, String name, boolean priv, int [] musicIDs) {
        String query = "INSERT INTO playlist (name, private, user_username) VALUES (\""+name+"\", "+priv+", \""+username+"\");";

        if (!DBUpdate(query)) return false;

        for (int music : musicIDs) {
            query = "INSERT INTO playlist_music (playlist_name, playlist_user_username, music_id) VALUES (\""+name+"\", \""+username+"\", "+music+");";
            if (!DBUpdate(query)) return false;
        }

        return true;
    }

    public ArrayList<String[]> getPlaylist(String name, String username) {
        return null;
    }

    public boolean writeReview(String username, int album, String review, int rate) {
        String query = "INSERT INTO review (rate, text, album_id, user_username) values ("+rate+", \""+review+"\", "+album+", \""+username+"\");";

        if (!DBUpdate(query)) return false;
        return true;
    }

    public int register(String username, String password) throws RemoteException {
        String query = "SELECT count(*) FROM user";
        ResultSet res = DBQuery(query);
        int privileges = 0;

        try {
            res.next();
            if (res.getInt("count(*)") == 0) { //se não houver registos na tabela user, significa que este user é o admin=editor
                privileges = 1;
            }
            else { //já existem users na tabela, então este user é normal
                privileges = 2;
            }
        } catch(SQLException exc) { }

        query = "INSERT INTO user (username, password, editor) VALUES (\"" + username + "\", \"" + password + "\", " + privileges + ");";

        if (!DBUpdate(query)) return 0;
        return privileges;
    }

    public int insertAlbum(String title, int year, String description, String genre, int interpreter_id, String publisher) {
        int album_id = -1;

        String query = "Select count(*)+1 from album";
        ResultSet res = DBQuery(query);
        try{
            res.next();
            album_id = res.getInt("count(*)+1");
        }catch (SQLException e){}

        query = "INSERT INTO album (title, yearofpublication, description, genre, interpreter_id) VALUES (\""+title+"\", "+year+", \""+description+"\", \""+genre+"\", " + interpreter_id + ");";

        if (!DBUpdate(query)) return -1;

        //associar publisher ao álbum
        query = "INSERT INTO album_publisher (album_id, publisher_name) VALUES ((SELECT ID FROM album WHERE title=\""+title+ "\" AND description=\"" + description+"\"), \"" + publisher+"\");";
        if (!DBUpdate(query)) return -1;

        return album_id;

    }

    public String[] searchMusic(String name){
        String query = "Select count(*) FROM music, music_interpreter, interpreter WHERE interpreter.name LIKE \"%" + name + "%\" AND music_interpreter.interpreter_id = interpreter.id and music.id = music_interpreter.music_id";
        ResultSet res = DBQuery(query);
        int n = 0;
        try{
            res.next();
            n = res.getInt("count(*)");
        }catch(SQLException e){
        }
        if(n == 0)return null;
        query = "Select music.id, music.title, interpreter.name FROM music, music_interpreter, interpreter WHERE interpreter.name LIKE \"%" + name + "%\" AND music_interpreter.interpreter_id = interpreter.id and music.id = music_interpreter.music_id";
        res = DBQuery(query);

        String reply[] = new String[n];
        int i = 0;
        try{
            res.next();
            while(!res.isAfterLast()){
                reply[i] = "";
                reply[i++] += res.getInt("music.id") + "/" + res.getString("music.title") + "/" + res.getString("interpreter.name");
                res.next();
            }
        }catch (SQLException exc){}
        return reply;
    }

    public String[] searchComposer(String name){
        String query = "Select count(*) from composer where name like \"%" + name + "%\";";
        ResultSet res = DBQuery(query);
        int n = 0;
        try{
            res.next();
            n = res.getInt("count(*)");
        }catch(SQLException e){
        }
        if(n == 0)return null;
        query = "Select id, name, description from composer where name like  \"%" + name + "%\";";
        res = DBQuery(query);

        String reply[] = new String[n];
        int i = 0;
        try{
            res.next();
            while(!res.isAfterLast()){
                reply[i] = "";
                reply[i++] += res.getInt("id") + "/" + res.getString("name") + "/" + res.getString("description");
                res.next();
            }
        }catch (SQLException exc){}
        return reply;
    }



     public String[] searchInterpreter(String name){
         String query = "Select count(*) from interpreter where name like \"%" + name + "%\";";
         ResultSet res = DBQuery(query);
         int n = 0;
         try{
             res.next();
             n = res.getInt("count(*)");
         }catch(SQLException e){
         }
         if(n == 0)return null;
         query = "Select id, name, description from interpreter where name like  \"%" + name + "%\";";
         res = DBQuery(query);

         String reply[] = new String[n];
         int i = 0;
         try{
             res.next();
             while(!res.isAfterLast()){
                 reply[i] = "";
                 reply[i++] += res.getInt("id") + "/" + res.getString("name") + "/" + res.getString("description");
                 res.next();
             }
         }catch (SQLException exc){}
         return reply;
    }

    public String[] searchAlbum(String name, int interpreter_id){
        String query = "Select count(*) from album, interpreter where album.title like  \"%" + name + "%\"and interpreter.id = album.interpreter_id and interpreter.id = " + interpreter_id +";";
        ResultSet res = DBQuery(query);
        int n = 0;
        try{
            res.next();
            n = res.getInt("count(*)");
        }catch(SQLException e){
        }
        if(n == 0)return null;
        query = "Select album.id, album.title, interpreter.name from album, interpreter where album.title like  \"%" + name + "%\" and interpreter.id = album.interpreter_id and interpreter.id = " + interpreter_id+ ";";
        res = DBQuery(query);

        String reply[] = new String[n];
        int i = 0;
        try{
            res.next();
            while(!res.isAfterLast()){
                reply[i] = "";
                reply[i++] += res.getInt("album.id") + "/" + res.getString("album.title") + "/" + res.getString("interpreter.name");
                res.next();
            }
        }catch (SQLException exc){}
        return reply;
    }

    public String[] searchAlbum(String name){

        String query = "Select count(*) from album, interpreter where album.title like  \"%" + name + "%\"and interpreter.id = album.interpreter_id;";
        ResultSet res = DBQuery(query);
        int n = 0;
        try{
            res.next();
            n = res.getInt("count(*)");
        }catch(SQLException e){
        }
        query = "Select album.id, album.title, interpreter.name from album, interpreter where album.title like  \"%" + name + "%\" and interpreter.id = album.interpreter_id";
        res = DBQuery(query);
        if(n == 0)return null;
        String reply[] = new String[n];
        int i = 0;
        try{
            res.next();
            while(!res.isAfterLast()){
                reply[i] = "";
                reply[i++] += res.getInt("album.id") + "/" + res.getString("album.title") + "/" + res.getString("interpreter.name");
                res.next();
            }
        }catch (SQLException exc){return null;}
        return reply;
    }

    public String[] getAlbumInfo(int id){
            String[] reply = new String[2];
            String query = "SELECT title from music, album_music WHERE album_id =" + id + " and music.id = album_music.music_id";
            ResultSet res = DBQuery(query);
            try{
                res.next();
                while(!res.isAfterLast()){
                    reply[0] = "";
                    reply[0] += res.getString("title") + "\n";
                }
            }catch (SQLException exc){
            }
            query = "SELECT genre, yearofpublication from album where album.id = " + id + ";";
            res = DBQuery(query);
            try{
                res.next();
                reply[1] = "";
                reply[1] += res.getString("genre") + "/" + res.getInt("yearofpublication") + "/";
            }catch (SQLException exc){

            }
            query = "SELECT round(AVG(rate),2) from review WHERE album_id = " + id + ";";
            res =DBQuery(query);
            try{
                res.next();
                reply[1] += res.getString("round(AVG(rate),2)");
            }catch (SQLException e){
                reply[1] += "NaN";
            }
            return reply;
    }


    public int createBand(String name, String description, String date){
        int id = 1;
        String query = "Select MAX(id)+1 from interpreter;";
        ResultSet res = DBQuery(query);
        try{
            res.next();
            id = res.getInt("MAX(id)+1");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        query = "Insert into interpreter (name, description) values (" + name + " , " + description + ");";
        if(!DBUpdate(query)) return -1;
        query = "Insert into band (interpreter_id, dateofcreation) values (" + id + " , " + date + ");";
        if(!DBUpdate(query)) return -1;
        return id;
    }

    public int createArtist(String name, String description, String date){
        int id = 1;
        String query = "Select MAX(id)+1 from interpreter;";
        ResultSet res = DBQuery(query);
        try{
            res.next();
            id = res.getInt("MAX(id)+1");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        query = "Insert into interpreter (name, description) values (" + name + " , " + description + ");";
        if(!DBUpdate(query)) return -1;
        query = "Insert into artist (interpreter_id, birthday) values (" + id + " , " + date + ");";
        if(!DBUpdate(query)) return -1;
        return id;
    }

    public int createComposer(String name, String description){
        int id = 1;
        String query = "Select MAX(id)+1 from composer;";
        ResultSet res = DBQuery(query);
        try{
            res.next();
            id = res.getInt("MAX(id)+1");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        query = "INSERT INTO composer (name, description) VALUES ('" + name + "' , '" + description + "');";
        if(!DBUpdate(query)) return -1;
        return id;
    }

    public boolean insertMusic(String username, String password, String title, float duration, String lyrics, int interpreter_id, int composer_id, int album_id) throws RemoteException {
        String query = "INSERT INTO music (title, duration, lyrics) VALUES ('"+title+"' , "+duration+", '"+lyrics + "');";

        if (!DBUpdate(query)) return false;

        //get music ID
        String query2 = "(SELECT ID FROM music WHERE title=\""+title+"\" AND lyrics=\""+lyrics+"\")";

        //associar o intérprete com a música
        query = "INSERT INTO music_interpreter (music_id, interpreter_id) VALUES " +
                "("+query2+ ", " + interpreter_id+");";

        if (!DBUpdate(query)) return false;

        //associar o compositor com a música
        query = "INSERT INTO composer_music (composer_id, music_id) VALUES ("+composer_id+", " + query2+");";

        if (!DBUpdate(query)) return false;

        //associar o álbum à música
        query = "INSERT INTO album_music (album_id, music_id) VALUES ("+album_id+", "+query2+");";

        if (!DBUpdate(query)) return false;
        return true;
    }

    public boolean changePerks(String username){
        String query = "UPDATE user SET editor = 1 where username = " + username;
        return (DBUpdate(query));
    }



    public boolean connectDatabase() { //returns true if connection to DB is successful; false if else
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException exc) {
            System.out.println("JDBC Driver not found");
            return false;
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/DropMusicDatabase",
                    "DBUser", "password");
        } catch (SQLException exc) {
            System.err.println("Database connection failed");
            exc.printStackTrace();
            return false;
        }

        System.out.println("Connected to DropMusicDatabase");
        return true;
    }

    public ResultSet DBQuery(String query) {

        try {
            Statement statement = connection.createStatement();

            if (statement == null) {
                System.out.println("Error creating statement " + query);
                return null;
            }

            System.out.println("Success executing " + query);
            return statement.executeQuery(query);

        }catch (SQLException exc) {
            System.out.println("Error executing " + query);
            exc.printStackTrace();
            return null;
        }
    }

    public boolean DBUpdate(String query) { //used for INSERT, DELETE or UPDATE statements - returns true if successful, if else returns false
        try {
            Statement statement = connection.createStatement();

            if (statement == null) {
                //falta resolver
                System.out.println("Error creating statement " + query);
                return false;
            }

            statement.executeUpdate(query);

        } catch (SQLException exc) {
            System.out.println("Error executing " + query);
            return false;
        }

        System.out.println("Success executing " + query);
        return true;
    }
}
