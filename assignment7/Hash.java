import java.security.*;
import java.io.*;

public class Hash {
	public static String generate(String pathToFile) {
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest="";
			byte[] mdbytes = md.digest(pathToFile.getBytes());

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			//System.out.println("in hex :: "+ sb.toString());

			digest = sb.toString();
			return digest;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	
}
