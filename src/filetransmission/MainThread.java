package filetransmission;

import com.alibaba.fastjson.JSONObject;
import usage.UserInfo;
import web.ServerRequester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainThread implements Runnable {

	private HashMap<Integer, FileThread> fileThreads = new HashMap<Integer, FileThread>();
	private HashMap<Integer, Future<?>> currentFutures = new HashMap<Integer, Future<?>>();
	private ThreadPoolExecutor executor; // thread pool of the download
	// private ArrayList<Future<?>> currentFutures = new ArrayList<Future<?>>();

	// request to the server
	private ServerRequester requester;

	private ServerSocket welcomeSocket;
	private int port;
	private String hostName;

	// user information
	private int ID;
	private String userName;
	private volatile long userPoint;
	private RequestHandler requestHandler;
	private int threadMaxNum = 10;
	private int threadBufferMaxNum = 20;

	private long totalUploads;
	private long totalDownloads;

	private File recordFile;

	// is this main thread is end
	private boolean isEnd;

	public MainThread(ServerRequester requester, UserInfo userInfo) {
		// TODO Auto-generated method stub
		try {
			this.ID = 1;
			this.hostName = getHostIp();
			this.userName=userInfo.getUsername();
			this.userPoint=userInfo.getPoint();

			this.port = 8001;

			this.totalUploads = 0;
			this.totalDownloads = 0;
			welcomeSocket = new ServerSocket(port);
			this.requestHandler = new RequestHandler(this);
			this.requester=requester;

			recordFile = new File("."+File.separator+"records"+File.separator
					+ userName + "-record.txt");

			// initialize the thread pool
			executor = new ThreadPoolExecutor(threadMaxNum, threadBufferMaxNum, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public MainThread(int ID, String hostName, int port) {
		// TODO Auto-generated method stub
		try {
			this.ID = ID;
			this.hostName = hostName;
			this.userPoint=getUserPoint();
			this.port = port;
			this.totalUploads = 0;
			this.totalDownloads = 0;
			welcomeSocket = new ServerSocket(port);
			this.requestHandler = new RequestHandler(this);

			recordFile = new File("C:\\Users\\ZJZ\\Desktop\\Test\\records\\"
							+ hostName + "-" + port + "-record.txt");

			// initialize the thread pool
			executor = new ThreadPoolExecutor(threadMaxNum, threadBufferMaxNum, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		this.isEnd=false;
		System.out.println("Run MainThread, ID: " + this.ID + ", Port number:" + this.port);
		Thread t = new Thread(new RequestHandler(this));
		t.start();
		readRecords();
		while(!isEnd){

		}

		System.out.println("mainThread ends");
	}
	
	public void startFile(int fileID) {

		JSONObject res=requester.download(fileID, userName);

		if(!res.getBoolean("success")){

			System.out.println("MainThread "+Thread.currentThread().getName()+" fileID "+ fileID
					+" download request fails："+res.getString("message"));

		}else{
			FileThread fileThread = fileThreads.get(fileID);

			System.out.println("\nMainThread "+Thread.currentThread().getName()+" fileID "+ fileID +" start to download");

			try {
				Future<?> future = executor.submit(fileThread);
				currentFutures.put(fileID, future);
				System.out.println("Download pool submit a subThread");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	public void stopFile(int fileID) {
		System.out.println("close file " + fileID);
		Future<?> f = currentFutures.get(fileID);
		if(f!=null) {
			f.cancel(true);
			currentFutures.remove(fileID);
		}
		JSONObject res=requester.exitgroup(fileID);
		System.out.println("remove result: "+ res.getBoolean("success")+", message:"+res.getString("message"));
	}
	
	public void removeTask(int fileID, boolean removeFile) {
		stopFile(fileID);
		FileThread file=fileThreads.remove(fileID);
		
		if(removeFile) {
			file.getFile().delete();
		}
	}
	
	public void exitProgram() {
		System.out.println("start to close all file threads");
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(recordFile);
			fileWriter.write("");
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Iterator<HashMap.Entry<Integer, Future<?>>> entries = currentFutures.entrySet().iterator();
		while (entries.hasNext()) {
			HashMap.Entry<Integer, Future<?>> entry = entries.next();
			System.out.println("remove file " + entry.getKey());
			entry.getValue().cancel(true);
			requester.exitgroup(entry.getKey());
			entries.remove();
		}

		System.out.println("Start to write records");
		Iterator<HashMap.Entry<Integer, FileThread>> fileList = fileThreads.entrySet().iterator();
		while (fileList.hasNext()) {
			HashMap.Entry<Integer, FileThread> file = fileList.next();
			// System.out.println("record file " + file.getKey());
			file.getValue().writeRecord(recordFile.length()<1,!fileList.hasNext());
		}
		System.out.println("close end");
		setIsEnd(true);
	}
	
	public void newFileThread(int fileID, String filePath, long fileSize, ArrayList<String> hashList) {
//		Scanner kb = new Scanner(System.in);
//		int fileID;
//		System.out.println("Please input the file ID:");
//		fileID = kb.nextInt();
//		kb.nextLine();

//		String path = "C:\\Users\\ZJZ\\Desktop\\Test\\";
//		System.out.println("Please input the file name:");
//		String fileName=kb.nextLine();
//		System.out.println("Please input the file size:");
//		long fileSize = kb.nextInt();
//		kb.nextLine();
		
//		ArrayList<String> hashList=Verification.calculateHashList(new File("C:\\Users\\ZJZ\\Desktop\\Test\\testLarge.txt"));

		FileThread fileThread = new FileThread(fileID,filePath, fileSize, hashList, this);
		fileThreads.put(fileID, fileThread);

	}
	
	public void readRecords() {
		try {
			if (!this.recordFile.exists()) {

				System.out.println("Record file does not exist, create record.");
				RandomAccessFile raf = new RandomAccessFile(this.recordFile, "rw");
				System.out.println("raf created");
				raf.setLength(0);
				System.out.println("file length set");
				raf.close();
			} else {
				if (recordFile.length() > 0) {
					System.out.println("start to read record");
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(recordFile));
					while (true) {
						Record record = (Record) ois.readObject();
						System.out.println("Read record:\n" + record.toString());
						FileThread recoverFileThread = new FileThread(this, record);
						fileThreads.put(recoverFileThread.getFileID(), recoverFileThread);
						if (record.isEndRecord()) {
							break;
						}
					}
					ois.close();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUserName(String userName){
		this.userName=userName;
	}

	public void setIsEnd(boolean isEnd){
		this.isEnd=isEnd;
		System.out.println("isEnd is set to:"+isEnd);
	}

	public long getUserPoint() {
		return this.userPoint;
	}

	public void setThreadMaxNum(int num) {
		this.threadMaxNum = num;
		this.threadBufferMaxNum = num + 10;
		this.executor.setMaximumPoolSize(this.threadBufferMaxNum);
		this.executor.setCorePoolSize(this.threadMaxNum);
	}

	public void addTotalUploadsBy(long num) {
		this.totalUploads += num;
	}

	public void addTotalDownloadsBy(long num) {
		this.totalDownloads += num;
	}
	
	public void updatePointsBy(long num) {
		this.userPoint+=num;
	}

	public FileThread getFileThread(int fileID) {
		return fileThreads.get(fileID);
	}

	public ServerSocket getWelcomeSocket() {
		return this.welcomeSocket;
	}

	public int getID() {
		return this.ID;
	}

	public String getHostName() {
		return this.hostName;
	}

	public int getPortNumber() {
		return this.port;
	}

	public long getTotalUploads() {
		return this.totalUploads;
	}

	public long getTotalDownloads() {
		return this.totalDownloads;
	}

	public File getRecordFile() {
		return this.recordFile;
	}
	
	public HashMap<Integer, FileThread> getFileThreadList(){
		return this.fileThreads;
	}

	public ServerRequester getRequester(){
		return this.requester;
	}

	public String getUserName(){
		return this.userName;
	}

	private static String getHostIp(){
		try{
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()){
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()){
					InetAddress ip = (InetAddress) addresses.nextElement();
					if (ip != null
							&& ip instanceof Inet4Address
							&& !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":")==-1){
						System.out.println("local IP = " + ip.getHostAddress());
						return ip.getHostAddress();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
