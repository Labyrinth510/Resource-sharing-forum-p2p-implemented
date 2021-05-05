package filetransmission;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class FileSubThread implements Runnable {
	private FileThread parentFileThread;
	private int fileID;
	private File file;
	private boolean isInitial;
	private Socket socket;
	private BitSet chunkList;
	private BitSet counterChunkList;
	private int chunkNum;
	private final static int chunkSize = 1024 * 1024; // 1MB one chunk
	private final static int bufferSize = 1024;
	private ArrayList<String> hashList;
	private RandomAccessFile raf;
	private ObjectOutputStream dos;
	private ObjectInputStream dis;
	private final static String readEnding = "EOC";
	private final static byte[] readEndingByte = readEnding.getBytes();

	public FileSubThread(Socket socket, FileThread parentFileThread) {
		this.socket = socket;
		this.parentFileThread = parentFileThread;
		this.chunkList = parentFileThread.getChunkList();
		this.chunkNum = parentFileThread.getChunkNum();
		this.fileID = parentFileThread.getFileID();
		this.file = parentFileThread.getFile();
		this.hashList = parentFileThread.getHashList();
	}

	@Override
	public void run() {

		System.out.println("\n\nFileSubThread of fileThread " + parentFileThread.getFileID() + " of mainThread "
				+ parentFileThread.getMainThread().getID() + " is running");
		System.out.println("SubThread: " + Thread.currentThread().getName() + " is started");

		// apply the thread pool of fileThread to kill the subThread

		if (isInitial) {
			// PrintWriter output;

			System.out.println("This is the download side");
			try {
				raf = new RandomAccessFile(file, "rw");
				System.out.println("raf created");
				dos = new ObjectOutputStream(socket.getOutputStream());

				// this message is used to request for file
				Message requestFile = new Message(Message.requestFile);
				System.out.println("Message created");
				requestFile.setFileID(fileID);
				System.out.println("file ID  set");
				dos.writeObject(requestFile);
				dos.flush();
				System.out.println("Send Message:\n" + requestFile.toString());

				dis = new ObjectInputStream(socket.getInputStream());
				System.out.println("dis created");
				int i = -1;
				int round = -1;
				while (!Thread.currentThread().isInterrupted() && parentFileThread.getMainThread().getUserPoint() > 0) {

					// if has all counter member's chunk quit
					if (chunkList.equals(counterChunkList))
						break;

					// request for next chunk
					i = (i + 1) % chunkNum;

					// if request for all chunk once, round++
					if (i == 0)
						round++;

					// if round==3, can assume the counter has no available chunk
					if (round == 3)
						break;

					if (!chunkList.get(i) && counterChunkList.get(i)) {

						System.out.println("\nCurrent Thread: " + Thread.currentThread().getName() + "\ni: " + i
								+ ", chunkList:" + chunkList + "\nThe server has lack chunk " + i);
						long offset = ((long) i) * chunkSize;
//						output.println(offset);

						// message to request for offset of file
						Message requestChunk = new Message(Message.requestChunk);
						requestChunk.setOffset(offset);
						dos.writeObject(requestChunk);
						dos.flush();
						System.out.println("Send Message:\n" + requestChunk.toString() + "\n");

						// receive the file & check whether correct MD5
						receiveFile(offset);
//						System.out.println("check chunk "+i);
//						System.out.println("receive MD5:"+Verification.calculateChunkMD5(file, offset));
//						System.out.println("correct MD5:"+hashList.get(i));
						if (Verification.calculateChunkMD5(file, offset).equals(hashList.get(i))) {
							chunkList.set(i);
							parentFileThread.downloadsAddBy(1);
							parentFileThread.getMainThread().addTotalDownloadsBy(1);
							System.out.println("SubThread" + Thread.currentThread().getName() + " chunk " + i
									+ " is correctly downloaded\n");

							Message responseReceivedChunk = new Message(Message.responseReceiveChunk);
							responseReceivedChunk.setReceiveChunkCorrectness(true, true);
							dos.writeObject(responseReceivedChunk);
							dos.flush();
						} else {
							System.out.println("SubThread" + Thread.currentThread().getName() + " chunk " + i
									+ " is not correctly downloaded\n");
							Message responseReceivedChunk = new Message(Message.responseReceiveChunk);
							responseReceivedChunk.setReceiveChunkCorrectness(true, false);
							dos.writeObject(responseReceivedChunk);
							dos.flush();
						}

					}
				}

				// message to terminate this subThread
				Message termination = new Message(Message.termination);
				dos.writeObject(termination);
				dos.flush();
				System.out.println("Send Message:\n" + termination.toString());
//				output.println("$#Terminated#$");
				dos.close();
				dis.close();
				raf.close();
				socket.close();
				System.out.println("subThread" + Thread.currentThread().getName()
						+ " no more chunk or cancelled, download FileSubThread ends");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.out.println("This is the upload side");
			try {
				raf = new RandomAccessFile(file, "r");
				dos = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("dos created");

				while (!Thread.currentThread().isInterrupted()) {
					try {
						Message input = (Message) dis.readObject();
						System.out.println("Receive Message:\n" + input.toString() + "\n");
						if (input.getType() == Message.termination)
							break;
						if (input.isValid() && input.getType() == Message.requestChunk) {
							long offset = input.getOffset();
							sendFile(offset);

							Message chunkResponse = (Message) dis.readObject();
							System.out.println("Receive Message:\n" + input.toString() + "\n");
							if (chunkResponse.getType() == Message.responseReceiveChunk
									&& chunkResponse.isCorrectChunk()) {
								parentFileThread.uploadsAddBy(1);
								parentFileThread.getMainThread().addTotalUploadsBy(1);
							}
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				raf.close();
				dos.close();
				dis.close();
				socket.close();
				System.out.println("finish upload, upload FileSubThread ends");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void sendFile(long offset) {

		System.out.println("start to send offset " + offset);
		// while (true) {
		try {
			byte[] buffer = new byte[bufferSize];
			int totalRead = 0, readLen;
			raf.seek(offset);
			boolean endChunk = false;
			while (true) {
				readLen = raf.read(buffer, 0, bufferSize);
				totalRead += readLen;
				// System.out.println("chunkSize: "+chunkSize+" total read:"+totalRead);
				Message sendMessage = new Message(Message.sendPiece);
				sendMessage.setSendPiece(true, buffer, readLen, false);
				dos.writeObject(sendMessage);
				dos.flush();
				// System.out.println("Send Message:\n" + sendMessage.toString());

				if (totalRead == chunkSize || readLen < bufferSize)
					break;

//				if (chunkSize - totalRead <= bufferSize) {
//					readLen = raf.read(buffer, 0, chunkSize - totalRead);
//					endChunk = true;
//				} else {
//					readLen = raf.read(buffer, 0, bufferSize);
//				}
//
//			
//
//				if (endChunk) {
//					System.out.println("end of chunk");
//					break;
//				}
			}

			Message sendChunkEnd = new Message(Message.sendPiece);
			sendChunkEnd.setSendPiece(true, null, 0, true);
			dos.writeObject(sendChunkEnd);
			dos.flush();
			System.out.println("Send Message:\n" + sendChunkEnd.toString());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// break;
		// }
	}

	public void receiveFile(long offset) {
		try {

			System.out.println("start to receive offset " + offset);
			raf.seek(offset);
			while (true) {
				try {
					Message receiveMessage = (Message) dis.readObject();
					//System.out.println("SubThread: " + Thread.currentThread().getName() + "Receive Message:\n"+receiveMessage.toString());
					if (receiveMessage.isValid() && receiveMessage.getType() == Message.sendPiece) {
						if (receiveMessage.getIsEndChunk()) {
							break;
						} else {
							raf.write(receiveMessage.getUploads(), 0, receiveMessage.getUploadLen());
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean readEnd(byte[] buffer, int readLen) {
		if (readLen < readEndingByte.length)
			return false;

		for (int i = 0; i < readEndingByte.length; i++) {
			if (buffer[readLen - 1 - i] != readEndingByte[readEndingByte.length - 1 - i])
				return false;
		}
		return true;
	}

	public void setIsInitial(boolean value) {
		this.isInitial = value;
	}

	public void setCounterChunkList(BitSet counterChunkList) {
		this.counterChunkList = counterChunkList;
	}

	public void setInputStream(ObjectInputStream dis) {
		this.dis = dis;
		System.out.println("dis is set");
	}
}
