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
    public static void main(String[] args) throws Exception {
        String line;    // user input
        String server = "localhost";    // default server
        int port = 8081;
        BufferedReader userdata = new BufferedReader(
                new InputStreamReader(System.in)
                );

        if (args.length > 2) {
            System.err.println("usage:	java CalcClient [hostname [port]]");
            System.err.println("or:		java CalcClient [hostname]");
            System.err.println("or:		java CalcClient");
            System.exit(1);
        } else if (args.length == 1) {
            server = args[0];
            System.out.println("server = " + server);
        } else if (args.length == 2) {
            server = args[0];
            System.out.println("server = " + server);
            try {
                port = Integer.parseInt(args[1]);  
            }
            catch(NumberFormatException e) {
                System.err.println("usage:  java CalcClient [hostname [port]]");
                System.err.println("argument 'port' must be a valid integer");
                System.exit(1);
            }
            catch(Exception e){
                System.err.println("Unspecified error. Exiting...");
                System.exit(1);
            }
            System.out.println("port = " + port);
        }

        Socket sock = null;
        DataOutputStream toServer = null;
        BufferedReader fromServer = null;

        try {
            sock = new Socket(server, port);
            toServer = new DataOutputStream(sock.getOutputStream());
            fromServer = new BufferedReader(
                    new InputStreamReader(sock.getInputStream())
                    );
        } catch(UnknownHostException e){
            System.err.println("Don't know about host:" +server);
            System.exit(1);
        } catch(ConnectException e) {
            System.err.println("Cannot connect to "+server+" on port "+port+". Please try again.");
            System.exit(1);
        } catch(IOException e){
            System.err.println("Couldn't get I/O for host:" +server);
            System.exit(1);
        }

        while ((line = userdata.readLine()) != null) {
            toServer.writeBytes(line + '\n');	// send the line to the server
            String result = fromServer.readLine();	// read a one-line result
            System.out.println(result);		// print it
        }
        sock.close();	// we're done with the connection
    }
}
