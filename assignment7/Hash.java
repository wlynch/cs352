import java.security.*;
import java.io.*;

/**
 * Hash
 * Helper class with static methods for generating MD5 hashes.
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
*/

public class Hash {

  /**
   * Generates and MD5 hash of a path to a file.
   *
   * @param pathToFile String representation of path to a file
   * @return MD5 digest of the path
   */
	public static String generate(String pathToFile) {
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest="";
			byte[] mdbytes = md.digest(pathToFile.getBytes());

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			digest = sb.toString();
			return digest;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
