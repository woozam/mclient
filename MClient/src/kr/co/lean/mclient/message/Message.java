package kr.co.lean.mclient.message;

import java.io.Serializable;

public abstract class Message implements Serializable {

	public static final char DELIMITER = ':';
	
	private static final long serialVersionUID = -4636777194467320171L;

	protected String mData;
	protected String mMessageId;
	protected char mHeader;
	protected char mSubHeader;
	
	public Message() {
	}
	
	public Message(char header, char subHeader, String messageId) {
		mHeader = header;
		mSubHeader = subHeader;
		mMessageId = messageId;
	}
	
	public void setData(String data) {
		mData = data;
	}
	
	public String getData() {
		return mData;
	}
	
	public String getMessageId() {
		return mMessageId;
	}
	
	protected abstract String makeMessage();
	
	@Override
	public String toString() {
		return makeMessage();
	}
	
	public String getProtocol() {
		return makeMessage();
	}

	public char getHeader() {
		return mHeader;
	}

	public void setHeader(char header) {
		this.mHeader = header;
	}

	public char getSubHeader() {
		return mSubHeader;
	}

	public void setSubHeader(char subHeader) {
		this.mSubHeader = subHeader;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (o instanceof String) {
			if (getMessageId().equals(o)) {
				return true;
			} else {
				return false;
			}
		} else if (this.getClass().getSimpleName().equals(o.getClass().getSimpleName())) {
			if (((Message) o).getMessageId().equals(getMessageId())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}