package kr.co.lean.mclient.message;

public class InboxMessage extends Message {

	private static final long serialVersionUID = 7987098956889444932L;
	
	private int mCount;
	
	public InboxMessage(String message) {
		String[] split = message.split(String.valueOf(DELIMITER));
		mCount = Integer.parseInt(split[1]);
	}
	
	public int getCount() {
		return mCount;
	}

	@Override
	protected String makeMessage() {
		return String.format("Z:%d", mCount);
	}
}