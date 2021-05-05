package filetransmission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TryFile {
	private final static String readEnding = "EOC";
	private final static byte[] readEndingByte = readEnding.getBytes();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {

			File file = new File("C:\\Users\\ZJZ\\Desktop\\Test\\testSmall.txt");
			FileWriter writer = new FileWriter(file);
			for (int i = 0; i < 1000; i++) {
				writer.write("outloop: " + i + "\n");

				for (int j = 0; j < 1000; j++) {
					writer.write("inner loop:" + j + ", ");
				}
				writer.write("\n");
				if (i % 100 == 0)
					System.out.println("outter loop: " + i + " finished");
			}
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
//		String s = "inner loop:199\nE OC";
//		System.out.println("Origin string: " + s);
//		byte[] b = s.getBytes();
//		System.out.println("End with EOC?" + readEnd(b));

	}

	public static boolean readEnd(byte[] buffer) {
		if (buffer.length < readEndingByte.length)
			return false;

		for (int i = 0; i < readEndingByte.length; i++) {
			if (buffer[buffer.length - 1 - i] != readEndingByte[readEndingByte.length - 1 - i])
				return false;
		}
		return true;
	}

}
