import java.net.*;

public class PeerNode {

	private String hash;
	InetAddress host;
	int port;

	public PeerNode(InetAddress host, int port, String hash) {
		this.host=host;
		this.port=port;
		this.hash=hash;
	}

	public PeerNode(InetAddress host, int port) {
		this.host=host;
		this.port=port;
		this.hash=Hash.generate(host+":"+port);
	}

	public PeerNode(String hostport) throws UnknownHostException {
		String[] arr = hostport.split(":");
		this.host=InetAddress.getByName(arr[0]);
		this.port=Integer.parseInt(arr[1]);
		this.hash=Hash.generate(hostport);
	}
	public String getHash() {
		return this.hash;
	}

	public InetAddress getAddress() {
		return this.host;
	}


	public int getPort() {
		return this.port;
	}

	public String toString() {
		return host.getHostAddress()+":"+port;
	}

	public boolean equals(PeerNode p) {
		return this.hash.equals(p.getHash());
	}
}
