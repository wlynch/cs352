import java.security.*;
import java.io.*

public class hash {
	public static String generate(String pathToFile) {

		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(pathToFile);
		byte[] data = new byte[1024];
		int nread = 0;
		String digest="";

		while((nread = fis.read(data)) != -1) {
			md.update(data, 0, nread);
		};
		byte[] mdbytes = md.digest();

		//byte to hex

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		//System.out.println("in hex :: "+ sb.toString());

		digest = sb.toString();
		return digest;
	}	
}
