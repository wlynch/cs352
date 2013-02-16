import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Server for calculations
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */
public class CalcServer implements Runnable {
    Socket conn;

    /**
     * Constructor
     *
     * @param sock The socket the CalcServer should connect on
     */
    public CalcServer(Socket sock) {
        this.conn = sock;
    }

    /**
     * Main method
     *
     * @throws Exception
     * @param args Command line arguments that are not used
     */
    public static void main(String[] args) throws Exception {
        ServerSocket svc = new ServerSocket(8081, 5);	// listen on port 12345

        for (;;) {
            Socket conn = svc.accept();	// get a connection from a client
            System.out.println("got a new connection");

            new Thread(new CalcServer(conn)).start();
        }
    }

    /**
     * Run method
     *
     * Runs the CalcServer
     */
    public void run() {
        try {
            CalcStack stack = new CalcStack();

            BufferedReader fromClient = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            DataOutputStream toClient = new DataOutputStream(
                conn.getOutputStream()
            );
            String line;

            while ((line = fromClient.readLine()) != null) {
                // while there's data from the client
                line = line.toLowerCase();
                double [] result = null;
                try {
                    result = stack.exec(line);
                } catch (EmptyStackException e) {
                    toClient.writeBytes("The stack is empty.\n");
                } catch (UnsupportedOperationException e) {
                    toClient.writeBytes("?\n");
                }
                if (result != null){
                    if (result.length == 0){
                        toClient.writeBytes("The stack is empty\n");
                    } else {
                        for (int i = 0; i < result.length; i++){
                            System.out.println(result[i]);
                            toClient.writeBytes(result[i]+"\n");	// send the result
                        }
                    }
                }
            }
            System.out.println("closing the connection\n");
            conn.close();		// close connection and exit the thread
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
