import java.io.*;
import java.net.*;

public class RPCClient {
    private InetAddress host;
    private int port;

    public RPCClient(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getPort(){
        return port;
    }


    public Object sendRequest(Object request) {
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Send the request object
            out.writeObject(request);
            out.flush();

            Object o = in.readObject();
            socket.close();

            // Receive the response object
            return o;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
