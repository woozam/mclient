package kr.co.lean.mclient.message;

public class KeepAliveMessage extends DefaultMessage {
	
	private static final long serialVersionUID = 7398992397189447841L;

	public KeepAliveMessage() {
		super('K', 'S');
	}
}