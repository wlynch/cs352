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
        int port = 8081;
        if (args.length > 1) {
            System.err.println("usage:  java CalcServer [port]");
            System.exit(1);
        } else if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("usage:  java CalcServer [port]");
                System.err.println("argument 'port' must be a valid integer.");
                System.exit(2);
            }
            if (port < 1024 || port > 65535){
                System.err.println("usage:  java CalcServer [port]");
                System.err.println("argument 'port' must be between 1024-65535");
                System.exit(3);
            }
        }

        ServerSocket svc = new ServerSocket(port, 5);

        for (;;) {
            Socket conn = svc.accept();	// get a connection from a client
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

            // while there's data from the client
            while ((line = fromClient.readLine()) != null) {
                // format input to ignore case and leading/trailing whitespace
                line = line.trim().toLowerCase();
                double [] result = null;
                // Catch exceptions from CalcStack
                try {
                    result = stack.exec(line);
                } catch (EmptyStackException e) {
                    toClient.writeBytes("not enough numbers on the stack!\n");
                } catch (UnsupportedOperationException e) {
                    toClient.writeBytes("?\n");
                }
                // Return result of operations
                if (result != null){
                    if (result.length == 0){
                        toClient.writeBytes("The stack is empty.\n");
                    } else {
                        for (int i = 0; i < result.length; i++){
                            toClient.writeBytes(result[i]+"\n");
                        }
                    }
                }
            }
            conn.close();		// close connection and exit the thread
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
