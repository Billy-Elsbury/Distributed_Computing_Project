import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class ClientHelper {
    private final MyStreamSocket mySocket;

    public ClientHelper(String hostName, int port) throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(hostName, port);
        this.mySocket = new MyStreamSocket(sslSocket);
    }

    public String login(String username, String password) throws IOException {
        mySocket.sendMessage(RequestCodes.LOGIN + " " + username + " " + password);
        return mySocket.receiveMessage();
    }

    public String upload(String username, int messageId, String message) throws IOException {
        mySocket.sendMessage(RequestCodes.UPLOAD + " " + username + " " + messageId + " " + message);
        return mySocket.receiveMessage();
    }

    public String download(String input) throws IOException {
        if (input.equalsIgnoreCase("all")) {
            mySocket.sendMessage(RequestCodes.DOWNLOAD_ALL + "");
        } else {
            mySocket.sendMessage(RequestCodes.DOWNLOAD + " " + input);
        }
        return mySocket.receiveMessage();
    }

    public String clear() throws IOException {
        mySocket.sendMessage(RequestCodes.CLEAR + "");
        return mySocket.receiveMessage();
    }

    public void close() throws IOException {
        mySocket.close();
    }
}