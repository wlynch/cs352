import java.io.*;
import java.net.*;

/**
 * Thread for writing output to a given socket
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */

public class SocketWriter implements Runnable {

    private Socket s;

    public SocketWriter(Socket s) {
        this.s = s;
    }

    /**
     * Thread method to write user input to the given socket
     */
    public void run(){
        
        String line;
        try{
            BufferedReader userData = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
            while ((line = userData.readLine()) != null){
                if (line.length() > 0){
                    toServer.writeBytes(line + '\n');
                }
            }
        } catch (IOException e) {
            System.out.println("Lost connection to server. Exiting.");
        }
    }
}
