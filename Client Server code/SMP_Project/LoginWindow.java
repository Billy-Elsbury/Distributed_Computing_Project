import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginWindow extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }

    public LoginWindow() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        JTextField userField = new JTextField(15);
        userField.setBackground(new Color(50, 50, 50));
        userField.setForeground(Color.WHITE);
        userField.setCaretColor(Color.WHITE);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        JPasswordField passField = new JPasswordField(15);
        passField.setBackground(new Color(50, 50, 50));
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);

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

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = getJPanel(userField, passField);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel getJPanel(JTextField userField, JPasswordField passField) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin(userField.getText(), new String(passField.getPassword())));

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(Color.WHITE);
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> handleRegister(userField.getText(), new String(passField.getPassword())));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        return buttonPanel;
    }

    private void handleLogin(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "password");

                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 12345);

                MyStreamSocket mySocket = new MyStreamSocket(sslSocket);

                mySocket.sendMessage(RequestCodes.LOGIN + " " + username + " " + password);
                String response = mySocket.receiveMessage();

                if (response.startsWith(String.valueOf(ErrorCodes.SUCCESS))) {
                    ClientHelper clientHelper = new ClientHelper("localhost", 12345);
                    SMPClientUI clientUI = new SMPClientUI(username, clientHelper);
                    clientUI.setVisible(true);
                    dispose();
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

    private void handleRegister(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "password");

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

        MyStreamSocket mySocket = new MyStreamSocket(sslSocket);

        mySocket.sendMessage(RequestCodes.REGISTER + " " + username + " " + password);
        String response = mySocket.receiveMessage();
        return response;
    }
}