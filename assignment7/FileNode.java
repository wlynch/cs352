public class FileNode {
	private String name;
	private byte[] data;

	public FileNode(String name, byte[] data) {
		this.name=name;
		this.data=data;
	}

	public String getName() {
		return name;
	}

	public byte[] getData() {
		return data;
	}
}
