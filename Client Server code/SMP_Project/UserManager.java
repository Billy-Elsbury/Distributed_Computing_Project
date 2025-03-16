import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final String USER_FILE = "users.txt"; // File to store user credentials
    private Map<String, String> users; // Map to store username-password pairs

    public UserManager() {
        this.users = new HashMap<>();
        loadUsersFromFile(); // Load users from file on startup
    }

    // Load users from the file
    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":"); // Split username and password
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]); // Add to the map
                }
            }
        } catch (IOException e) {
            System.out.println("No existing user file found. Starting with an empty user store.");
        }
    }

    // Save users to the file
    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Failed to save users to file: " + e.getMessage());
        }
    }

    // Verify a username and password
    public boolean verifyUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    // Add a new user (for future use, e.g., registration)
    public synchronized boolean addUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, password);
        saveUsersToFile(); // Save changes to file
        return true;
    }
}