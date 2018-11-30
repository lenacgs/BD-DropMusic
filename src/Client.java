import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import sun.misc.REException;

import java.io.IOException;
import java.rmi.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
            System.out.println("9) Find music");
            System.out.println("10) Album info");
            System.out.println("11) Write album review");
            System.out.println("12) Editor privileges");
            System.out.println("13) Playlists");

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
            else if(option == 9){
                searchMusic();
            }
            else if(option == 10){
                albumDetails();
            }
            else if (option == 12 && editor == 1) {
                changePerks();
            }
            else if (option == 13) {
                playlists();
            }

            else if (option == 11) {
                writeReview();
            }

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

    private void albumDetails(){
        int id = -1;
        System.out.println("\n ———————————");
        System.out.println("ALBUM DETAILS");
        System.out.println(" ———————————\n");
        System.out.println("Album title:");
        String title = sc.nextLine();
        try{
            id = showSearchResults(RMI.searchAlbum(title));
            if(id > 0){
                String reply[] = RMI.getAlbumInfo(id);
                String[] aux = reply[1].split("/");
                System.out.println("Genre : " + aux[0]);
                System.out.println("Year of publication: " + aux[1]);
                System.out.println("Average rate: " + aux[2]);
                System.out.println("Songlist:");
                System.out.println(reply[0]);
            }
        }catch(RemoteException e){
            retryRMIConnection();
        }
    }

    private void searchMusic() {
        System.out.println("\n ———————————");
        System.out.println("FIND MUSIC");
        System.out.println(" ———————————\n");
        System.out.println("Interpreter name:");
        String name = sc.nextLine();
        try{
            showSearchResults2(RMI.searchMusic(name));
        }catch (RemoteException e){
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
        int year;
        int album_id, interpreter_id = -1;
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
        System.out.println("Interpreter:");
        while (interpreter_id < 0) {
            System.out.println("1 - Create a new interpreter\n2 - Find an existing interpreter");
            int reply;
            do {
                reply = Integer.parseInt(sc.nextLine());
                if (reply == 0) return;
            } while (reply != 2 && reply != 1);
            if (reply == 1) {
                interpreter_id = createInterpreter();
            } else {
                System.out.println("Interpreter name: ");
                String name = sc.nextLine();
                try {
                    interpreter_id = showSearchResults(RMI.searchInterpreter(name));
                    System.out.println(interpreter_id);
                } catch (RemoteException exc) {
                    retryRMIConnection();
                }
            }
        }
        while (true) {
            try {
                album_id = RMI.insertAlbum(title, year, description, genre, interpreter_id, publisher);
                break;
            } catch (RemoteException exc) {
                retryRMIConnection();
            }
        }
        if(album_id == -1)return;
        int count = 0;
        System.out.println("Add a songs to the album:");
        while(true){
            insertMusic(interpreter_id, album_id);
            System.out.println("Do you want to add more songs to the album?\n1 - Yes\n2 - No");
            int option = Integer.parseInt(sc.nextLine().replaceAll("\n", ""));
            if(option == 0)break;
        }


    }

    private int showSearchResults(String[] res){
        if(res == null) return -1;
        int i = 1, reply;
        for(int k = 0; k < res.length; k++){
            String[] aux = res[k].split("/");
            System.out.println(i++ + " - " + aux[1] + " (" + aux[2] + ")");
        }
        reply = -1;
        while(reply > i-1 || reply < 0){
            try{
                reply = Integer.parseInt(sc.nextLine().replaceAll("\n", ""));
            }catch(NumberFormatException exc){
                System.out.println("Please insert a valid option!");
            }
            if(reply == 0)return -1;
            else{
                String aux[] = res[reply-1].split("/");
                System.out.println(aux[0]);
                return Integer.parseInt(aux[0]);
            }
        }
        return -1;
    }

    private void showSearchResults2(String[] res){
        if(res == null) return;
        for(int k = 0; k < res.length; k++){
            String[] aux = res[k].split("/");
            System.out.println(aux[1] + " (" + aux[2] + ")");
        }
    }

    private int createComposer(){
        int verifier = -1;
        String name, description;
        System.out.println("Name: ");
        name = sc.nextLine().replaceAll("\n", "");
        System.out.println("Description: ");
        description = sc.nextLine().replaceAll("\n", "");
        while(true){
            try{
                verifier = RMI.createComposer(name, description);
                return verifier;
            }catch(RemoteException exc){
                retryRMIConnection();
            }
        }
    }

    private boolean checkMonth(int[] months, int month){
        for(int i = 0; i < months.length; i++){
            if(month == months[i]){
                return true;
            }
        }
        return false;
    }

    private boolean verifyDate(String date){
        int months31[] = {1,3,5,7,8,10,12};
        int months30[] = {2,4,6,9,11};
        String[] tokens = date.split("-");
        int year = Integer.parseInt(tokens[0]), month = Integer.parseInt(tokens[1]), day = Integer.parseInt(tokens[2]);
        if(year < 1900 || year > 2018){
            return false;
        }
        if(month > 12 || month < 1){
            return false;
        }
        if(checkMonth(months31, month) && (day > 31 || day < 1)){
            return false;
        }else if(checkMonth(months30, month) && (day > 30 || day < 1)){
            return false;
        }else if(year%4 ==  0 && month == 2 && (day > 29 || day < 1)){
            return false;
        }else if(year%4 != 0 && month == 2 && (day > 28 || day < 1)){
            return false;
        }
        return true;

    }


    private int createInterpreter(){
        int option, verifier = -1;
        do{
            System.out.println("Type of interpreter:\n1 - Solo artist\n2 - Band");
            option = sc.nextInt();
            if(option == 0)return -1;
        }while(option != 1 || option != 2);
        String name, description, date;
        System.out.println("Name: ");
        name = sc.nextLine();
        System.out.println("Description: ");
        description = sc.nextLine();
        do{
            if(option == 1){
                System.out.println("Date of birth: (yyyy-mm-dd");
            }else{
                System.out.println("Date of creation: (yyyy-mm-dd");
            }
            date = sc.nextLine();
            if(date.equals("0"))return -1;
        }while(!verifyDate(date));

        while(true){
            try{
                if(option == 1){
                    verifier = RMI.createArtist(name, description, date);
                }else{
                    verifier = RMI.createBand(name, description, date);
                }
                return verifier;
            }catch(RemoteException exc){
                retryRMIConnection();
            }
        }
    }

    private void insertMusic(int interpreter_id, int album_id){
        int composer_id = -1;
        String title, lyrics;
        float duration;
        boolean verifier = false;
        System.out.println("Title: ");
        title = sc.nextLine();
        System.out.println("Duration: ");
        while(true){
            try{
                duration = Float.parseFloat(sc.nextLine());
                break;
            }catch(NumberFormatException e){
                System.out.println("Please insert a valid option!");
            }
        }
        System.out.println("Lyrics: ");
        lyrics = sc.nextLine();
        System.out.println("Composer: ");
        while(composer_id < 0){
            System.out.println("1 - Create a new composer\n2 - Find an existing composer:");
            int reply;
            do{
                reply = Integer.parseInt(sc.nextLine().replaceAll("\n", ""));
                if(reply == 0)return;
            }while(reply != 2 && reply != 1);
            if(reply == 1){
                composer_id = createComposer();
            }else{
                System.out.println("Composer name: ");
                String name = sc.nextLine().replaceAll("\n", "");
                try{
                    composer_id = showSearchResults(RMI.searchComposer(name));
                }catch(RemoteException exc){
                    exc.printStackTrace();
                    retryRMIConnection();
                }
            }
        }
        while(true) {
            try {
                verifier = RMI.insertMusic(username, password, title, duration, lyrics, interpreter_id, composer_id, album_id);
                break;
            } catch (RemoteException exc) {
                retryRMIConnection();
            }
        }
    }

    private void insertMusic() {
        String title, lyrics;
        float duration;
        boolean verifier = false;
        int interpreter_id = -1, composer_id = -1, album_id = -1;

        System.out.println("\n ————————————————");
        System.out.println("INSERT NEW MUSIC");
        System.out.println(" ————————————————\n");

        System.out.println("Title: ");
        title = sc.nextLine();
        System.out.println("Duration: ");
        while(true){
            try{

                duration = Float.parseFloat(sc.nextLine());
                break;
            }catch(NumberFormatException e){
                System.out.println("Please insert a valid option!");
            }
        }
        System.out.println("Lyrics: ");
        lyrics = sc.nextLine();
        System.out.println("Composer: ");
        while(composer_id < 0){
            System.out.println("1 - Create a new composer\n2 - Find an existing composer:");
            int reply;
            do{
                reply = Integer.parseInt(sc.nextLine().replaceAll("\n", ""));
                if(reply == 0)return;
            }while(reply != 2 && reply != 1);
            if(reply == 1){
                composer_id = createComposer();
            }else{
                System.out.println("Composer name: ");
                String name = sc.nextLine().replaceAll("\n", "");
                try{
                    composer_id = showSearchResults(RMI.searchComposer(name));
                }catch(RemoteException exc){
                    exc.printStackTrace();
                    retryRMIConnection();
                }
            }
        }
        System.out.println("Interpreter:");
        while(interpreter_id < 0){
            System.out.println("1 - Create a new interpreter\n2 - Find an existing interpreter");
            int reply;
            do{
                reply = Integer.parseInt(sc.nextLine());
                if(reply == 0)return;
            }while(reply != 2 && reply != 1);
            if(reply == 1){
                interpreter_id = createInterpreter();
            }else{
                System.out.println("Interpreter name: ");
                String name = sc.nextLine();
                try{
                    interpreter_id = showSearchResults(RMI.searchInterpreter(name));
                    System.out.println(interpreter_id);
                }catch(RemoteException exc){
                    retryRMIConnection();
                }
            }
        }
        System.out.println("Album:");
        while(album_id < 0){
            System.out.println("1 - Create a new album\n2 - Find an existing album");
            int reply;
            do{
                reply = Integer.parseInt(sc.nextLine());
                if(reply == 0)return;
            }while(reply != 2 && reply != 1);
            if(reply == 1){
               //album_id = createAlbum();
            }else{
                System.out.println("Album name:");
                String name = sc.nextLine();
                try{
                    album_id = showSearchResults(RMI.searchAlbum(name, interpreter_id));
                }catch(RemoteException exc){
                    retryRMIConnection();
                }
            }
        }

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
