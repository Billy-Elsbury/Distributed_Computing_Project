import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class SMPClientUI extends Application {
    private String username;
    private final MyStreamSocket mySocket;
    private TextArea outputArea;

    public SMPClientUI(String username, MyStreamSocket mySocket) {
        this.username = username;
        this.mySocket = mySocket;
    }

    public void show() {
        Stage stage = new Stage();
        start(stage);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SMP Client - " + username);

        // Create a border layout
        BorderPane borderPane = new BorderPane();

        // Input panel
        GridPane inputPanel = new GridPane();
        inputPanel.setPadding(new Insets(10, 10, 10, 10));
        inputPanel.setVgap(10);
        inputPanel.setHgap(10);

        // Message field
        Label messageLabel = new Label("Message:");
        GridPane.setConstraints(messageLabel, 0, 0);
        TextField messageField = new TextField();
        messageField.setPromptText("Enter message");
        GridPane.setConstraints(messageField, 1, 0);

        // Message ID field
        Label idLabel = new Label("Message ID:");
        GridPane.setConstraints(idLabel, 0, 1);
        TextField idField = new TextField();
        idField.setPromptText("Enter message ID (leave blank for all)");
        GridPane.setConstraints(idField, 1, 1);

        // Add components to the input panel
        inputPanel.getChildren().addAll(messageLabel, messageField, idLabel, idField);

        // Button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));

        Button uploadButton = new Button("Upload");
        Button downloadButton = new Button("Download");
        Button clearButton = new Button("Clear");
        Button logoffButton = new Button("Logoff");

        buttonPanel.getChildren().addAll(uploadButton, downloadButton, clearButton, logoffButton);

        Button downloadAllButton = new Button("Download All Messages");
        buttonPanel.getChildren().add(downloadAllButton);

// Add action listener for the new button
        downloadAllButton.setOnAction(e -> downloadAll());
        
        // Output area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        // Add components to the border layout
        borderPane.setTop(inputPanel);
        borderPane.setCenter(buttonPanel);
        borderPane.setBottom(new ScrollPane(outputArea));

        // Set the scene
        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add action listeners
        uploadButton.setOnAction(e -> upload(messageField.getText(), idField.getText()));
        downloadButton.setOnAction(e -> download(idField.getText()));
        clearButton.setOnAction(e -> clear());
        logoffButton.setOnAction(e -> logoff(primaryStage));
    }

    private void upload(String message, String id) {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        try {
            int messageId = id.isEmpty() ? -1 : Integer.parseInt(id);  // Use -1 if ID is blank
            mySocket.sendMessage("UPLOAD " + username + " " + messageId + " " + message);
            String response = mySocket.receiveMessage();
            outputArea.appendText(response + "\n");
        } catch (NumberFormatException e) {
            outputArea.appendText("102 Invalid message ID.\n");
        } catch (IOException ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }

    private void download(String id) {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        try {
            //download the specific message by ID
            mySocket.sendMessage("DOWNLOAD " + id);

            String response = mySocket.receiveMessage();
            outputArea.appendText("Messages from server:\n" + response + "\n");  // Display all messages at once
        } catch (IOException ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }

    private void downloadAll() {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        try {
            mySocket.sendMessage("DOWNLOAD_ALL");
            String response = mySocket.receiveMessage();

            // Split the response using the delimiter
            String[] messages = response.split("\\|");

            // Clear the output area before displaying new messages
            outputArea.clear();

            // Append each message on a new line
            for (String message : messages) {
                outputArea.appendText(message + "\n");
            }
        } catch (IOException ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }

    private void clear() {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        // Show confirmation dialog on the client side
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear all messages?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    mySocket.sendMessage("CLEAR");
                    String result = mySocket.receiveMessage();
                    outputArea.appendText(result + "\n");
                } catch (IOException ex) {
                    outputArea.appendText("Error: " + ex.getMessage() + "\n");
                }
            }
        });
    }

    private void logoff(Stage primaryStage) {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        try {
            mySocket.sendMessage("LOGOFF " + username);
            String response = mySocket.receiveMessage();
            outputArea.appendText(response + "\n");
            mySocket.close();
            username = null;
            primaryStage.close();
        } catch (IOException ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }
}