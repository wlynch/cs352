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
public class Server implements Runnable {
	private Socket conn;
	private static HashMap<String,byte[]> filemap;		
	/**
	 * Constructor
	 *
	 * @param sock The socket the CalcServer should connect on
	 */
	public Server(Socket sock) {
		this.conn = sock;
	}

	public String httpResponse(int retCode) {
		return httpResponse(retCode,null);
	}

	public String httpResponse(int retCode, byte[] data) {
		try {
			String dataString="";
			int dataLength=0;
			if (data != null) {
				dataString = new String(data,"UTF-8");
				dataLength = dataString.length();
			}
			String output="HTTP/1.1 ";
			switch(retCode) {
				case 200:
					output+="200 OK\n";
					break;
				case 404:
					output+="404 Not Found\n";
					dataString="<html>\n<head><title>404 Error</title></head>\n"+
						"<body>\n<h1>Not Found</h1>\nThe requested URL "+dataString+
						" was not found\n</body>\n</html>";
					dataLength=dataString.length();
					break;
			}
			output+="Content-Length: "+dataLength;
			output+="\nServer: p2pws\n\n";
			if (data != null) {
				output+=dataString;
			}
			return output;
		} catch (UnsupportedEncodingException e) {
			/* THIS SHOULDN'T HAPPEN */
			return null;
		}
	}
	/**
	 * Main method
	 *
	 * @throws Exception
	 * @param args Command line arguments that are not used
	 */
	public static void main(String[] args) throws Exception {
		int port = 8081;
		filemap = new HashMap<String,byte[]>();
		if (args.length > 1) {
			System.err.println("usage:  java Server [port]");
			System.exit(1);
		} else if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("usage:  java Server [port]");
				System.err.println("argument 'port' must be a valid integer.");
				System.exit(2);
			}
			if (port < 1024 || port > 65535){
				System.err.println("usage:  java Server [port]");
				System.err.println("argument 'port' must be between 1024-65535");
				System.exit(3);
			}
		}

		/* Insert peer add here */

		try {
			ServerSocket svc = new ServerSocket(port, 5);

			for (;;) {
				Socket conn = svc.accept();	// get a connection from a client
				new Thread(new Server(conn)).start();
			}
		} catch (BindException e) {
			System.err.println("Port "+port+" is already in use.");
			System.exit(4);
		}
	}

	/**
	 * Run method
	 *
	 * Handles client requests and runs the corresponding operation
	 */
	public void run() {
		try {
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
				//START MASSIVE BLOCK OF IF STATEMENTS
				System.out.println("["+line+"]");
				String[] input = line.split(" ");
				if (line.startsWith("get ")){
					String filename=input[1];
					System.out.println("Got a HTTP GET");
					if (filename.equals("/local.html")){
						String output="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
						output+="<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n";
						output+="<head>\n";
						output+="<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />";
						output+="<title> Local page </title>\n";
						output+="</head>\n";
						output+="<body>\n";
						output+="<p> This is the local page on peer server "+conn.getLocalAddress().toString().substring(1)+" port "+conn.getLocalPort()+" </p>\n";
						output+="</body>\n";
						output+="</html>";
						toClient.writeBytes(httpResponse(200,output.getBytes()));
					} else {
						System.out.println(Hash.generate(filename));
						byte[] data = filemap.get(Hash.generate(filename));
						if (data != null){
							toClient.writeBytes(httpResponse(200,data));
						} else {
							toClient.writeBytes(httpResponse(404,filename.getBytes()));
						}
					}
					break;
				} else if (line.startsWith("put ")) {
					String filename = input[1];
					int clength = 0;
					System.out.println("HTTP PUT "+filename);
					/* Get content length */
					while(!line.equals("")) {
						System.out.println("#["+line+"]"+line.length());
						if (line.startsWith("Content-Length: ")){
							clength = Integer.parseInt(line.substring(16));
							System.out.println("Got content length: "+clength);
						}
						line = fromClient.readLine();
					}
					/* Read in content */
					byte[] data = new byte[clength];
					for (int i=0; i < clength; i++) {
						data[i] = (byte)fromClient.read();
					}
					System.out.println("Hash: "+Hash.generate(filename));
					filemap.put(Hash.generate(filename),data);
					toClient.writeBytes(httpResponse(200));
				} else if (line.startsWith("delete ")) {
					String filename=input[1];
					if (filemap.remove(Hash.generate(filename)) != null) {
						toClient.writeBytes(httpResponse(200));
					} else {
						toClient.writeBytes(httpResponse(404,filename.getBytes()));
					}
				} else if (line.startsWith("list ")) {
					System.out.println("LIST");
					String filelist="";
					for (String key : filemap.keySet()) {
						System.out.println(key);
						filelist+=key+"\n";
					}
					System.out.println("Results:\n"+filelist);
					toClient.writeBytes(httpResponse(200,filelist.getBytes()));
				} else if (line.startsWith("peers ")) {

				} else if (line.startsWith("remove ")) {

				}
			}
			conn.close();		// close connection and exit the thread
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
