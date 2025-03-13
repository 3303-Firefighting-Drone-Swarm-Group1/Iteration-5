import java.io.*;
import java.net.*;

public class RPCClient {
    private String host;
    private int port;

    public RPCClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object sendRequest(Object request) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send the request object
            out.writeObject(request);
            out.flush();

            // Receive the response object
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
