import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;

public class SMPClient {
    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);

        String username = "";

        try {
            System.out.println("Welcome to the SMP Client.");
            System.out.println("Enter the server hostname (default: localhost):");
            String hostName = br.readLine();
            if (hostName.isEmpty()) {
                hostName = "localhost";
            }

            System.out.println("Enter the server port (default: 12345):");
            String portNum = br.readLine();
            if (portNum.isEmpty()) {
                portNum = "12345";
            }

            // Set the truststore properties
            System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");

            // Create an SSL socket
            SSLSocketFactory sslSocketFactory =
                    (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket =
                    (SSLSocket) sslSocketFactory.createSocket(hostName, Integer.parseInt(portNum));

            // Wrap the SSL socket in MyStreamSocket
            MyStreamSocket mySocket = new MyStreamSocket(sslSocket);
            System.out.println("Connected to server (SSL).");

            boolean done = false;
            while (!done) {
                System.out.println("Enter a command (LOGIN, UPLOAD, DOWNLOAD, CLEAR):");
                String command = br.readLine().toUpperCase();

                switch (command) {

                    case "LOGIN":
                        System.out.println("Enter username:");
                        username = br.readLine();
                        System.out.println("Enter password:");
                        String password = br.readLine();
                        mySocket.sendMessage(RequestCodes.LOGIN + " " + username + " " + password);
                        System.out.println(mySocket.receiveMessage());
                        break;

                    case "UPLOAD":
                        System.out.println("Enter message:");
                        String message = br.readLine();
                        if (message.isEmpty()) {
                            System.out.println(ErrorCodes.EMPTY_MESSAGE + " Message content cannot be empty.");
                            break;
                        }
                        System.out.println("Enter message ID (leave blank to auto-generate):");
                        String id = br.readLine();
                        int messageId = id.isEmpty() ? -1 : Integer.parseInt(id);
                        mySocket.sendMessage(RequestCodes.UPLOAD + " " + username + " " + messageId + " " + message);
                        System.out.println(mySocket.receiveMessage());
                        break;

                    case "DOWNLOAD":
                        System.out.println("Enter 'all' to download all messages or a specific message ID:");
                        String downloadInput = br.readLine();
                        if (downloadInput.equalsIgnoreCase("all")) {
                            mySocket.sendMessage(RequestCodes.DOWNLOAD_ALL + "");
                            System.out.println("Messages from server:\n" + mySocket.receiveMessage());
                        } else {
                            mySocket.sendMessage(RequestCodes.DOWNLOAD + " " + downloadInput);
                            System.out.println("Message from server:\n" + mySocket.receiveMessage());
                        }
                        break;

                    case "CLEAR":
                        System.out.println("Are you sure you want to clear all messages? (yes/no):");
                        String confirmation = br.readLine().toLowerCase();
                        if (confirmation.equals("yes")) {
                            mySocket.sendMessage(RequestCodes.CLEAR + "");
                            System.out.println(mySocket.receiveMessage());
                        } else {
                            System.out.println("Clear operation cancelled.");
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}