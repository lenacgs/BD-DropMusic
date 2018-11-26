import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.IOException;
import java.rmi.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
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
            System.out.println();
            System.out.println(" ———————————————");
            System.out.println("| 1) Register  |");
            System.out.println("| 2) Login     |");
            System.out.println("| 3) Exit      |");
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

            try {
                option = Integer.parseInt(sc.nextLine().replaceAll("^[,\\s]+", ""));
            } catch (NumberFormatException exc) {
                System.out.println("-!- Invalid input");
            }

            if (option == 0) {
                return;
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
                return false;
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
