import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

            ClientHelper clientHelper = new ClientHelper(hostName, Integer.parseInt(portNum));
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
                        System.out.println(clientHelper.login(username, password));
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
                        System.out.println(clientHelper.upload(username, messageId, message));
                        break;

                    case "DOWNLOAD":
                        System.out.println("Enter 'all' to download all messages or a specific message ID:");
                        String downloadInput = br.readLine();
                        System.out.println("Messages from server:\n" + clientHelper.download(downloadInput));
                        break;

                    case "CLEAR":
                        System.out.println("Are you sure you want to clear all messages? (yes/no):");
                        String confirmation = br.readLine().toLowerCase();
                        if (confirmation.equals("yes")) {
                            System.out.println(clientHelper.clear());
                        } else {
                            System.out.println("Clear operation cancelled.");
                        }
                        break;

                    default:
                        System.out.println(ErrorCodes.UNKNOWN_COMMAND + " Unknown command.");
                        break;
                }
            }

            clientHelper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}