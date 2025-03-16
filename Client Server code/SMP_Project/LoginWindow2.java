import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow2 extends JFrame {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }

    public LoginWindow2() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen

        // Create a panel with a GridLayout
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Username label and field
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        userField.setToolTipText("Enter username");

        // Password label and field
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        passField.setToolTipText("Enter password");

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                // Validate login
                if (!username.isEmpty() && !password.isEmpty()) {
                    try {
                        // Set the truststore properties
                        System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
                        System.setProperty("javax.net.ssl.trustStorePassword", "password");

                        // Create an SSL socket
                        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 12345);

                        // Wrap the SSL socket in MyStreamSocket
                        MyStreamSocket mySocket = new MyStreamSocket(sslSocket);

                        // Send the LOGIN request using RequestCodes
                        mySocket.sendMessage(RequestCodes.LOGIN + " " + username + " " + password);
                        String response = mySocket.receiveMessage();

                        if (response.startsWith(String.valueOf(ErrorCodes.SUCCESS))) {
                            // Login successful, open the main application window
                            SMPClientUI clientUI = new SMPClientUI(username, mySocket);
                            clientUI.show();
                            dispose(); // Close the login window
                        } else {
                            // Login failed, show an error message
                            JOptionPane.showMessageDialog(LoginWindow2.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(LoginWindow2.this, "Error connecting to the server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginWindow2.this, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to the panel
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(loginButton);

        // Add the panel to the frame
        add(panel);
    }
}