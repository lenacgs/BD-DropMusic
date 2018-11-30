import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.IOException;
import java.rmi.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Scanner;


public class Client extends UnicastRemoteObject {
    private RMIServices RMI;
    private int port;
    private String RMIhost;
    private int RMIport;
    private Scanner sc;
    private String username, password;
    private int editor;


    private Client() throws RemoteException {
        sc = new Scanner(System.in);
    }

    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        Client client = new Client();
        client.RMIhost = args[0];
        client.RMIport = Integer.parseInt(args[1]);
        client.RMIConnection();
        client.menu();
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

    private void menu() {
        int option = 0;
        System.out.println();
        System.out.println("———————————");
        System.out.println("| WELCOME! |");
        System.out.println("———————————");

        while (true) {
            option = 0;
            System.out.println();
            System.out.println(" ———————————————");
            System.out.println("| 0) Exit       |");
            System.out.println("| 1) Register   |");
            System.out.println("| 2) Login      |");
            System.out.println(" ———————————————");

            try {
                option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
            } catch (NumberFormatException exc) {
                System.out.println("-!- Invalid input");
            }

            if (option == 2 && login()) { //se o login teve sucesso
                mainMenu();
            }

            else if (option == 1 && register()) { //se o registo teve sucesso
                mainMenu();
            }

            else if (option == 3) {
                System.exit(0);
            }

            else {
                System.out.println("-!- Invalid input");
            }
        }
    }

    private void mainMenu() {
        int option = 0;
        while (true) {

            System.out.println();
            System.out.println(" ——————");
            System.out.println("| MENU |");
            System.out.println(" ——————\n");
            System.out.println("0) Exit");
            System.out.println("1) Insert new music");
            System.out.println("2) Insert new artist");
            System.out.println("3) Insert new album");
            System.out.println("4) Modify info - music");
            System.out.println("5) Modify info - artist");
            System.out.println("6) Modify info - album");
            System.out.println("7) Upload music file");
            System.out.println("8) Share music file");
            System.out.println("9) Download music file");
            System.out.println("10) Find music");
            System.out.println("11) Album info");
            System.out.println("12) Write album review");
            System.out.println("13) Editor privileges");
            System.out.println("14) Playlists");

            try {
                option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
            } catch (NumberFormatException exc) {
                System.out.println("-!- Invalid input");
            }

            if (option == 0) {
                return;
            }

            else if (option == 1 && editor == 1) { //inserir nova música na BD
                insertMusic();
            }

            else if (option == 3 && editor == 1) { //inserir novo artista na BD
                insertAlbum();
            }
            else if (option == 7) { //upload de ficheiro
                uploadFile();
            }

            else if (option == 8) { //partilha de ficheiro musical com outro user
                shareFile();
            }
            else if (option == 12) {
                writeReview();
            }
            else if (option == 13 && editor == 1) {
                changePerks();
            }
            else if (option == 14) {
                playlists();
            }
        }
    }

    private void shareFile() {
        ArrayList<String[]> files = new ArrayList<>();
        try {
            files = RMI.getFiles(username); //lista de ficheiros a que o user tem acesso
        } catch (RemoteException e) {
            retryRMIConnection();
        }

        for (String[] file:files) {
            System.out.println(file[0] + " - " + file[1] + " by " + file[2]);
        }

        System.out.println("\nInsert the ID of the music for the file you want to share: ");
        int musicID = Integer.parseInt(sc.nextLine());
        System.out.println("Insert the username of the user you want to share this file with: ");
        String sharing = sc.nextLine();

        try {
            boolean verifier = RMI.shareFile(username, musicID, sharing);
        } catch (RemoteException e) {
            retryRMIConnection();
        }

    }

    private void uploadFile() {
        int music;
        String path;
        boolean verifier;

        System.out.println("Music ID for the file you're uploading: ");
        music = Integer.parseInt(sc.nextLine());
        System.out.println("Path to file: ");
        path = sc.nextLine();

        try {
            verifier = RMI.uploadFile(username, music, path);
        } catch (RemoteException e) {
            retryRMIConnection();
        }

    }

    private void writeReview() {
        System.out.println("Insert album ID: ");
        int album = Integer.parseInt(sc.nextLine());
        System.out.println("Write your review: ");
        String review = sc.nextLine();
        System.out.println("Rate this album (0-10): ");
        int rate = Integer.parseInt(sc.nextLine());

        try {
            boolean verifier = RMI.writeReview(username, album, review, rate);
        } catch (RemoteException e) {
            retryRMIConnection();
        }
    }

    private void playlists() {
        System.out.println("\n ———————————");
        System.out.println("PLAYLISTS");
        System.out.println(" ———————————\n");
        ArrayList<String[]> playlists = new ArrayList<>();
        String name;
        int musicCount;
        int [] musicIDs;
        boolean verifier, priv;

        try {
            playlists = RMI.getPlaylists(username);
        } catch (RemoteException exc) {
            retryRMIConnection();
        }
        int i=1;
        for (String[] obj : playlists) {
            System.out.println(i+") " +obj[0]+" by " + obj[1]);
            i++;
        }

        System.out.println("\nDo you want to...\n1) Create new playlist\n2) See one of the existing playlists");
        int option = Integer.parseInt(sc.nextLine());

        if (option == 1) {
            System.out.println("Playlist name: ");
            name = sc.nextLine();
            System.out.println("Private? (true/false): ");
            priv = Boolean.parseBoolean(sc.nextLine());
            System.out.println("How many musics do you want to add?: ");
            musicCount = Integer.parseInt(sc.nextLine());
            musicIDs = new int[musicCount];
            for (int j=0; j<musicCount; j++) {
                System.out.println("Music ID:");
                musicIDs[j] = Integer.parseInt(sc.nextLine());
            }

            try {
                verifier = RMI.newPlaylist(username, name, priv, musicIDs);
            } catch (RemoteException e) {
                retryRMIConnection();
            }

        }
        if (option == 2) {
            System.out.println("Insert playlist index: ");
            option = Integer.parseInt(sc.nextLine());

            try {
                ArrayList<String[]> musics = RMI.getPlaylist(playlists.get(option-1)[0], playlists.get(option-1)[1]);
            } catch (RemoteException e) {
                retryRMIConnection();
            }
        }

    }

