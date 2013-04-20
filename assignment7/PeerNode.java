import java.net.InetAddress;
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

	public String getHash() {
		return this.hash;
	}

	public InetAddress getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String toString() {
		return host.getHostAddress()+":"+port;
	}
}
