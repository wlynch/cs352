/**
 * FileNode
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
*/
public class FileNode {
	private String name;
	private byte[] data;

  /**
   * Constructor for a FileNode.
   *
   * @param name Name of file
   * @param data The contents of the file as a byte array
   */
	public FileNode(String name, byte[] data) {
		this.name=name;
		this.data=data;
	}

  /**
   * Getter for the name of a FileNode.
   */
	public String getName() {
		return name;
	}

  /**
   * Getter for the data of a FileNode.
   */
	public byte[] getData() {
		return data;
	}
}
