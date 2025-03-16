import javax.swing.*;

public class TestLauncher {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        new Thread(() -> SMPServer.main(new String[]{})).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launchLoginWindow();
        launchLoginWindow();
    }

    private static void launchLoginWindow() {
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            loginWindow.setVisible(true);
        });
    }
}