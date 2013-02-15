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
        ServerSocket svc = new ServerSocket(12345, 5);	// listen on port 12345

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
                String result = null;
                try {
                    result = stack.exec(line).toString();
                } catch (EmptyStackException e) {
                    toClient.writeBytes("The stack is empty.");
                } catch (UnsupportedOperationException e) {
                    toClient.writeBytes("That is an unsupported operation.");
                }
                toClient.writeBytes(result);	// send the result
            }
            System.out.println("closing the connection\n");
            conn.close();		// close connection and exit the thread
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
