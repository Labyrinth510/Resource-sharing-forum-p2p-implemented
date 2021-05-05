package filetransmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class RequestHandler implements Runnable{
	
	private MainThread mainThread;
	private ServerSocket welcomeSocket;
	
	public RequestHandler(MainThread mainThread) {
		this.mainThread=mainThread;
		this.welcomeSocket=mainThread.getWelcomeSocket();
	}
	
	@Override
	public void run() {
		System.out.println("Run Request Handler of MainThread "+mainThread.getID());
		while(true) {
			try {
				Socket acceptSocket=welcomeSocket.accept();
				System.out.println("\n\nconnection is set up");
				
				ObjectInputStream  input = new ObjectInputStream (acceptSocket.getInputStream());
				Message m=(Message)input.readObject();
				System.out.println("Receive Message:\n"+m.toString());
				if(m.isValid()) {
					
					if(m.getType()==Message.requestChunkList) {
						int fileID=m.getFileID();
						mainThread.getFileThread(fileID).uploadChunkList(acceptSocket);
					}else if(m.getType()==Message.requestFile) {
						int fileID=m.getFileID();
						mainThread.getFileThread(fileID).upload(acceptSocket, input);
					}
				}
			}catch(IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
