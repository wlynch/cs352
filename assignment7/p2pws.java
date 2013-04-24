import java.io.*;
import java.net.*;
import java.util.*;

/**
 * P2PWS: Peer to Peer Web Server
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */
public class p2pws implements Runnable {
	private Socket conn;
	private static HashMap<String,FileNode> filemap;
	private static ArrayList<PeerNode> peers;
	private static PeerNode localPeer;

	/**
	 * Constructor
	 *
	 * @param sock The socket the CalcServer should connect on
	 */
	public p2pws(Socket sock) {
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
	 * Returns a HTTP header
	 *
	 * @param retcode HTTP return code to send
	 * @param byte[] data
	 * @return HTTP header to send
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
	public static void sendMessage(byte[] message, PeerNode peer) throws IOException {
		Socket peerConn = new Socket(peer.getAddress(),peer.getPort());
		DataOutputStream toClient = new DataOutputStream(peerConn.getOutputStream());
		toClient.write(message,0,message.length);
		toClient.flush();
	}

	/**
	 * Adds a peer to the current server
	 *
	 * @param rawInput Raw input from the the request. Important for determining recursion
	 */
	public synchronized void addPeer(String rawInput) throws IOException,UnknownHostException {
		String[] input = rawInput.split(" ");
		PeerNode newpeer = new PeerNode(input[1]);
		// Send add to all peers if norecurse not specified
		if ( (input.length==2) || ((input.length >= 3) && (!input[2].equals("norecurse")))) {
			int size = peers.size();
			String message="";
			for (int i=0; i<size; i++) {
				PeerNode peer=peers.get(i);
				if (!peer.equals(localPeer)) {
					String newpeerMessage = "ADD "+newpeer+ " norecurse\n";
					sendMessage(newpeerMessage.getBytes(), peer);
				}
				message+="ADD "+peer+" norecurse\n";
			}
			sendMessage(message.getBytes(),newpeer);
		}

		// Determine location to insert peer
		int index = locatePeerLoc(newpeer.getHash());
		if (index != -1) {
			if (!newpeer.equals(peers.get(index))) {
				peers.add(index,newpeer);
			}
		} else {
			peers.add(newpeer);
		}

		// If newpeer is the immediate predecesor
		int next = index+1;
		if (index == (peers.size()-1)) {
			next = 0;
		}
		if (peers.get(next).equals(localPeer)) {
			for (String filehash : filemap.keySet()) {
				if (filehash.compareTo(newpeer.getHash()) <= 0){
					FileNode file = filemap.get(filehash);
					// Send file to peer
					try {
						String message="PUT "+file.getName()+" HTTP/1.1\nContent-Length: "
							+file.getData().length+"\n\n"+new String(file.getData(),"UTF-8");
						sendMessage(message.getBytes(),newpeer);
					} catch (UnsupportedEncodingException e) {}
					filemap.remove(filehash);
				}
			}
		}
	}

	/**
	 * Search to determine the index of the PeerNode where a peer should be inserted
	 *
	 * @param hash Hash of peer to locate
	 * @return Index peer should be inserted
	 */
	public int locatePeerLoc(String hash) {
		for (int i=0; i < peers.size(); i++) {
			if (peers.get(i).getHash().compareTo(hash) >= 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Search to determine the index of the PeerNode where a peer should be inserted
	 *
	 * @param hash Hash of filename
	 * @return Index of peer to insert file into
	 */
	public int locateFileLoc(String hash) {
		for (int i=0; i < peers.size(); i++) {

			if (peers.get(i).getHash().compareTo(hash) >= 0) {
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
	public static void main(String[] args) {
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
				new Thread(new p2pws(conn)).start();
			}
		} catch (BindException e) {
			System.err.println("Port "+port+" is already in use.");
			System.exit(4);
		} catch (IOException e) {
			System.exit(5);
		}
	}

	/**
	 * Run method
	 *
	 * Handles client requests and runs the corresponding operation
	 */
	public void run() {
		try {
			// I/O Streams for the socket
			DataInputStream rawStream = new DataInputStream(conn.getInputStream());
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
				String[] input = line.split(" ");
				// Determine which command to run
				if (line.startsWith("get ")){
					//HTTP GET
					String filename=input[1];
					if (filename.equals("/local.html")){
						// Special local.html case
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
						FileNode file = filemap.get(hash);
						if (file != null){
							// If file exists on this peer, return the file
							toClient.writeBytes(httpHeader(200,file.getData()));
							toClient.write(file.getData(),0,file.getData().length);
						} else {
							PeerNode peer = peers.get(locateFileLoc(hash));
							if (peer.equals(localPeer)) {
								/* If the file does not exist on this peer, but should be,
								 * return 404
								 */
								String dataString="<html>\n<head><title>404 Error</title></head>\n"+
									"<body>\n<h1>Not Found</h1>\nThe requested URL "+filename+
									" was not found\n</body>\n</html>";
								toClient.writeBytes(httpHeader(404, dataString.getBytes()));
								toClient.writeBytes(dataString);
							} else {
								// If the file should exist on another peer, send a redirect
								String content = peer + filename;
								toClient.writeBytes(httpHeader(301, content.getBytes()));
							}
						}
					}
					break;
				} else if (line.startsWith("put ")) {
					// HTTP PUT
					String filename = input[1];
					int clength = 0;

					/* Read in header, get content length */
					while(!line.isEmpty()) {
						if (line.startsWith("Content-Length: ")){
							clength = Integer.parseInt(line.substring(16));
						}
						line = fromClient.readLine();
					}
					/* Read in content */
					byte[] data = new byte[clength];
					for (int i=0; i < clength; i++) {
						data[i] = rawStream.readByte();
					}

					// Generate hash for filename and get the location for the file
					String hash = Hash.generate(filename);
					PeerNode peer = peers.get(locateFileLoc(hash));
					if (peer.equals(localPeer)) {
						// If content belongs on this peer, store content
						filemap.put(hash,new FileNode(filename,data));
						toClient.writeBytes(httpHeader(200));
					} else {
						// If content is on remote peer, resend data to other peer
						String message = "PUT " + filename + " HTTP/1.1\n";
						message += "Content-Length: " + clength + "\n\n";
						byte[] msgBytes = message.getBytes();
						byte[] bytesToSend = new byte[msgBytes.length+data.length];
						for (int i=0; i < msgBytes.length; i++) {
							bytesToSend[i] = msgBytes[i];
						}
						for (int i=0; i < data.length; i++) {
							bytesToSend[i+msgBytes.length] = data[i];
						}
						sendMessage(bytesToSend, peer);
						toClient.writeBytes(httpHeader(200));
					}
				} else if (line.startsWith("delete ")) {
					// HTTP DELETE
					String filename=input[1];
					if (filemap.remove(Hash.generate(filename)) != null) {
						// If we delete the file from this peer, return OK
						toClient.writeBytes(httpHeader(200));
					} else {
						// Get intended location for file
						String hash = Hash.generate(filename);
						PeerNode peer = peers.get(locateFileLoc(hash));
						if (peer.equals(localPeer)) {
							/* If file should be on this peer but we could not delete it,
							 * it does not exist. Return 404.
							 */
							String dataString="<html>\n<head><title>404 Error</title></head>\n"+
								"<body>\n<h1>Not Found</h1>\nThe requested URL "+filename+
								" was not found\n</body>\n</html>";
							toClient.writeBytes(httpHeader(404,dataString.getBytes()));
							toClient.writeBytes(dataString);
						} else {
							// If file should be on another peer, redirect command to the other peer
							String message = "DELETE " + filename + " HTTP/1.1\n";
							sendMessage(message.getBytes(), peer);
							toClient.writeBytes(httpHeader(200));
						}
					}
					break;
				} else if (line.startsWith("list ")) {
					// NON-HTTP LIST
					String filelist="";
					// Get list of all FileNodes, make a list of their names
					for (FileNode file : filemap.values()) {
						filelist+=file.getName()+"\n";
					}
					toClient.writeBytes(httpHeader(200,filelist.getBytes()));
					toClient.writeBytes(filelist);
					break;
				} else if (line.startsWith("peers ")) {
					// NON-HTTP PEERS
					String peerlist="";
					// Get list of the peers
					for (int i=0; i< peers.size(); i++) {
						peerlist+=peers.get(i)+"\n";
					}
					toClient.writeBytes(httpHeader(200,peerlist.getBytes()));
					toClient.writeBytes(peerlist);
					break;
				} else if (line.startsWith("add ")) {
					// Add a given peer to the group of peers
					try {
						addPeer(line);
						toClient.writeBytes(httpHeader(200));
					} catch (Exception e) {
						// If unable to connect for any reason, return 400
						toClient.writeBytes(httpHeader(400));
					}
					break;
				} else if (line.startsWith("remove ")) {
					// NON-HTTP REMOVE
					PeerNode target = new PeerNode(input[1]);
					// Check to see if we need to send command to other peers
					if ((input.length == 2) || (input.length > 2 && !input[2].equals("norecurse"))) {
						for (PeerNode peer : peers) {
							String message = "REMOVE "+target+" norecurse\n";
							sendMessage(message.getBytes(),peer);
						}
					}

					// If we are removing ourselves, move all files to the successor
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
								sendMessage(message.getBytes(),successor);
							}
							filemap.remove(filehash);
						}
					}

					// Remove the peer from the list of peers
					for (int i=0; i<peers.size(); i++) {
						if (peers.get(i).equals(target)) {
							peers.remove(peers.get(i));
							break;
						}
					}

					toClient.writeBytes(httpHeader(200));
					if (target.equals(localPeer)) {
						// If we are removing ourselves, exit
						System.exit(0);
					}
					break;
				} else {
					// If not a valid command, return 400
					toClient.writeBytes(httpHeader(400));
				}
			}
			conn.close();		// close connection and exit the thread
		} catch (IOException e) {} //Make sure we don't crash on any peer disconnects
	}
}
