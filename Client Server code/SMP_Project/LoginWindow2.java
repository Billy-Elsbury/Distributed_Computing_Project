import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class LoginWindow2 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Create a grid layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        // Username label and field
        Label userLabel = new Label("Username:");
        GridPane.setConstraints(userLabel, 0, 0);
        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        GridPane.setConstraints(userField, 1, 0);

        // Password label and field
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 1);
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        GridPane.setConstraints(passField, 1, 1);

        // Login button
        Button loginButton = new Button("Login");
        GridPane.setConstraints(loginButton, 1, 2);
        loginButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();

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

                    // Send the LOGIN command
                    mySocket.sendMessage("LOGIN " + username + " " + password);
                    String response = mySocket.receiveMessage();

                    if (response.startsWith("101")) {
                        // Login successful, open the main application window
                        SMPClientUI clientUI = new SMPClientUI(username, mySocket);
                        clientUI.show();
                        primaryStage.close();
                    } else {
                        // Login failed, show an error message
                        Alert alert = new Alert(Alert.AlertType.ERROR, response, ButtonType.OK);
                        alert.showAndWait();
                    }
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error connecting to the server: " + ex.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a username and password.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Add components to the grid
        grid.getChildren().addAll(userLabel, userField, passLabel, passField, loginButton);

        // Set the scene
        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}