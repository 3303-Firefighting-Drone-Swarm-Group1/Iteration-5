import java.io.*;
 import java.net.*;
 import java.util.Base64;
 
 public class RPCServer implements Runnable {
     private int port;
     private Object handler;
 
     public RPCServer(int port, Object handler) {
         this.port = port;
         this.handler = handler;
     }
 
     @Override
     public void run() {
         try (DatagramSocket socket = new DatagramSocket(port)) {
             System.out.println("RPC Server running on port " + port);
 
             while (true) {
                 try {

                    DatagramPacket packet = new DatagramPacket(new byte[10000], 10000);
                    socket.receive(packet);
                    Object req = fromString(packet.getData());
                    int respondPort = packet.getPort();
                      

                     // Process the request using the handler
                     Object response = processRequest(req);
                     byte[] responseData= toString((Serializable)response);
 
                     // Send the response object
                     packet.setLength(responseData.length);
                     packet.setData(responseData);
                     packet.setPort(respondPort);
                     socket.send(packet);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private Object processRequest(Object request) {
         // Handle the request based on the handler
         if (handler instanceof Scheduler) {
             return ((Scheduler) handler).handleRequest(request);
         } else if (handler instanceof DroneSubsystem) {
             return ((DroneSubsystem) handler).handleRequest(request);
         }
         return null;
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
