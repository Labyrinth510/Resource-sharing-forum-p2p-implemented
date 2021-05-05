package filetransmission;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileThread implements Runnable/* , ConnectionListener */ {

	private MainThread mainThread;

	// 0 means wait
	// 1 means run
	// 2 means stop
	// 3 means finish
	private int stage;

	private ArrayList<FileSubThread> subThreads = new ArrayList<FileSubThread>();
	private ArrayList<Future<?>> currentFutures = new ArrayList<Future<?>>();
	private ThreadPoolExecutor downloadExecutor; // thread pool of the download
	private ThreadPoolExecutor uploadExecutor; // thread pool the upload

	// private FileHandler fileHandler;
	private int fileID;
	private File filePath;
	private long fileSize;
	private int chunkNum;
	private final int chunkSize = 1024 * 1024; // 1MB

	// list of group member, each Member object has IP, port number, its chunkList
	private ArrayList<Member> groupList = new ArrayList<>();

	// chunklist of this file in this client, 1 means has, 0 means not
	private volatile BitSet chunkList = new BitSet();
	private final ArrayList<String> hashList;

	private int downloadMaxNum = 15; // can download with at most 15 neighbors at the same time
	private int downloadMaxBufferNum = 20; // at most keep 20 connection in download,15 downloading 5 waits
	private int uploadMaxNum = 15; // can download with at most 15 neighbors at the same time
	private int uploadMaxBufferNum = 20; // at most keep 20 connection in download,15 downloading 5 waits
	private final long subThreadTimeLapse = 1 * 60 * 1000; // 1 minute
	private final long updateTimeLapse = 1 * 60 * 1000; // 1 minute

	private long uploads;
	private long downloads;

	private Scanner kb = new Scanner(System.in);

	public FileThread(int fileID, String filePath, long fileSize, ArrayList<String> hashList, MainThread mainThread) {
		this.stage = 0;
		this.fileID = fileID;
		this.mainThread = mainThread;
		this.filePath = new File(filePath);
		this.fileSize = fileSize;
		this.hashList = hashList;
		this.chunkNum = (int) Math.ceil(1.0 * fileSize / chunkSize);
		this.uploads = 0;
		this.downloads = 0;

		// if does not have file, allocate space
		// initialize chunklist, true empty, false full
		if (!this.filePath.exists()) {
			try {
				System.out.println("File does not exist, create buffer.");
				RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw");
				raf.setLength(fileSize);
				raf.close();
				setChunkList(-1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
//			System.out.println("File Exists.");
//			System.out.println("Please input the chunk list type:");
//			setChunkList(kb.nextInt());
//			kb.nextLine();
			setChunkList(0);
		}
		System.out.println("FileThread created, belong to mainThread " + mainThread.getID() + ", fileID:" + fileID
				+ ", fileName:" + this.filePath);
		System.out.println("ChunkList:" + this.chunkList);

		// initialize the thread pool
		downloadExecutor = new ThreadPoolExecutor(downloadMaxNum, downloadMaxBufferNum, subThreadTimeLapse,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		downloadExecutor.allowCoreThreadTimeOut(true);

		uploadExecutor = new ThreadPoolExecutor(uploadMaxNum, uploadMaxBufferNum, subThreadTimeLapse,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		uploadExecutor.allowCoreThreadTimeOut(true);

		System.out.println("Thread pools initialization finished");
	}

	public FileThread(MainThread mainThread, Record record) {
		this.stage = 0;
		this.fileID = record.getFileID();
		this.mainThread = mainThread;
		this.filePath = record.getFilePath();
		this.fileSize = record.getFileSize();
		this.chunkNum = record.getChunkNum();
		this.chunkList = record.getChunkList();
		this.hashList = record.getHashList();

		downloadExecutor = new ThreadPoolExecutor(downloadMaxNum, downloadMaxBufferNum, subThreadTimeLapse,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		downloadExecutor.allowCoreThreadTimeOut(true);

		uploadExecutor = new ThreadPoolExecutor(uploadMaxNum, uploadMaxBufferNum, subThreadTimeLapse,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		uploadExecutor.allowCoreThreadTimeOut(true);

		System.out.println("Thread pools initialized by record finished");
	}

	@Override
	public void run() {

		this.stage = 1;
		System.out.println("Thread: "+Thread.currentThread().getName()+" start file ID: "+fileID);
		// use the thread pool of main thread to kill file thread
		long round = 0;
		try {
			while (!Thread.currentThread().isInterrupted()) {

				// has all chunks, finishes
//				if (chunkList.cardinality() == chunkNum) {
//					downloadExecutor.shutdown();
//					break;
//				}

				System.out.println("\n\n\n\nFile Thread round: " + (round++) + " is started");
				// long start = System.currentTimeMillis();

				// every 5 minutes update the grouplist and chunk list of each member
				// request for lack chunk in a sequence

				System.out.println("FileThread "+Thread.currentThread().getName()+" fileID "+ fileID+" start to update grouplist");
				//updateGroupList();
				JSONObject res=mainThread.getRequester().getgrouplist(fileID);
				System.out.println(res);
				if(!res.getBoolean("success")){

					System.out.println("FileThread "+Thread.currentThread().getName()+" fileID "+ fileID
							+" get grouplist fails："+res.getString("message"));

				}else{

					System.out.println("FileThread "+Thread.currentThread().getName()+" fileID "+ fileID+" receive group list");
					// read JSONArray to arraylist
					ArrayList<String> ipList=new ArrayList<String>();
					ipList=(ArrayList<String>)res.getJSONObject("data").getJSONArray("groupList").toJavaList(String.class);
					System.out.println("FileThread "+Thread.currentThread().getName()+" fileID "+ fileID+" group list -> arraylist");

					// request each group member to update their chunk list
					for(int i=0;i<ipList.size();i++){
						
						int port=8001;

						Member m=new Member(ipList.get(i),port);
						if (m.getHostName().equals(mainThread.getHostName())
								&& m.getPortNumber() == mainThread.getPortNumber()) {
							System.out.println(m.getHostName()+" is the host ip, skip");
							continue;
						}
						groupList.add(m);
						System.out.println(
								"Request chunklist for host:" + m.getHostName() + ", port number:" + m.getPortNumber());
						BitSet receiveChunkList = requestChunkList(m);
						System.out.println("Receive chunklist of port " + m.getPortNumber() + ":\n" + receiveChunkList);
						m.setChunkList(receiveChunkList);
					}

					System.out.println("FileThread "+Thread.currentThread().getName()+" fileID "+ fileID+" update chunklist finished");

					// request each group member to update their chunk list
//				for (int i = 0; i < groupList.size(); i++) {
//					Member m = groupList.get(i);
//					if (m.getHostName().equals(mainThread.getHostName())
//							&& m.getPortNumber() == mainThread.getPortNumber()) {
//						groupList.remove(i);
//						i--;
//						continue;
//					}
//					System.out.println(
//							"Request chunklist for host:" + m.getHostName() + ", port number:" + m.getPortNumber());
//					BitSet receiveChunkList = requestChunkList(m);
//					System.out.println("Receive chunklist of port " + m.getPortNumber() + ":\n" + receiveChunkList);
//					m.setChunkList(receiveChunkList);
//				}

					// sort the group list from fewest chunks to most chunks
					Collections.sort(groupList);

					int nextMember = -1;

					// while this fileThread does not have all chunks
					while (chunkList.cardinality() != chunkNum && nextMember < groupList.size() - 1) {
						System.out.println("Total Chunks:" + chunkNum + ", has chunks:" + chunkList.cardinality());

						// find the member that has chunk that this client does not have
						BitSet cloneB = (BitSet) chunkList.clone();
						do {
							nextMember++;
							cloneB.or(groupList.get(nextMember).getChunkList());
						} while (cloneB.equals(chunkList) && nextMember < groupList.size() - 1);

						Member m = groupList.get(nextMember);
						System.out.println("Member host:" + m.getHostName() + ", port number:" + m.getPortNumber()
								+ " has lack chunk ");
						requestDownload(m, m.getChunkList());

					}
				}

				System.out.println("Start sleep until update time lapse");
				Thread.sleep(updateTimeLapse);

				// terminate all subThreads
				for (Iterator<Future<?>> it = currentFutures.iterator(); it.hasNext();) {
					System.out.println("Cancel subThread");
					it.next().cancel(true);
					it.remove();
				}
				System.out.println("Sleep ends");

				mainThread.getRequester().update(mainThread.getUserName(),"update-status",mainThread.getUserPoint());
			}
//			System.out.println("File Thread:" + Thread.currentThread().getName() + " start to close");
//			close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
			System.out.println("File Thread: " + Thread.currentThread().getName() + " interrupted when sleeps");			
		}finally {
			System.out.println("File Thread:" + Thread.currentThread().getName() + " start to close");
			close();
		}
	}

	public void upload(Socket socket, ObjectInputStream dis) {
		FileSubThread newSubThread = new FileSubThread(socket, this);
		newSubThread.setIsInitial(false);
		newSubThread.setInputStream(dis);
		Future<?> future = uploadExecutor.submit(newSubThread);
		currentFutures.add(future);
		System.out.println("Upload pool executes a subThread");
	}

	public void uploadChunkList(Socket socket) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			Message sendChunkList = new Message(Message.responseChunkList);
			sendChunkList.setChunkList(true, chunkList);
			out.writeObject(sendChunkList);
			out.flush();
			System.out.println("Upload chunklist success");
			out.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public BitSet requestChunkList(Member m) {

		try {
			Socket socket = new Socket(m.getHostName(), m.getPortNumber());
			// PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
//			output.println("chunkList");
//			output.println(fileID);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			Message requestChunkList = new Message(Message.requestChunkList);
			requestChunkList.setFileID(fileID);
			output.writeObject(requestChunkList);
			output.flush();
//			output.close();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Message receiveMessage = (Message) in.readObject();
			if (receiveMessage.isValid() && receiveMessage.getType() == Message.responseChunkList)
				return receiveMessage.getChunkList();
			return null;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void requestDownload(Member m, BitSet counterChunkList) {

		try {
			Socket socket = new Socket(m.getHostName(), m.getPortNumber());
			FileSubThread newSubThread = new FileSubThread(socket, this);
			newSubThread.setIsInitial(true);
			newSubThread.setCounterChunkList(counterChunkList);
			Future<?> future = downloadExecutor.submit(newSubThread);
			currentFutures.add(future);
			System.out.println("Download pool submit a subThread");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (this.chunkList.cardinality() == this.chunkNum)
			this.stage = 3;
		else
			this.stage = 2;

		for (Iterator<Future<?>> it = currentFutures.iterator(); it.hasNext();) {
			System.out.println("Cancel subThread");
			it.next().cancel(true);
			it.remove();
		}
		mainThread.stopFile(fileID);
	}

	public void writeRecord(boolean isFirst, boolean isLast) {
		Record record = new Record(this, isLast);
		System.out.println("File Thread:" + Thread.currentThread().getName() + " made record:\n" + record.toString());
		try {
			ObjectOutputStream oos;
			if (isFirst) {
				oos = new ObjectOutputStream(new FileOutputStream(mainThread.getRecordFile(), true));
			} else {
				oos = new MyObjectOutputStream(new FileOutputStream(mainThread.getRecordFile(), true));
			}
			oos.writeObject(record);
			oos.flush();
			oos.close();
			System.out.println("Record of file " + record.getFileID() + " ends");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addMember(String hostname, int portNumber) {
		Member m = new Member(hostname, portNumber);
		this.groupList.add(m);

		System.out.println("current group list:");
		for (int i = 0; i < groupList.size(); i++) {
			Member member = groupList.get(i);
			System.out.println("ip:" + member.getHostName() + ", portNumber:" + member.getPortNumber());
		}
	}

	public void setChunkList(int type) {
		// type -1 has no chunk
		// type 0 has all chunk
		// type 1 has 0,4,8,12... chunks
		// type 2 has 1,5,9,13... chunks
		// type 3 has 2,6,10,14... chunks
		// type 4 has 3,7,11,15... chunks

		BitSet b = new BitSet(chunkNum);

		switch (type) {
		case -1:
			this.chunkList = b;
			break;
		case 0:

			b.set(0, chunkNum);
			this.chunkList = b;
			break;
		default:
			for (int i = type - 1; i <= chunkNum; i += 2) {
				b.set(i);
			}
			this.chunkList = b;
		}

	}

	public BitSet getChunkList() {
		return this.chunkList;
	}

	public int getChunkNum() {
		return this.chunkNum;
	}

	public int getFileID() {
		return this.fileID;
	}

	public File getFile() {
		return this.filePath;
	}

	public MainThread getMainThread() {
		return this.mainThread;
	}

	public long getUploads() {
		return this.uploads;
	}

	public long getDownloads() {
		return this.downloads;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public void uploadsAddBy(long num) {
		this.uploads += num;
		mainThread.addTotalUploadsBy(num);
		mainThread.updatePointsBy(num);
	}

	public String getStage() {
		if (stage == 0)
			return "wait";
		else if (stage == 1)
			return "running";
		else if (stage == 2)
			return "stopped";
		else if (stage == 3)
			return "finished";
		else
			return "error";
	}

	public ArrayList<String> getHashList() {
		return this.hashList;
	}

	public void downloadsAddBy(long num) {
		this.downloads += num;
		mainThread.addTotalDownloadsBy(num);
		mainThread.updatePointsBy(-num);
	}

}

class Member implements Comparable<Member> {
	private String hostname;
	private int portNumber;
	private BitSet chunkList;

	public Member(String hostname, int portNumber) {
		this.hostname = hostname;
		this.portNumber = portNumber;
	}

	public Member(String hostname, int portNumber, BitSet chunkList) {
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.chunkList = chunkList;
	}

	@Override
	public int compareTo(Member m) {
		return this.chunkList.cardinality() - m.getChunkList().cardinality();
	}

	public void setChunkList(BitSet c) {
		this.chunkList = c;
	}

	public String getHostName() {
		return this.hostname;
	}

	public int getPortNumber() {
		return this.portNumber;
	}

	public BitSet getChunkList() {
		return this.chunkList;
	}
}

class MyObjectOutputStream extends ObjectOutputStream {
	public MyObjectOutputStream() throws IOException {
		super();
	}

	public MyObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override

	protected void writeStreamHeader() throws IOException {
		return;
	}
}
