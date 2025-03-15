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
    private MyStreamSocket mySocket;
    private TextArea outputArea;
    private Stage primaryStage;

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
        this.primaryStage = primaryStage;
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
        idField.setPromptText("Enter message ID");
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
            // Send the UPLOAD command with username, ID, and message
            mySocket.sendMessage("UPLOAD " + username + " " + id + " " + message);
            String response = mySocket.receiveMessage();
            outputArea.appendText(response + "\n");
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
            if (id.isEmpty()) {
                mySocket.sendMessage("DOWNLOAD " + username);
            } else {
                mySocket.sendMessage("DOWNLOAD " + username + " " + id);
            }
            String response = mySocket.receiveMessage();
            outputArea.appendText("Messages from server:\n" + response + "\n");
        } catch (IOException ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }

    private void clear() {
        if (username == null) {
            outputArea.appendText("102 Not logged in.\n");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear all messages?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    mySocket.sendMessage("CLEAR " + username);
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