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
	private static HashMap<String,FileNode> filemap;
	private static ArrayList<PeerNode> peers;
	private static PeerNode localPeer;

	/**
	 * Constructor
	 *
	 * @param sock The socket the CalcServer should connect on
	 */
	public Server(Socket sock) {
		this.conn = sock;
	}

	/**
	 * Returns HTTP response with no content
	 *
	 * @param retcode HTTP return code to send
	 * @return HTTP response to send
	 */
	public String httpHeader(int retCode) {
		return httpHeader(retCode,null);
	}

	/**
	 * Returns HTTP response with given content
	 *
	 * @param retcode HTTP return code to send
	 * @param byte[] data
	 * @return HTTP response to send
	 */
	public String httpHeader(int retCode, byte[] data) {
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
                case 301:
                    output += "301 Moved Permanently\n";
                    output += "Location: " + new String(data) + "\n\n";
                    return output;
				case 404:
					output+="404 Not Found\n";
					break;
				case 400:
					output+="400 Bad Request\n";
					break;
			}
			output+="Content-Length: "+dataLength;
			output+="\nServer: p2pws\n\n";
			/*
			if (data != null) {
				output+=dataString;
			}
			*/
			return output;
		} catch (UnsupportedEncodingException e) {
			/* THIS SHOULDN'T HAPPEN */
			return null;
		}
	}

	/**
	 * Sends single message to a given peer
	 *
	 * @param message Message to send to the given peer
	 * @param peer Peer to send message to
	 */
	public static void sendMessage(String message, PeerNode peer) throws IOException {
		System.out.println("Sending message: ["+message+"] to: "+peer);
		Socket peerConn = new Socket(peer.getAddress(),peer.getPort());
		DataOutputStream toClient = new DataOutputStream(peerConn.getOutputStream());
		toClient.writeBytes(message);
	}

	/**
	 * Adds a peer to the current server
	 *
	 * @param rawInput Raw input from the the request. Important for determining recursion
	 */
	public synchronized void addPeer(String rawInput) throws IOException,UnknownHostException {
		System.out.println("Adding peer: "+rawInput);
		String[] input = rawInput.split(" ");
		PeerNode newpeer = new PeerNode(input[1]);
		
		if ( (input.length==2) || ((input.length >= 3) && (!input[2].equals("norecurse")))) {
			int size = peers.size();
			String message="";
			for (int i=0; i<size; i++) {
				PeerNode peer=peers.get(i);
				if (!peer.equals(localPeer)) {
					sendMessage("ADD "+newpeer+" norecurse\n", peer);
				}
				message+="ADD "+peer+" norecurse\n";
			}
			sendMessage(message,newpeer);
		}
		int index = locatePeer(newpeer.getHash());
		System.out.println("Before: "+peers);
		peers.add(index,newpeer);
		System.out.println("After: "+peers);
		System.out.println("Added peer "+newpeer);
		/* If newpeer is the immediate predecesor */
		if (peers.get(index+1).equals(localPeer)) {
			for (String filehash : filemap.keySet()) {
				if (filehash.compareTo(newpeer.getHash()) <= 0){
					FileNode file = filemap.get(filehash);
					/* Send file to peer */
					try {
						String message="PUT "+file.getName()+" HTTP/1.1\nContent-Length: "
							+file.getData().length+"\n\n"+new String(file.getData(),"UTF-8");
						sendMessage(message,newpeer);
					} catch (UnsupportedEncodingException e) {}
					filemap.remove(filehash);
				}
			}
		}

	}


	/**
	 * Search to determine the index of the PeerNode where a file/peer should be inserted
	 *
	 * @param hash Hash of file/peer to locate
	 * @return Index of peer file belongs to or index peer should be inserted
	 */
	public int locatePeer(String hash) {
		for (int i=0; i < peers.size(); i++) {
			if (peers.get(i).getHash().compareTo(hash) > 0) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Main method
	 *
	 * @throws Exception
	 * @param args Command line arguments that are not used
	 */
	public static void main(String[] args) throws Exception {
		int port = 8081;
		filemap = new HashMap<String,FileNode>();
		peers = new ArrayList<PeerNode>();

		if (args.length != 1) {
			System.err.println("usage:  java Server [port]");
			System.exit(1);
		} else {
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

		try {
			ServerSocket svc = new ServerSocket(port, 5);

			localPeer = new PeerNode(InetAddress.getLocalHost(),svc.getLocalPort());
			peers.add(localPeer);
		
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
			DataInputStream rawStream = new DataInputStream(conn.getInputStream());
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
						toClient.writeBytes(httpHeader(200,output.getBytes()));
						toClient.writeBytes(output);
					} else {
						String hash = Hash.generate(filename);
						System.out.println(hash);
						FileNode file = filemap.get(hash);
						if (file != null){
							toClient.writeBytes(httpHeader(200,file.getData()));
							toClient.write(file.getData(),0,file.getData().length);
						} else {
							PeerNode peer = peers.get(locatePeer(hash));
							if (peer.equals(localPeer)) {
								String dataString="<html>\n<head><title>404 Error</title></head>\n"+
									"<body>\n<h1>Not Found</h1>\nThe requested URL "+filename+
									" was not found\n</body>\n</html>";
								toClient.writeBytes(httpHeader(404, dataString.getBytes()));
								toClient.writeBytes(dataString);
							} else {
								String content = peer + filename;
								toClient.writeBytes(httpHeader(301, content.getBytes()));
							}
						}
					}
					break;
				} else if (line.startsWith("put ")) {
					String filename = input[1];
					int clength = 0;
					System.out.println("HTTP PUT "+filename);
					/* Get content length */
					while(!line.isEmpty()) {
						System.out.println("#["+line+"]"+line.length());
						if (line.startsWith("Content-Length: ")){
							clength = Integer.parseInt(line.substring(16));
							System.out.println("Got content length: "+clength);
						}
						line = fromClient.readLine();
					}
					System.out.println("Got out of loop");
					/* Read in content */
					byte[] data = new byte[clength];
					for (int i=0; i < clength; i++) {
						System.out.print(i);
						//data[i] = (byte)fromClient.read();
						data[i] = rawStream.readByte();
					}
					String hash = Hash.generate(filename);
					System.out.println("Hash: "+hash);
					PeerNode peer = peers.get(locatePeer(hash));
					if (peer.equals(localPeer)) {
						filemap.put(hash,new FileNode(filename,data));
						toClient.writeBytes(httpHeader(200));
					} else {
						String message = "PUT " + filename + " HTTP/1.1\n";
						message += "Content-Length: " + clength + "\n\n";
						sendMessage(message, peer);
						toClient.writeBytes(httpHeader(200));
					}
				} else if (line.startsWith("delete ")) {
					String filename=input[1];
					if (filemap.remove(Hash.generate(filename)) != null) {
						toClient.writeBytes(httpHeader(200));
					} else {
						String hash = Hash.generate(filename);
						PeerNode peer = peers.get(locatePeer(hash));
						if (peer.equals(localPeer)) {
							String dataString="<html>\n<head><title>404 Error</title></head>\n"+
								"<body>\n<h1>Not Found</h1>\nThe requested URL "+filename+
								" was not found\n</body>\n</html>";
							toClient.writeBytes(httpHeader(404,dataString.getBytes()));
							toClient.writeBytes(dataString);
						} else {
							String message = "DELETE " + filename + " HTTP/1.1\n";
							sendMessage(message, peer);
							toClient.writeBytes(httpHeader(200));
						}
					}
				} else if (line.startsWith("list ")) {
					System.out.println("LIST");
					String filelist="";
					for (FileNode file : filemap.values()) {
						System.out.println(file.getName());
						filelist+=file.getName()+"\n";
					}
					System.out.println("Results:\n"+filelist);
					toClient.writeBytes(httpHeader(200,filelist.getBytes()));
					toClient.writeBytes(filelist);
				} else if (line.startsWith("peers ")) {
					System.out.println("PEERS");
					String peerlist="";
					for (int i=0; i< peers.size(); i++) {
						peerlist+=peers.get(i)+"\n";
					}
					System.out.println("Results:\n"+peerlist);
					toClient.writeBytes(httpHeader(200,peerlist.getBytes()));
					toClient.writeBytes(peerlist);
				} else if (line.startsWith("add ")) {
					try {
						addPeer(line);
						toClient.writeBytes(httpHeader(200));
					} catch (Exception e) {
						toClient.writeBytes(httpHeader(400));
					}
				} else if (line.startsWith("remove ")) {
					System.out.println("REMOVE PEER");
					PeerNode target = new PeerNode(input[1]);
					if ((input.length == 2) || (input.length > 2 && !input[2].equals("norecurse"))) {
						for (PeerNode peer : peers) {
							sendMessage("REMOVE "+target+" norecurse\n",peer);
						}
					}
					if (localPeer.equals(target)) {
						int index = peers.indexOf(localPeer);
						for (String filehash : filemap.keySet()){
							FileNode file=filemap.get(filehash);
							if (peers.size() > 1) {
								String message="PUT "+file.getName()+"\nContent-Length: "+
									file.getData().length+"\n\n"+file.getData();
								PeerNode successor;
								if (index == peers.size()-1) {
									successor=peers.get(index-1);
								} else {
									successor=peers.get(index);
								}
								sendMessage(message,successor);
							}
							filemap.remove(filehash);
						}
					}
					System.out.println("Removing peer: "+target);
					System.out.println(peers);
					for (int i=0; i<peers.size()-1; i++) {
						if (peers.get(i).getHash().equals(target.getHash())) {
							peers.remove(peers.get(i));
							break;
						}
					}
					System.out.println(peers);
					toClient.writeBytes(httpHeader(200));
					System.exit(0);
					break;
				}
			}
			conn.close();		// close connection and exit the thread
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
