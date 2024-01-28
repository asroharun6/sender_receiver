import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    private ServerSocket serverSocket;

    public Receiver(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started. Listening on Port " + port);
    }

    public void start() {
        while (true) {
            try (Socket socket = serverSocket.accept();
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                System.out.println("Client connected.");

                String initialMessage = dis.readUTF();
                if (initialMessage.startsWith("FILE:")) {
                    saveFile(dis, initialMessage.substring(5));
                } else if (initialMessage.startsWith("TEXT:")) {
                    System.out.println(initialMessage.substring(5));
                }
                
                System.out.println("Client disconnected.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFile(DataInputStream dis, String fileName) throws IOException {
        File file = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];

            int read;
            while ((read = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }
        }
        System.out.println("File " + fileName + " received.");
    }

    public static void main(String[] args) {
        try {
            int port = 5000; // Port number
            Receiver server = new Receiver(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
