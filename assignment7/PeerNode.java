import java.net.*;

/**
 * PeerNode
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
*/
public class PeerNode {

	private String hash;
	private String host;
	private int port;

  /**
   * Constructor for PeerNode.
   *
   * @param host Host address of peer
   * @param port Port that the peer is running on
   * @param hash Hash of peer
   */
	public PeerNode(InetAddress host, int port, String hash) {
		this.host=host.getHostAddress();
		this.port=port;
		this.hash=hash;
	}

  /**
   * Constructor for PeerNode.
   * Generates hash based on host and port.
   *
   * @param host Host address of peer
   * @param port Port that the peer is running on
   */
	public PeerNode(InetAddress host, int port) {
		this.host=host.getHostAddress();
		this.port=port;
		this.hash=Hash.generate(this.host+":"+port);
	}

  /**
   * Constructor for PeerNode.
   * Generates hash based on host and port.
   *
   * @param hostport Hostname and port in the format of hostname:port
   */
	public PeerNode(String hostport) throws UnknownHostException {
		String[] arr = hostport.split(":");
		this.host=InetAddress.getByName(arr[0]).getHostAddress();
		this.port=Integer.parseInt(arr[1]);
		this.hash=Hash.generate(host+":"+port);
	}

  /**
   * Getter for hash of this peer.
   * @return Hash of this peer
   */
	public String getHash() {
		return this.hash;
	}

  /**
   * Getter for address of this peer.
   * @return Address of this peer
   */
	public String getAddress() {
		return this.host;
	}

  /**
   * Getter for port of this peer.
   * @return Port of this peer
   */
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
