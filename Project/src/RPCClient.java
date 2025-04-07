import java.io.*;
 import java.net.*;
 
 public class RPCClient {
     private String host;
     private int port;
     private DatagramSocket socket;
 
     public RPCClient(String host, int port) {
         this.host = host;
         this.port = port;
     }

     public int getPort(){
        return socket.getLocalPort();
     }
 
     public Object sendRequest(Object request) {
         try {
            socket = new DatagramSocket();
            byte[] req = toString((Serializable)request);
            DatagramPacket packet = new DatagramPacket(req, req.length, InetAddress.getLocalHost(), port);
            socket.send(packet);
            socket.receive(packet);
            return fromString(packet.getData());
         } catch (Exception e) {
             return null;
         }
     }

     public Object sendRequest(Object request, int timeout){
        try {
            socket = new DatagramSocket();
            byte[] req = toString((Serializable)request);
            DatagramPacket packet = new DatagramPacket(req, req.length, InetAddress.getLocalHost(), port);
            socket.send(packet);
            socket.setSoTimeout(timeout);
            socket.receive(packet);
            return fromString(packet.getData());
         } catch (Exception e) {
             return null;
         }
     }


     private byte[] toString(final Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
    
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object fromString(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
 }
