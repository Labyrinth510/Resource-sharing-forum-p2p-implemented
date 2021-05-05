package filetransmission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;

public class Verification {

	public static final int chunkSize = 1024 * 1024;

	public static String calculateChunkMD5(File file, long offset) {

//		System.out.println("start to encrypt offset " + offset + " of file:" + file);

		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(offset);

			byte[] buffer = new byte[chunkSize];
			int readLen = raf.read(buffer, 0, chunkSize);
			byte[] reads = Arrays.copyOfRange(buffer, 0, readLen);
			String md5 = DigestUtils.md5Hex(reads);

//			System.out.println("reads: " + (new String(reads, "UTF-8")));
//			System.out.println("md5: " + md5);

			return md5;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String> calculateHashList(File file) {

		int chunkNum = (int) Math.ceil(1.0 * file.length() / chunkSize);
		ArrayList<String> hashList = new ArrayList<String>();
		
		for (int i = 0; i < chunkNum; i++) {
			hashList.add(calculateChunkMD5(file, i * chunkSize));
		}
		
		return hashList;
	}
}
