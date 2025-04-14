import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class FileTransferUtils {

    // Display menu options to the user
    public static void showMenu() {
        System.out.println("\n===== Java Chat & File Transfer =====");
        System.out.println("[1] Send Message");
        System.out.println("[2] Send File(s)");
        System.out.println("[3] Exit");
        System.out.print("Choose an option: ");
    }

    // Ensure the folder exists where received files will be saved
    public static void ensureReceiveFolder() {
        File dir = new File("received_files");
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                System.out.println("❌ Failed to create 'received_files' folder.");
            }
        }
    }

    // Send one or more files to the server
    public static void sendFiles(Socket socket, String[] fileNames) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    System.out.println("❌ File not found or not a file: " + fileName);
                    continue;
                }

                byte[] fileBytes = Files.readAllBytes(file.toPath());

                dos.writeUTF("FILE:" + file.getName());
                dos.writeLong(fileBytes.length);
                dos.write(fileBytes);
                dos.flush();

                System.out.println("✅ Sent file: " + file.getName());
            }

        } catch (IOException e) {
            System.out.println("❗ Error sending file(s): " + e.getMessage());
        }
    }

    // Listen for incoming messages or files from the server
    public static void receive(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            while (!socket.isClosed()) {
                String header = dis.readUTF();

                if (header.startsWith("MSG:")) {
                    // Handle incoming text message
                    System.out.println("💬 " + header.substring(4));

                } else if (header.startsWith("FILE:")) {
                    // Handle incoming file
                    String fileName = header.substring(5);
                    long size = dis.readLong();
                    byte[] data = new byte[(int) size];
                    dis.readFully(data);

                    File file = new File("received_files/" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(data);
                    }

                    System.out.println("📥 [File received] -> " + file.getName());

                } else if (header.equals("BYE")) {
                    System.out.println("🔌 Connection closed by server.");
                    socket.close();
                    break;
                }
            }

        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.out.println("🚫 Connection lost: " + e.getMessage());
            }
        }
    }
}