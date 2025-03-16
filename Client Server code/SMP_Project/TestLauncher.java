import javax.swing.*;

public class TestLauncher {
    public static void main(String[] args) {
        // Server in separate thread
        new Thread(() -> SMPServer.main(new String[]{})).start();

        try {
            Thread.sleep(1000); // Wait x seconds for the server to initialise
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // First login window
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow1 = new LoginWindow();
            loginWindow1.setVisible(true);
        });

        // Second login window
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow2 = new LoginWindow();
            loginWindow2.setVisible(true);
        });
    }
}