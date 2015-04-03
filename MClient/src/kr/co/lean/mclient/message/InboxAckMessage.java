package kr.co.lean.mclient.message;

public class InboxAckMessage extends DefaultMessage {
	
	private static final long serialVersionUID = -357805513732084443L;

	public InboxAckMessage() {
		super();
	}

	@Override
	protected String makeMessage() {
		return "A:Z";
	}
}