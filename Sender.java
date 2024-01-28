import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;

public class Sender extends JFrame {
    private JTextField ipField;
    private JTextField portField;
    private JButton chooseFileButton;
    private JButton sendFileButton;
    private JButton sendTextButton;
    private File selectedFile;
    private static final Pattern IP_ADDRESS_PATTERN = 
        Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

    public Sender() {
        createWindow();
    }

    private void createWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new GridLayout(5, 2));

        chooseFileButton = new JButton("Choose File");
        ipField = new JTextField();
        portField = new JTextField("5000");
        sendFileButton = new JButton("Send File");
        sendTextButton = new JButton("Send Text from file.txt");

        add(chooseFileButton);
        add(new JLabel(""));  // Placeholder for layout
        add(new JLabel("Receiver IP:"));
        add(ipField);
        add(new JLabel("Port:"));
        add(portField);
        add(sendFileButton);
        add(sendTextButton);

        chooseFileButton.addActionListener(e -> chooseFile());
        sendFileButton.addActionListener(e -> sendFile());
        sendTextButton.addActionListener(e -> sendText());

        setVisible(true);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            chooseFileButton.setText("Selected: " + selectedFile.getName());
        }
    }

    private void sendFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose a file first.");
            return;
        }

        new Thread(() -> {
            try {
                if (!validateInput()) return;

                try (Socket socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     FileInputStream fis = new FileInputStream(selectedFile)) {

                    byte[] buffer = new byte[400096]; // Buffer size of 4000KB
                    long totalBytesRead = 0;
                    dos.writeUTF("FILE:" + selectedFile.getName());

                    int read;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                        totalBytesRead += read;
                        if (totalBytesRead > 64 * 1024 * 1024) { // 64 MB limit
                            JOptionPane.showMessageDialog(this, "File is larger than 64 MB. Transfer stopped.");
                            break;
                        }
                    }

                    if (totalBytesRead <= 64 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(this, "File sent successfully!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error sending file: " + e.getMessage());
            }
        }).start();
    }

  private void sendText() {
    File textFile = new File("file.txt");
    if (!textFile.exists()) {
        JOptionPane.showMessageDialog(this, "file.txt not found.");
        return;
    }

    new Thread(() -> {
        try {
            if (!validateInput()) return;

            try (Socket socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 BufferedReader br = new BufferedReader(new FileReader(textFile))) {

                String line;
                while ((line = br.readLine()) != null) {
                    dos.writeUTF("TEXT:" + line);
                }

                JOptionPane.showMessageDialog(this, "Text from file.txt sent successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending text: " + e.getMessage());
        }
    }).start();
}


    private boolean validateInput() {
        if (!IP_ADDRESS_PATTERN.matcher(ipField.getText()).matches()) {
            JOptionPane.showMessageDialog(this, "Invalid IP address format.");
            return false;
        }
        try {
            int port = Integer.parseInt(portField.getText());
            if (port < 0 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Port number out of range.");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        new Sender();
    }
}
