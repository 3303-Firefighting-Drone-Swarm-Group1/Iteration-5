import java.io.*;
 import java.net.*;
 
 public class RPCServer implements Runnable {
     private int port;
     private Object handler;
 
     public RPCServer(int port, Object handler) {
         this.port = port;
         this.handler = handler;
     }
 
     @Override
     public void run() {
         try (ServerSocket serverSocket = new ServerSocket(port)) {
             System.out.println("RPC Server running on port " + port);
 
             while (true) {
                 try (Socket socket = serverSocket.accept();
                      ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                      ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
 
                     // Receive the request object
                     Object request = in.readObject();
 
                     // Process the request using the handler
                     Object response = processRequest(request);
 
                     // Send the response object
                     out.writeObject(response);
                     out.flush();
                 } catch (IOException | ClassNotFoundException e) {
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
 }