import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Template for worker threads.
 */
class ConnectionThread implements Runnable{

    Socket socket;
    Map<String,Long> ntpTimesMap;

    ConnectionThread(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        InputStream socketInputStream = null;
        OutputStream socketOutputStream = null;
        ObjectOutputStream objectOS = null;
        ObjectInputStream objectIS = null;
        try {
            // Read from the socket input stream.
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Get output stream from socket to write to socket.
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            String receivedString = inFromClient.readLine();
            // Local time in server when packet is received from client.
            Long t2 = System.currentTimeMillis();
            // Local time in server when packet is sent to client.
            Long t3 = System.currentTimeMillis();
            String responseString = t2.toString() + "," + t3.toString();
            // Write to output stream.
            outToClient.writeUTF(responseString);
            outToClient.flush();

        } catch (IOException e) {
            System.out.println("IOException occurred while reading from socket input stream");
            e.printStackTrace();
        } finally {
            try{
                if(null != socket) {
                    socket.close();
                }
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}

public class TSServer {  

    public static void main(String[] args) throws IOException{
        // Server listening for new connections on port 13503
        ServerSocket serverSocket = new ServerSocket(13503);
        // Service to manage threadpool
        ExecutorService threadPoolManager = Executors.newFixedThreadPool(5);
        try {
            while(true) {
                try {
                    // Accept an incoming connection
                    Socket socket = serverSocket.accept();
                    // Spawn a new worker thread from the thread pool for the new connection. 
                    Runnable connectionThread = new ConnectionThread(socket);
                    // Execute the worker thread and wait for new connections.
                    threadPoolManager.execute(connectionThread);
                } catch(IOException exception) {
                    System.out.println("IO Exception occurred while running multiple threads!!");
                    exception.printStackTrace();
                }
            }
        } finally {
            // Release resources from the thread pool.
            threadPoolManager.shutdown();
            // Close the server socket.
            serverSocket.close();
        }
    }
}