    private void changePerks(){
        String username;
        boolean verifier;

        System.out.println("\n ————————————————");
        System.out.println("INSERT USERNAME");
        System.out.println(" ————————————————\n");
        username = sc.nextLine();

        try{
            verifier = RMI.changePerks(username);
        }catch(RemoteException exc){
            retryRMIConnection();
        }
        if(verifier = true){
            System.out.println("Changed " + username + " perks successfully!");
        }else{
            System.out.println("Failed to change " + username + "perks!");
        }

    }

    private void insertAlbum() {
        String title, description, genre, publisher;
        int year, musicCount;
        int musicIDs[];
        boolean verifier;

        System.out.println("\n ————————————————");
        System.out.println("INSERT NEW ALBUM");
        System.out.println(" ————————————————\n");
        System.out.println("Title: ");
        title = sc.nextLine();
        System.out.println("Description: ");
        description = sc.nextLine();
        System.out.println("Year of publication: ");
        year = Integer.parseInt(sc.nextLine());
        System.out.println("Genre: ");
        genre = sc.nextLine();
        System.out.println("Publisher: ");
        publisher = sc.nextLine();
        System.out.println("How many musics do you want to add to this album?: ");
        musicCount = Integer.parseInt(sc.nextLine());
        musicIDs = new int[musicCount];
        for (int i=0; i<musicCount; i++) {
            System.out.println("Music ID: ");
            musicIDs[i] = Integer.parseInt(sc.nextLine());
        }

        while (true) {
            try {
                verifier = RMI.insertAlbum(title, year, description, genre, musicIDs, publisher);
                break;
            } catch (RemoteException exc) {
                retryRMIConnection();
            }
        }




    }

    private void insertMusic() {
        String title, lyrics, album;
        float duration;
        boolean verifier = false;
        int interpreter_id, composer_id, album_id;

        System.out.println("\n ————————————————");
        System.out.println("INSERT NEW MUSIC");
        System.out.println(" ————————————————\n");
        System.out.println("Title: ");
        title = sc.nextLine();
        System.out.println("Duration: ");
        duration = Float.parseFloat(sc.nextLine());
        System.out.println("Lyrics: ");
        lyrics = sc.nextLine();
        System.out.println("Composer ID: ");
        composer_id = Integer.parseInt(sc.nextLine());
        System.out.println("Interpreter ID: ");
        interpreter_id = Integer.parseInt(sc.nextLine());
        System.out.println("Album ID: ");
        album_id = Integer.parseInt(sc.nextLine());

        while(true) {
            try {
                verifier = RMI.insertMusic(username, password, title, duration, lyrics, interpreter_id, composer_id, album_id);
                break;
            } catch (RemoteException exc) {
                retryRMIConnection();
            }
        }
    }

    private boolean register() {
        String username, password;
        int verifier = 0;

        while (true) {
            System.out.println("\n——————————");
            System.out.println("| REGISTER |");
            System.out.println("——————————");
            System.out.println("\n0) Exit");

            System.out.print("\nUsername: ");
            username = sc.nextLine().replaceAll("^[,\\s]+", "");
            if (username.equals("0")) break;
            if (username.contains(" ")) {
                System.out.println("-!- Username cannot contain spaces");
                continue;
            }

            System.out.print("\nPassword: ");
            password = sc.nextLine().replaceAll("^[,\\s]+", "");
            if (password.equals("0")) break;
            if (password.contains(" ")) {
                System.out.println("-!- Password cannot contain spaces");
                continue;
            }

            while(true) {
                try {
                    verifier = RMI.register(username, password);
                    break;
                } catch (RemoteException exc) {
                    retryRMIConnection();
                }
            }

            if (verifier == 0) {
                System.out.println("-!- User register failed");
                continue;
            }

            else if (verifier > 0) { //1=editor, 2=normal
                System.out.println("User register successful");
                this.username = username;
                this.password = password;
                this.editor = verifier;
                return true;
            }
        }
        return false;
    }

    private boolean login() {
        String username, password;
        int verifier = 0;
        while (true) {
            System.out.println("\n————————");
            System.out.println("| LOGIN |");
            System.out.println("————————");
            System.out.println("\n0) Exit");

            System.out.print("\nUsername: ");
            username = sc.nextLine().replaceAll("^[,\\s]+", "");
            if (username.equals("0")) break;
            if (username.contains(" ")) {
                System.out.println("-!- Username cannot contain spaces");
                continue;
            }

            System.out.print("\nPassword: ");
            password = sc.nextLine().replaceAll("^[,\\s]+", "");
            if (password.equals("0")) break;
            if (password.contains(" ")) {
                System.out.println("-!- Password cannot contain spaces");
                continue;
            }

            while(true) {
                try {
                    verifier = RMI.login(username, password);
                    break;
                } catch (RemoteException exc) {
                    retryRMIConnection();
                }
            }

            if (verifier == 0) {
                System.out.println("-!- User login failed");
                return false;
            }
            else if (verifier > 0) { //1=editor, 2=normal
                System.out.println("User login successful!");
                this.password = password;
                this.username = username;
                this.editor = verifier;
                return true;
            }
        }
        return false;
    }
}
