import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginWindow extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow2 loginWindow2 = new LoginWindow2();
            loginWindow2.setVisible(true);
        });
    }

    public LoginWindow() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create a panel for the form with a GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username label and field
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        userField.setToolTipText("Enter username");

        // Password label and field
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        passField.setToolTipText("Enter password");

        // Add components to the form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(passField, gbc);

        // Add the form panel to the main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Create a panel for the buttons
        JPanel buttonPanel = getJPanel(userField, passField);

        // Add the button panel to the main panel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the main panel to the frame
        add(mainPanel);

        // Set a modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel getJPanel(JTextField userField, JPasswordField passField) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 123, 255)); // Blue color
        loginButton.setForeground(Color.WHITE); // White text
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.addActionListener(_ -> handleLogin(userField.getText(), new String(passField.getPassword())));

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(40, 167, 69)); // Green color
        registerButton.setForeground(Color.WHITE); // White text
        registerButton.setFocusPainted(false); // Remove focus border
        registerButton.addActionListener(_ -> handleRegister(userField.getText(), new String(passField.getPassword())));

        // Add buttons to the button panel
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        return buttonPanel;
    }

    private void handleLogin(String username, String password) {
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
                    clientUI.setVisible(true);
                    dispose(); // Close the login window
                } else {
                    // Login failed, show an error message
                    JOptionPane.showMessageDialog(LoginWindow.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(LoginWindow.this, "Error connecting to the server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(LoginWindow.this, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                // Set the truststore properties
                System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "password");

                // Create an SSL socket
                String response = getString(username, password);

                if (response.startsWith(String.valueOf(ErrorCodes.SUCCESS))) {
                    JOptionPane.showMessageDialog(LoginWindow.this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(LoginWindow.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(LoginWindow.this, "Error connecting to the server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(LoginWindow.this, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getString(String username, String password) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 12345);

        // Wrap the SSL socket in MyStreamSocket
        MyStreamSocket mySocket = new MyStreamSocket(sslSocket);

        // Send the REGISTER command
        mySocket.sendMessage(RequestCodes.REGISTER + " " + username + " " + password);
        String response = mySocket.receiveMessage();
        return response;
    }
}