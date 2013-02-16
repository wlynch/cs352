import java.io.*;
import java.net.*;

/**
 * Thread for reading output from a given socket
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */

public class SocketReader implements Runnable {

    private Socket s;

    public SocketReader(Socket s) {
        this.s = s;
    }

    public void run(){
        String line;
        try {
            BufferedReader fromServer = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            while ((line = fromServer.readLine()) != null){
                System.out.println("=> "+line);
            }
        } catch (IOException e){
            /*
             * If we get here, it is because the writer has finished 
             * and the socket has been closed.
             */
        }

    }
}
