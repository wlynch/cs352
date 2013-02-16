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
        System.out.println("Reader is alive");
        String line;
        try {
            BufferedReader fromServer = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            while ((line = fromServer.readLine()) != null){
                System.out.println(line);
            }
        } catch (IOException e){
            System.out.println("Reader IO error");
            System.out.println(e);
        }

    }
}
