import java.io.*;
import java.util.List;

public class SMPThread implements Runnable {
    private MyStreamSocket myDataSocket;
    private MessageStorage messageStorage;  // Use the updated MessageStorage
    private String username = null;  // Track the username for this session

    public SMPThread(MyStreamSocket myDataSocket) {
        this.myDataSocket = myDataSocket;
        this.messageStorage = new MessageStorage();  // Initialize message storage
    }

    public void run() {
        boolean done = false;
        String message;

        try {
            while (!done) {
                // Receive a message from the client
                message = myDataSocket.receiveMessage();
                System.out.println("Message received: " + message);

                // Split the message into parts
                String[] parts = message.split(" ", 4);  // Split into at most 4 parts
                if (parts.length == 0) {
                    myDataSocket.sendMessage("102 Invalid command format.");
                    continue;
                }

                String command = parts[0].toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            // Accept any login without checking if the user is already logged in
                            this.username = username;  // Associate this session with the username
                            myDataSocket.sendMessage("101 Login successful.");
                        } else {
                            myDataSocket.sendMessage("102 Invalid login format. Usage: LOGIN <username> <password>");
                        }
                        break;

                    case "UPLOAD":
                        if (parts.length == 4) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            try {
                                int id = Integer.parseInt(parts[2]);
                                String messageContent = parts[3];  // The message content
                                if (messageStorage.addMessage(username, id, messageContent)) {
                                    myDataSocket.sendMessage("101 Message uploaded.");
                                } else {
                                    myDataSocket.sendMessage("102 Message ID already exists.");
                                }
                            } catch (NumberFormatException e) {
                                myDataSocket.sendMessage("102 Invalid message ID.");
                            }
                        } else {
                            myDataSocket.sendMessage("102 Invalid upload format. Usage: UPLOAD <username> <ID> <message>");
                        }
                        break;

                    case "DOWNLOAD_ALL":
                        List<String> allMessages = messageStorage.getAllMessages();
                        String response = String.join("|", allMessages);  // Join messages with a delimiter
                        myDataSocket.sendMessage(response);
                        break;

                    case "DOWNLOAD":
                        if (parts.length == 2) {
                            String input = parts[1];  // Either "all" or a specific ID
                            if (input.equalsIgnoreCase("all")) {
                                // Download all messages for the logged-in user
                                String userMessages = messageStorage.getMessages(this.username);
                                myDataSocket.sendMessage(userMessages);  // Send all messages for the user
                            } else {
                                // Download specific message by ID for the logged-in user
                                try {
                                    int messageId = Integer.parseInt(input);
                                    String specificMessage = messageStorage.getMessageById(this.username, messageId);
                                    myDataSocket.sendMessage(specificMessage);
                                } catch (NumberFormatException e) {
                                    myDataSocket.sendMessage("102 Invalid message ID.");
                                }
                            }
                        } else {
                            myDataSocket.sendMessage("102 Invalid download format. Usage: DOWNLOAD <ID> or DOWNLOAD all");
                        }
                        break;

                    case "CLEAR":
                        if (parts.length == 2) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            messageStorage.clearMessages(username);
                            myDataSocket.sendMessage("101 All messages cleared.");
                        } else {
                            myDataSocket.sendMessage("102 Invalid clear format. Usage: CLEAR <username>");
                        }
                        break;

                    case "LOGOFF":
                        if (parts.length == 2) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            this.username = null;  // Clear the session's username
                            myDataSocket.sendMessage("101 Logoff successful.");
                            myDataSocket.close();
                            done = true;
                        } else {
                            myDataSocket.sendMessage("102 Invalid logoff format. Usage: LOGOFF <username>");
                        }
                        break;

                    default:
                        myDataSocket.sendMessage("102 Unknown command.");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in thread: " + ex);
        }
    }
}