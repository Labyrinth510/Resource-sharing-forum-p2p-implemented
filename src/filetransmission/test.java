package filetransmission;

import java.io.File;
import java.util.ArrayList;

public class test {

	public static final long chunkSize=1024*1024;
	public static void main(String[] args) {
		
//		String path="/media/lucas/WD 2T/共用文件/文档/学习/COMP208 Group Software  Project/test/test.txt";
//		File f=new File(path);
//		System.out.println("file name: "+parseFileName(f.getAbsolutePath()));
		File file=new File("/media/lucas/WD 2T/共用文件/文档/学习/COMP208 Group Software  Project/test/testSmall.txt");
		ArrayList<String> hashList=Verification.calculateHashList(file);
		
		for(int i=0;i<hashList.size();i++) {
			System.out.println("chunk "+i+ " MD5:\t"+hashList.get(i));
			System.out.println("chunk "+i+ " MD52:\t"+Verification.calculateChunkMD5(file, i*chunkSize));
		}
			
	}
	
	public static String parseFileName(String path) {
		int start;
		for(start=path.length()-1;start>=0;start--)
			if(path.charAt(start)=='/'||path.charAt(start)=='\\') {
				break;
			}
		
		if(start==0) return path;
		return path.substring(start+1);
	}
}
