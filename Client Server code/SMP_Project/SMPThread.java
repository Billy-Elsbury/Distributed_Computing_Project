import java.io.IOException;
import java.util.List;

public class SMPThread implements Runnable {
    private MyStreamSocket myDataSocket;
    private MessageStorage messageStorage = MessageStorage.getInstance(); // Use the singleton instance
    private UserManager userManager = new UserManager(); // Add UserManager
    private String username = null; // Track the username for this session

    public SMPThread(MyStreamSocket myDataSocket) {
        this.myDataSocket = myDataSocket;
    }

    public void run() {
        boolean done = false;
        String message = "";

        try {
            while (!done) {
                // Receive a message from the client
                message = myDataSocket.receiveMessage();
                if (message == null) { // Client disconnected
                    System.out.println("Client disconnected.");
                    break;
                }
                System.out.println("Message received: " + message);

                // Split the message into parts
                String[] parts = message.split(" ", 4); // Split into at most 4 parts
                if (parts.length == 0) {
                    myDataSocket.sendMessage(ErrorCodes.INVALID_COMMAND + " Invalid command format.");
                    continue;
                }

                int requestCode = Integer.parseInt(parts[0]);

                switch (requestCode) {

                    case RequestCodes.REGISTER:
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            if (userManager.addUser(username, password)) {
                                myDataSocket.sendMessage(ErrorCodes.SUCCESS + " Registration successful.");
                            } else {
                                myDataSocket.sendMessage(ErrorCodes.INVALID_LOGIN_FORMAT + " Username already exists.");
                            }
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_LOGIN_FORMAT + " Invalid registration format. Usage: " + RequestCodes.REGISTER + " <username> <password>");
                        }
                        break;

                    case RequestCodes.LOGIN:
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            if (userManager.verifyUser(username, password)) { // Verify user credentials
                                this.username = username;
                                myDataSocket.sendMessage(ErrorCodes.SUCCESS + " Login successful.");
                            } else {
                                myDataSocket.sendMessage(ErrorCodes.NOT_LOGGED_IN + "Login Failed in Thread, Invalid username or password.");
                            }
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_LOGIN_FORMAT + "Login Failed in Thread, Invalid login format. Usage: " + RequestCodes.LOGIN + " <username> <password>");
                        }
                        break;

                    case RequestCodes.UPLOAD:
                        if (parts.length == 4) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage(ErrorCodes.NOT_LOGGED_IN + "Upload Failed in Thread, Not logged in.");
                                break;
                            }
                            try {
                                int id = Integer.parseInt(parts[2]);
                                String messageContent = parts[3];
                                if (messageContent.isEmpty()) {
                                    myDataSocket.sendMessage(ErrorCodes.EMPTY_MESSAGE + " Message content cannot be empty.");
                                } else if (messageStorage.addMessage(username, id, messageContent)) {
                                    myDataSocket.sendMessage(ErrorCodes.SUCCESS + " Message uploaded.");
                                } else {
                                    myDataSocket.sendMessage(ErrorCodes.MESSAGE_ID_EXISTS + " Message ID already exists.");
                                }
                            } catch (NumberFormatException e) {
                                myDataSocket.sendMessage(ErrorCodes.INVALID_MESSAGE_ID + " Invalid message ID.");
                            }
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_UPLOAD_FORMAT + " Invalid upload format. Usage: " + RequestCodes.UPLOAD + " <username> <ID> <message>");
                        }
                        break;

                    case RequestCodes.DOWNLOAD_ALL:
                        List<String> allMessages = messageStorage.getAllMessages();
                        String response = String.join("|", allMessages);  // Join messages with a delimiter
                        myDataSocket.sendMessage(response);
                        break;

                    case RequestCodes.DOWNLOAD:
                        if (parts.length == 2) {
                            String input = parts[1];
                            if (input.isEmpty()) {
                                myDataSocket.sendMessage(ErrorCodes.NO_MESSAGE_ID_PROVIDED + " No message ID provided. Usage: " + RequestCodes.DOWNLOAD + " <ID>");
                            } else {
                                try {
                                    int messageId = Integer.parseInt(input);
                                    String specificMessage = messageStorage.getMessageById(this.username, messageId);
                                    myDataSocket.sendMessage(specificMessage);
                                } catch (NumberFormatException e) {
                                    myDataSocket.sendMessage(ErrorCodes.INVALID_MESSAGE_ID + " Invalid message ID.");
                                }
                            }
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_DOWNLOAD_FORMAT + " Invalid download format. Usage: " + RequestCodes.DOWNLOAD + " <ID>");
                        }
                        break;

                    case RequestCodes.CLEAR:
                        if (parts.length == 1) {
                            try {
                                messageStorage.clearMessages();
                                myDataSocket.sendMessage(ErrorCodes.SUCCESS + " All messages cleared.");
                            } catch (Exception ex) {
                                myDataSocket.sendMessage(ErrorCodes.ERROR_CLEARING_MESSAGES + " Error clearing messages.");
                            }
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_CLEAR_FORMAT + " Invalid clear format. Usage: " + RequestCodes.CLEAR);
                        }
                        break;

                    case RequestCodes.LOGOFF:
                        if (parts.length == 2) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username)) {
                                myDataSocket.sendMessage(ErrorCodes.NOT_LOGGED_IN + " Not logged in.");
                                break;
                            }
                            this.username = null;
                            myDataSocket.sendMessage(ErrorCodes.SUCCESS + " Logoff successful.");
                            myDataSocket.close();
                            done = true;
                        } else {
                            myDataSocket.sendMessage(ErrorCodes.INVALID_LOGOFF_FORMAT + " Invalid logoff format. Usage: " + RequestCodes.LOGOFF + " <username>");
                        }
                        break;

                    default:
                        myDataSocket.sendMessage(ErrorCodes.UNKNOWN_COMMAND + " Unknown command.");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in thread: " + ex);
        }
        finally
        {
            try {
                myDataSocket.close(); // Close the socket on exit
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}