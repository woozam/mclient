package kr.co.lean.mclient.message;

public class AckMessage extends Message {

	private static final long serialVersionUID = 3041150649475070746L;

	public AckMessage(String protocol) {
		super();
		String[] split = protocol.split(String.valueOf(DELIMITER));
		mHeader = split[1].charAt(0);
		mSubHeader = split[2].charAt(0);
		mMessageId = split[3];
		mData = split[4];
	}

	@Override
	protected String makeMessage() {
		return new StringBuilder().append("A")
				.append(DELIMITER).append(mHeader)
				.append(DELIMITER).append(mSubHeader)
				.append(DELIMITER).append(mMessageId)
				.append(DELIMITER).append(mData)
				.toString();
	}
}