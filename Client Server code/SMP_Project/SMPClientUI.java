import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SMPClientUI extends JFrame {
    private String username;
    private final MyStreamSocket mySocket;
    private JTextArea outputArea;

    public SMPClientUI(String username, MyStreamSocket mySocket) {
        this.username = username;
        this.mySocket = mySocket;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SMP Client - " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Change to DISPOSE_ON_CLOSE
        setLocationRelativeTo(null);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel("Message:");
        JTextField messageField = new JTextField();
        messageField.setToolTipText("Enter message");

        JLabel idLabel = new JLabel("Message ID:");
        JTextField idField = new JTextField();
        idField.setToolTipText("Enter message ID (leave blank for all)");

        inputPanel.add(messageLabel);
        inputPanel.add(messageField);
        inputPanel.add(idLabel);
        inputPanel.add(idField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        JButton clearButton = new JButton("Clear");
        JButton logoffButton = new JButton("Logoff");
        JButton downloadAllButton = new JButton("Download All Messages");

        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(logoffButton);
        buttonPanel.add(downloadAllButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputPanel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        add(buttonPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        uploadButton.addActionListener(e -> upload(messageField.getText(), idField.getText()));
        downloadButton.addActionListener(e -> download(idField.getText()));
        downloadAllButton.addActionListener(e -> downloadAll());
        clearButton.addActionListener(e -> clear());
        logoffButton.addActionListener(e -> logoff());
    }

    private void upload(String message, String id) {
        if (username == null) {
            outputArea.append(ErrorCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        if (message.isEmpty()) {
            outputArea.append(ErrorCodes.EMPTY_MESSAGE + " Message content cannot be empty.\n");
            return;
        }
        try {
            int messageId = id.isEmpty() ? -1 : Integer.parseInt(id);
            mySocket.sendMessage(RequestCodes.UPLOAD + " " + username + " " + messageId + " " + message);
            String response = mySocket.receiveMessage();
            outputArea.append(response + "\n");
        } catch (NumberFormatException e) {
            outputArea.append(ErrorCodes.INVALID_MESSAGE_ID + " Invalid message ID.\n");
        } catch (IOException ex) {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void download(String id) {
        if (username == null) {
            outputArea.append(ErrorCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        try {
            mySocket.sendMessage(RequestCodes.DOWNLOAD + " " + id);
            String response = mySocket.receiveMessage();
            outputArea.append("Messages from server:\n" + response + "\n");
        } catch (IOException ex) {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void downloadAll() {
        if (username == null) {
            outputArea.append(ErrorCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        try {
            mySocket.sendMessage(RequestCodes.DOWNLOAD_ALL + "");
            String response = mySocket.receiveMessage();

            String[] messages = response.split("\\|");

            outputArea.setText("");

            for (String message : messages) {
                outputArea.append(message + "\n");
            }
        } catch (IOException ex) {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void clear() {
        if (username == null) {
            outputArea.append(ErrorCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all messages?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mySocket.sendMessage(RequestCodes.CLEAR + "");
                String result = mySocket.receiveMessage();
                outputArea.append(result + "\n");
            } catch (IOException ex) {
                outputArea.append("Error: " + ex.getMessage() + "\n");
            }
        }
    }

    private void logoff() {
        if (username == null) {
            outputArea.append(ErrorCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        try {
            mySocket.sendMessage(RequestCodes.LOGOFF + " " + username);
            String response = mySocket.receiveMessage();
            outputArea.append(response + "\n");
            mySocket.close();
            username = null;

            dispose();

            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            });
        } catch (IOException ex) {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }
}