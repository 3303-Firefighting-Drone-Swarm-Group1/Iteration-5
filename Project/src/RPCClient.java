/**
 * A Client object used to communicate with Server objects
 */

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

     /**
      * Gets the port of the socket used by the RPCClient
      * @return the port number
      */
     public int getPort(){
        return socket.getLocalPort();
     }
 
     /**
      * Sends a message to the associated server
      * @param request the message sent
      * @return the response
      */
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

     /**
      * 
      * Sends a message to the associated server
      * @param request the message sent
      * @param timeout the time allowed for a response
      * @return the response
      */
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


     /**
      * Converts a serialiable object to a byte array
      * @param obj the object to be converted
      * @return the byte array
      */
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

    /**
     * Converts a byte array to its associated serializable object
     * @param bytes the bytes to be converted
     * @return the object
     */
    private Object fromString(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
 }
