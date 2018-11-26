import java.rmi.*;
import java.sql.*;
import java.rmi.server.*;
import java.rmi.registry.*;


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

    public int login(String username, String password) {
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

    public int register(String username, String password) {
        String query = "SELECT * FROM user";
        ResultSet res = DBQuery(query);
        int privileges = 0;

        try {
            if (!res.next()) { //se não houver registos na tabela user, significa que este user é o admin=editor
                privileges = 1;
            }
            else { //já existem users na tabela, então este user é normal
                privileges = 2;
            }
        } catch(SQLException exc) { }

        query = "INSERT INTO user (username, password, editor) VALUES (\"" + username + "\", \"" + password + "\", " + privileges + ");";
        DBUpdate(query);
        System.out.println("Returning " + privileges);
        return privileges;
    }


    public boolean connectDatabase() { //returns true if connection to DB is successful; false if else
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException exc) {
            System.out.println("JDBC Driver not found");
            return false;
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/DropMusicDB",
                    "DBUser", "password");
        } catch (SQLException exc) {
            System.err.println("Database connection failed");
            exc.printStackTrace();
            return false;
        }

        System.out.println("Connected to DropMusicDB");
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
            exc.printStackTrace();
            return false;
        }

        System.out.println("Success executing " + query);
        return true;
    }
}
