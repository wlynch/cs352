import java.net.*;
import java.io.*;

public class CalcClient implements Runnable
{  private Socket socket              = null;
   private Thread thread              = null;
   private BufferedReader  console   = null;
   private DataOutputStream streamOut = null;
   private CalcClientThread client    = null;
   private InterruptInput end = null;
   private InterruptInputErr endErr = null;

   public CalcClient(String serverName, int serverPort)
   {  System.out.println("Establishing connection. Please wait ...");
      try
      {  socket = new Socket(serverName, serverPort);
         System.out.println("Connected: " + socket);
         start();
      }
      catch(UnknownHostException uhe)
      {  System.out.println("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  System.out.println("Unexpected exception: " + ioe.getMessage()); }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  streamOut.writeUTF(console.readLine());
            streamOut.flush();
         }
         catch(IOException ioe)
         {  System.out.println("Sending error: " + ioe.getMessage());
            stop();
         }
      }
   }
   public void handle(String msg)
   {  if (msg.equals(".bye")) //what do we do for termination trigger
      {  System.out.println("Good bye. Press RETURN to exit ...");
         stop();
      }
      else
         System.out.println(msg);
   }
   public void start() throws IOException
   {  console   = new BufferedReader(new InputStreamReader(System.in));
      streamOut = new DataOutputStream(socket.getOutputStream());
      if (thread == null)
      {  client = new CalcClientThread(this, socket);
         thread = new Thread(this);                   
         thread.start();
      }
   }
   public void stop()
   {  if (thread != null)
      {
         end = new InterruptInput();
         end.Interrupt();  
         thread = null;
      }
      try
      {  if (console   != null)  console.close();
         if (streamOut != null)  streamOut.close();
         if (socket    != null)  socket.close();
      }
      catch(IOException ioe)
      {  System.out.println("Error closing ..."); }
      client.close();  
      endErr = new InterruptInputErr();
	  endErr.Interrupt();
   }
   public static void main(String[] args)
   {  CalcClient client = null;
      if (args.length < 2){
         System.err.println("Usage: java CalcClient [host [port]]");
         System.exit(1);
      }
      else if (args.length == 1){
         client = new CalcClient(args[0], 8081);
      }
      else if (args.length == 2){ 
         client = new CalcClient(args[0], Integer.parseInt(args[1]));
      }
      else
         client = new CalcClient("localhost", 8081);
   }
}
