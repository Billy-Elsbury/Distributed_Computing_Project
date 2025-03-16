import java.io.*;
import java.util.*;

public class MessageStorage {
    // Singleton instance
    private static MessageStorage instance;
    private Map<String, List<Message>> userMessages;  // Store messages with usernames
    private String storageFile = "messages.txt";  // File to store messages

    // Private constructor to prevent instantiation
    private MessageStorage() {
        this.userMessages = new HashMap<>();
        loadMessagesFromFile();  // Load messages from file on startup
    }

    // Singleton accessor
    public static synchronized MessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
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

    // Add a message with an optional ID (if ID is -1, generate the next available ID)
    public synchronized boolean addMessage(String username, int id, String message) {
        userMessages.putIfAbsent(username, new ArrayList<>());

        // If ID is not provided (e.g., -1), generate the next available ID
        if (id == -1) {
            id = getNextAvailableId(username);
        }

        // Check if the ID already exists
        for (Message msg : userMessages.get(username)) {
            if (msg.id == id) {
                return false;  // ID already exists
            }
        }

        // Add the message with the generated or provided ID
        userMessages.get(username).add(new Message(id, message));
        saveMessagesToFile();  // Save messages to file
        return true;
    }

    // Helper method to generate the next available ID for a user
    private int getNextAvailableId(String username) {
        List<Message> messages = userMessages.get(username);
        if (messages.isEmpty()) {
            return 1;  // Start with ID 1 if no messages exist
        } else {
            // Find the maximum ID and increment it by 1
            int maxId = messages.stream()
                    .mapToInt(msg -> msg.id)
                    .max()
                    .orElse(0);
            return maxId + 1;
        }
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

    // Clear all messages (thread-safe)
    public synchronized void clearMessages() {
        userMessages.clear();  // Clear the entire map
        saveMessagesToFile();  // Save changes to file
    }
}