import java.io.*;
import java.util.*;

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

                // Use StringTokenizer to parse the message
                StringTokenizer tokenizer = new StringTokenizer(message);
                if (!tokenizer.hasMoreTokens()) {
                    myDataSocket.sendMessage("102 Invalid command format.");
                    continue;
                }

                String command = tokenizer.nextToken().toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (tokenizer.countTokens() == 2) {
                            String username = tokenizer.nextToken();
                            String password = tokenizer.nextToken();
                            // Accept any login without checking if the user is already logged in
                            this.username = username;  // Associate this session with the username
                            myDataSocket.sendMessage("101 Login successful.");
                        } else {
                            myDataSocket.sendMessage("102 Invalid login format. Usage: LOGIN <username> <password>");
                        }
                        break;

                    case "UPLOAD":
                        if (tokenizer.countTokens() >= 3) {
                            String username = tokenizer.nextToken();
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            try {
                                int id = Integer.parseInt(tokenizer.nextToken());
                                // The rest of the tokens are the message
                                StringBuilder messageBuilder = new StringBuilder();
                                while (tokenizer.hasMoreTokens()) {
                                    messageBuilder.append(tokenizer.nextToken()).append(" ");
                                }
                                String messageContent = messageBuilder.toString().trim();  // Remove trailing space
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

                    case "DOWNLOAD":
                        if (tokenizer.countTokens() == 1) {
                            String username = tokenizer.nextToken();
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            // Download all messages for the user
                            myDataSocket.sendMessage(messageStorage.getMessages(username));
                        } else if (tokenizer.countTokens() == 2) {
                            String username = tokenizer.nextToken();
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage("102 Not logged in.");
                                break;
                            }
                            // Download specific message by ID for the user
                            try {
                                int messageId = Integer.parseInt(tokenizer.nextToken());
                                String specificMessage = messageStorage.getMessageById(username, messageId);
                                myDataSocket.sendMessage(specificMessage);
                            } catch (NumberFormatException e) {
                                myDataSocket.sendMessage("102 Invalid message ID.");
                            }
                        } else {
                            myDataSocket.sendMessage("102 Invalid download format. Usage: DOWNLOAD <username> or DOWNLOAD <username> <ID>");
                        }
                        break;

                    case "CLEAR":
                        if (tokenizer.countTokens() == 1) {
                            String username = tokenizer.nextToken();
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
                        if (tokenizer.countTokens() == 1) {
                            String username = tokenizer.nextToken();
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