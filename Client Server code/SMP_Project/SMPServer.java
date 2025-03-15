import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.util.Properties;

public class SMPServer {
    public static void main(String[] args) {
        int serverPort = 12345;  // Default port
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        // Load properties from config file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (Exception ex) {
            System.err.println("Failed to load config.properties");
            ex.printStackTrace();
            return;
        }

        // Set the keystore properties
        System.setProperty("javax.net.ssl.keyStore", props.getProperty("keystore.path"));
        System.setProperty("javax.net.ssl.keyStorePassword", props.getProperty("keystore.password"));

        try {
            // Create an SSL server socket
            SSLServerSocketFactory sslServerSocketFactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket =
                    (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverPort);

            System.out.println("SMP Server (SSL) ready on port " + serverPort);

            while (true) {  // Forever loop to accept client connections
                System.out.println("Waiting for a connection...");
                MyStreamSocket myDataSocket = new MyStreamSocket(sslServerSocket.accept());
                System.out.println("Connection accepted.");

                // Start a new thread to handle the client session
                Thread theThread = new Thread(new SMPThread(myDataSocket));
                theThread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}