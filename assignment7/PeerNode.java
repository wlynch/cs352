import java.net.*;

public class PeerNode {

	private String hash;
	String host;
	int port;

	public PeerNode(InetAddress host, int port, String hash) {
		this.host=host.getHostAddress();
		this.port=port;
		this.hash=hash;
	}

	public PeerNode(InetAddress host, int port) {
		this.host=host.getHostAddress();
		this.port=port;
		System.out.println(this.host+":"+port);
		this.hash=Hash.generate(this.host+":"+port);
	}

	public PeerNode(String hostport) throws UnknownHostException {
		String[] arr = hostport.split(":");
		this.host=InetAddress.getByName(arr[0]).getHostAddress();
		this.port=Integer.parseInt(arr[1]);
		System.out.println(host+":"+port);
		this.hash=Hash.generate(host+":"+port);
	}
	public String getHash() {
		return this.hash;
	}

	public String getAddress() {
		return this.host;
	}


	public int getPort() {
		return this.port;
	}

	public String toString() {
		return host+":"+port;
	}

	public boolean equals(PeerNode p) {
		return this.toString().equals(p.toString());
	}
}
