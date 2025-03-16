import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MessageStorage {
    private Map<String, List<Message>> userMessages;  // Store messages with usernames
    private String storageFile = "messages.txt";  // File to store messages

    public MessageStorage() {
        this.userMessages = new HashMap<>();
        loadMessagesFromFile();  // Load messages from file on startup
    }

    // Inner class to represent a message with an ID
    private static class Message {
        int id;
        String content;

        Message(int id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return id + ":" + content;
        }
    }

    // Load messages from file
    private void loadMessagesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (parts.length == 3) {
                    String username = parts[0];
                    int id = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    userMessages.putIfAbsent(username, new ArrayList<>());
                    userMessages.get(username).add(new Message(id, content));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing message file found. Starting with an empty message store.");
        }
    }

    // Save messages to file
    private void saveMessagesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile))) {
            for (Map.Entry<String, List<Message>> entry : userMessages.entrySet()) {
                for (Message msg : entry.getValue()) {
                    writer.write(entry.getKey() + ":" + msg.toString() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to save messages to file: " + e.getMessage());
        }
    }

    // Add a message with a custom ID
    public boolean addMessage(String username, int id, String message) {
        userMessages.putIfAbsent(username, new ArrayList<>());
        for (Message msg : userMessages.get(username)) {
            if (msg.id == id) {
                return false;  // ID already exists
            }
        }
        userMessages.get(username).add(new Message(id, message));
        saveMessagesToFile();  // Save messages to file
        return true;
    }

    public String getMessages(String username) {
        if (userMessages.containsKey(username)) {
            StringBuilder allMessages = new StringBuilder();
            for (Message msg : userMessages.get(username)) {
                allMessages.append("ID: ").append(msg.id).append(" - ").append(msg.content).append("\n");
            }
            return allMessages.toString();
        }
        return "No messages found for user: " + username;
    }

    // Get a specific message by ID for a user
    public String getMessageById(String username, int messageId) {
        if (userMessages.containsKey(username)) {
            for (Message msg : userMessages.get(username)) {
                if (msg.id == messageId) {
                    return "ID: " + msg.id + " - " + msg.content;
                }
            }
        }
        return "Message not found for user: " + username;
    }

    // Get all messages in the file regardless of user or ID
    public List<String> getAllMessages() {
        List<String> allMessages = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : userMessages.entrySet()) {
            for (Message msg : entry.getValue()) {
                allMessages.add("ID: " + msg.id + " - " + msg.content);  // Add each message as a separate item
            }
        }
        return allMessages;
    }

    // Clear all messages for a specific user
    public void clearMessages(String username) {
        if (userMessages.containsKey(username)) {
            userMessages.get(username).clear();
            saveMessagesToFile();  // Save changes to file
        }
    }
}