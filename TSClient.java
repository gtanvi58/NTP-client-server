import java.io.*;
import java.net.*;
import java.util.HashMap;

class TSClient {


	public static void main(String argv[]) throws Exception {
        String hostName = "";
        Long offset_CS = null;
        Long rTT = null;
        Long currentOffset = null;
        Long current_RTT = null;
        // Check if host name sent as command line argument and set it else set it to localhost.
        if(argv.length == 0)
        {
            hostName = "localhost";
        }
        else
        {
            hostName = argv[0];
        }
        Socket clientSocket = null;
        try{
            // 8 pings to server to calculate the offset with minimum delay.
            for(int i=0;i<8;i++)
            {
                // Try to establish a connection with server.
                clientSocket = new Socket(hostName, 13503);
                // Creating object output stream from socket's output stream.
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                // Creating object input stream to read from socket connection.
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
                // Local time for sending packet to server.
                Long T1 = System.currentTimeMillis();
                outToServer.writeUTF(T1.toString());
                outToServer.flush();
                
                String receivedString = inFromServer.readLine();
                // Local time in client when packet received from server
                Long T4 = System.currentTimeMillis();
                String  stringValue = receivedString.trim();
                String[] parts = stringValue.split(",");

                Long T2 = Long.parseLong(parts[0]);
                Long T3 = Long.parseLong(parts[1]);
                
                // Calculating offset for this ping to server.
                currentOffset = 
                ((T4 - T3) - 
                (T2 - T1))/2;
                
                //Calculating round trip time for this ping to server.
                current_RTT = 
                (T4 - T1) - 
                (T3 - T2);

                // Choosing the best delay out of the 8 runs
                if(rTT == null || current_RTT< rTT) {
                    offset_CS = currentOffset;
                    rTT = current_RTT;
                }

            }

            // Pausing the thread for 5 seconds
            Thread.sleep(5000);

            /** 
             * REMOTE_TIME: Server time synchronised by client
             * LOCAL_TIME: Local time on client machine
             * RTT_ESTIMATE: the estimated round-trip time between client and your server.
             */ 

            System.out.println("REMOTE_TIME " + (System.currentTimeMillis() - offset_CS) + 
            "\nLOCAL_TIME " + System.currentTimeMillis() +
            "\nRTT_ESTIMATE " + rTT);
        } catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try{
                if(null != clientSocket) {
                    // Closing socket closes the streams too.
                    clientSocket.close();
                }
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
	}
}