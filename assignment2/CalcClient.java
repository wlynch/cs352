import java.io.*;
import java.net.*;

/**
 * Client for calculations
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */
public class CalcClient {
    /**
     * Main method
     *
     * @throws Exception
     * @param args Unused command line arguments
     */
    public static void main(String[] args) {
        String line;    // user input
        String server = "localhost";    // default server
        int port = 8081;    // default Port
        Socket sock = null;

        // Parse the arguments
        if (args.length > 2) {
            System.err.println("usage:	java CalcClient [hostname [port]]");
            System.exit(1);
        } else if (args.length == 1) {
            server = args[0];
        } else if (args.length == 2) {
            server = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                System.err.println("usage:  java CalcClient [hostname [port]]");
                System.err.println("argument 'port' must be a valid integer.");
                System.exit(2);
            } catch(Exception e) {
                System.err.println("Unspecified error. Exiting...");
                System.exit(-1);
            }
            if (port < 1024 || port > 65535){
                System.err.println("usage:  java CalcClient [hostname [port]]");
                System.err.println("argument 'port' must be between 1024-65535");
                System.exit(3);
            }
        }

        try {
            try {
                sock = new Socket(server, port);
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: "+server);
                System.exit(4);
            } catch (ConnectException e) {
                System.err.println("Cannot connect to "+server+" on port "+port+". Please try again.");
                System.exit(5);
            } catch (SocketException e) {
                System.err.println("Invalid hostname: "+server);
                System.exit(6);
            }
            // Start reader and writer threads for the given socket
            Thread reader = new Thread(new SocketReader(sock));
            Thread writer = new Thread(new SocketWriter(sock));
            reader.start();
            writer.start();

            // Wait for the user to close the writer before exiting
            try {
                writer.join();
            } catch (InterruptedException e) {}

            sock.close();	// we're done with the connection
        } catch (IOException e) {
            System.err.println("Socket IO error. Exiting.");
            System.exit(7);
        }
        System.exit(0);
    }
}
