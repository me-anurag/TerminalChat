import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started. Waiting for client...");

        Socket socket = serverSocket.accept();
        System.out.println("Client connected!");

        FileTransferUtils.ensureReceiveFolder();

        DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> FileTransferUtils.receive(socket)).start();

        Scanner sc = new Scanner(System.in);

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
                socket.close();
                break;
            }
        }

        sc.close();
        serverSocket.close();
    }
}
