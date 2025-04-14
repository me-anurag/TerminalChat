import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter server IP: ");
            String ip = sc.nextLine();

            try (
                Socket socket = new Socket(ip, 5000);
                DataOutputStream writer = new DataOutputStream(socket.getOutputStream())
            ) {
                System.out.println("Connected to server!");

                FileTransferUtils.ensureReceiveFolder();

                // Start thread to listen for incoming messages or files
                new Thread(() -> FileTransferUtils.receive(socket)).start();

                // Main interaction loop
                while (true) {
                    FileTransferUtils.showMenu();
                    String option = sc.nextLine();

                    if (option.equals("1")) {
                        System.out.print("Enter message: ");
                        String msg = sc.nextLine();
                        writer.writeUTF("MSG:" + msg);
                        writer.flush();
                    } else if (option.equals("2")) {
                        System.out.print("Enter file(s) to send (space separated): ");
                        String[] files = sc.nextLine().split(" ");
                        FileTransferUtils.sendFiles(socket, files);
                    } else if (option.equals("3")) {
                        System.out.println("Exiting...");
                        writer.writeUTF("BYE");
                        writer.flush();
                        break;
                    } else {
                        System.out.println("Invalid option. Try again.");
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}