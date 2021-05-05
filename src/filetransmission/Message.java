package filetransmission;

import java.util.BitSet;

public class Message implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2363878671740490539L;

	private static long ID = 0;
	private long messageID;

	// One upload should not be larger than 1024
	private static final int uploadSize = 1024;

	// denote whether the message is valid
	private boolean isValid;

	// 0 means terminate the connection
	public static final byte termination = 0;

	// -1 request for chunklist of file
	public static final byte requestChunkList = -1;
	// -2 request for download file
	public static final byte requestFile = -2;
	// -3 request for a chunk of file
	public static final byte requestChunk = -3;

	// 1 response chunklist of file
	public static final byte responseChunkList = 1;
	// 2 response accept request for a file
	public static final byte responseFile = 2;
	// 3 response the requested chunk of file
	public static final byte responseChunk = 3;
	// 4 upload a piece of chunk to the requester
	public static final byte sendPiece = 4;
	// 5 response to the received chunk, whether the received chunk correct
	public static final byte responseReceiveChunk = 5;
	// type to denote the type of message
	private byte type;

	// the ID of file that request for
	// use in request message
	private int fileID;

	// the offset of the request chunk
	private long offset;

	// All response expect sendPiece has a boolean
	// to denote whether accept request
	private boolean response;

	// when this is a response message for chunklist
	// this message has a BitSet
	private BitSet chunkList;

	// when the received chunk is correct
	private boolean isCorrectChunk;

	// denote whether this uploads is the last piece of chunk
	private boolean isEndChunk;
	// when this sends the piece that has read
	// this uploads contains the uploads to be sent
	private byte[] uploads = new byte[uploadSize];
	// uploadLen is the length of uploads
	private int uploadLen;

	// Basic constructor
	public Message(byte type) {
		this.type = type;
		this.isValid = true;
		ID++;
		this.messageID = ID;
	}

	// request message, set the request fileID
	// if not request message, not valid message
	public void setFileID(int fileID) {
		if (this.type < 0) {
			this.fileID = fileID;
		} else {
			this.isValid = false;
		}
	}

	// request chunk message, set the chunk offset
	// if not request chunk message, not valid
	public void setOffset(long offset) {
		if (type == requestChunk) {
			this.offset = offset;
		} else {
			this.isValid = false;
		}
	}

	// when response message, whether accept request
	// not response message, invalid
	private void setResponse(boolean ans) {
		if (type > 0) {
			this.response = ans;
		} else {
			this.isValid = false;
		}
	}

	// response chunkList message, if accept request, set chunkList
	// if not response chunkList message, not valid message
	public void setChunkList(boolean ans, BitSet chunkList) {
		if (type == responseChunkList) {
			setResponse(ans);
			if (ans)
				this.chunkList = chunkList;
		} else {
			this.isValid = false;
		}
	}

	// response file message, denote whether accept request for file
	// not response file message, invalid message
	public void setRespnseFile(boolean ans) {
		if (type == responseFile)
			setResponse(ans);
		else {
			this.isValid = false;
		}
	}

	// response chunk message, denote whether accept request for a chunk of file
	// not response chunk message, invalid message
	public void setResponseChunk(boolean ans) {
		if (type == responseChunk)
			setResponse(ans);
		else {
			this.isValid = false;
		}
	}

	// sendPiece message, if can send piece, set uploads and whether is end of chunk
	// not response chunk message, invalid message
	public void setSendPiece(boolean ans, byte[] uploads, int uploadLen, boolean isEndChunk) {
		if (type == sendPiece) {
			setResponse(ans);
			if (ans) {
				if (uploadLen > Message.uploadSize)
					this.isValid = false;
				else {
					if (isEndChunk) {
						this.uploadLen = 0;
						this.isEndChunk = isEndChunk;
					} else {
						System.arraycopy(uploads, 0, this.uploads, 0, uploadLen);
						this.uploadLen = uploadLen;
						this.isEndChunk = isEndChunk;
					}
				}
			}
		} else {
			this.isValid = false;
		}
	}

	public void setReceiveChunkCorrectness(boolean ans, boolean isCorrect) {
		if (this.type == Message.responseReceiveChunk) {
			setResponse(ans);
			if (ans) {
				this.isValid = true;
				this.isCorrectChunk = isCorrect;
			}
		} else {
			this.isValid = false;
		}

	}

	public String toString() {
		String s = "Message ID:" + this.messageID + " IsValid:" + this.isValid;
		if (this.isValid) {
			s += ", Type:" + this.type;
			switch (this.type) {
			case Message.termination:
				s += " Termination";
				break;
			case Message.requestChunkList:
				s += " Request chunkList for file " + this.fileID;
				break;
			case Message.requestFile:
				s += " Request to download file " + this.fileID;
				break;
			case Message.requestChunk:
				s += " Request file " + this.fileID + " offset " + this.offset;
				break;
			case Message.responseChunkList:
				if (!this.response)
					s += " Reject the request";
				else
					s += " Upload chunklist: " + this.chunkList;
				break;
			case Message.responseFile:
				if (!this.response)
					s += " Reject the request";
				else
					s += " Accept upload file: " + this.fileID;
				break;
			case Message.responseChunk:
				if (!this.response)
					s += " Reject the request";
				else
					s += " Accept upload file" + this.fileID + " offset: " + this.offset;
				break;
			case Message.sendPiece:
				if (!this.response)
					s += " Reject the request";
				else {
					s += " Upload file " + this.fileID + " offset: " + this.offset;
					if (this.isEndChunk)
						s += " Is end of chunk: " + this.isEndChunk;
					else {
						s += " upload len: " + this.uploadLen;
						// s+="\nUpload:"+(new String(this.uploads));
					}
				}
				break;
			case Message.responseReceiveChunk:
				if (!this.response)
					s += " Error in receiving the chunk";
				else
					s += " The received chunk is " + this.isCorrectChunk + " offset: " + this.offset;
				break;
			default:
				s += " Message type error";
			}
		}
		return s;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public boolean isCorrectChunk() {
		return this.isCorrectChunk;
	}

	public byte getType() {
		return this.type;
	}

	public int getFileID() {
		return this.fileID;
	}

	public long getOffset() {
		return this.offset;
	}

	public BitSet getChunkList() {
		return this.chunkList;
	}

	public boolean getResponseFile() {
		return this.response;
	}

	public boolean getIsEndChunk() {
		return this.isEndChunk;
	}

	public int getUploadLen() {
		return this.uploadLen;
	}

	public byte[] getUploads() {
		return this.uploads;
	}

	public long getId() {
		return this.messageID;
	}
}
