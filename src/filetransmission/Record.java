package filetransmission;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;

public class Record implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4260338618013991326L;
	
	private boolean endRecord;
	private int fileID;
	private File filePath;
	private long fileSize;
	private int chunkNum;
	private ArrayList<String> hashList;
	private BitSet chunkList;

	public Record(FileThread f, boolean endRecord) {
		this.endRecord = endRecord;
		this.fileID = f.getFileID();
		this.filePath = f.getFile();
		this.fileSize = f.getFileSize();
		this.chunkNum = f.getChunkNum();
		this.chunkList = f.getChunkList();
		this.hashList=f.getHashList();
	}

	public int getFileID() {
		return this.fileID;
	}

	public File getFilePath() {
		return this.filePath;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public int getChunkNum() {
		return this.chunkNum;
	}

	public BitSet getChunkList() {
		return this.chunkList;
	}

	public boolean isEndRecord() {
		return this.endRecord;
	}

	public ArrayList<String> getHashList(){
		return this.hashList;
	}
	
	public String toString() {
		String s = "";
		if (endRecord)
			s += "last one of the record files\n";
		s += "file ID: " + fileID + ", fileSize: " + fileSize + "\nfile path: " + filePath + "\nchunk num: " + chunkNum
			+ "\nChunk list: " + chunkList;
		return s;
	}
}
