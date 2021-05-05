package usage;

public class TaskInfo {

	private int fid;
	private String taskName;
	private String status;
	private String fileSize;
	private int downloadPercent;
	private String uploadAmount;
	private String downloadAmount;
	private String fileLocation;

	public TaskInfo(filetransmission.FileThread fileThread) {
		
		this.fid=fileThread.getFileID();
		this.status=fileThread.getStage();
		this.taskName = parseFileName(fileThread.getFile().getAbsolutePath());
		this.fileSize = toDisplaySize(fileThread.getFileSize());
		this.downloadAmount = toDisplaySize(fileThread.getDownloads());
		this.uploadAmount = toDisplaySize(fileThread.getUploads());

		double percent = 1.0 *  fileThread.getChunkList().cardinality()/ fileThread.getChunkNum();
		this.downloadPercent=(int)(percent*100);
		//this.downloadPercent = String.format("%.2f", (percent * 100)) + "%";

		this.fileLocation = fileThread.getFile().getAbsolutePath();
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

	public static String toDisplaySize(long size) {
		double fileSize;
		String sizeUnit;
		if (size < 1000) {
			fileSize = size;
			sizeUnit = "B";
		} else if (size >= 1000 && size < 1000 * 1000) {
			fileSize = 1.0 * size / 1000;
			sizeUnit = "KB";
		} else if (size >= 1000 * 1000 && size < 1000 * 1000 * 1000) {
			fileSize = 1.0 * size / 1000 / 1000;
			sizeUnit = "MB";
		} else if (size >= 1000 * 1000 * 1000 && size < 1000 * 1000 * 1000 * 1000) {
			fileSize = 1.0 * size / 1000 / 1000 / 1000;
			sizeUnit = "GB";
		} else {
			fileSize = 1.0 * size / 1000 / 1000 / 1000 / 1000;
			sizeUnit = "TB";
		}

		return String.format("%.2f", fileSize) + sizeUnit;
	}

	public int getFileID() {
		return this.fid;
	}
	
	public String getTaskName() {
		return this.taskName;
	}
	
	public String getStatus() {
		return this.status;
	}

	public String getFileSize() {
		return this.fileSize;
	}
	
	public int getDownloadPercent() {
		return this.downloadPercent;
	}
	
	public String getUploadAmount() {
		return this.uploadAmount;
	}
	
	public String getDownloadAmount() {
		return this.downloadAmount;
	}
	
	public String getFileLocation() {
		return this.fileLocation;
	}

}
