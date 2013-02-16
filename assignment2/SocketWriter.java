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

    public void run(){
        
        String line;
        try{
            BufferedReader userData = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
            while ((line = userData.readLine()) != null){
                if (line.length() > 0){
                    System.out.println("["+line+"]");
                    toServer.writeBytes(line + '\n');
                }
            }
        } catch (IOException e) {
            System.out.println("Writer IO error");
        }
    }
}
