package kr.co.lean.mclient.message;

import java.io.Serializable;

import kr.co.lean.mclient.SeqIdGenerator;

public abstract class DefaultMessage extends Message implements Serializable {

	private static final long serialVersionUID = -4466061496762042798L;
	public static String deviceinfo = "";
	public static String uid = "";
	
	protected DefaultMessage() {
		super();
	}

	protected DefaultMessage(char header, char subHeader) {
		super(header, subHeader, SeqIdGenerator.generate());
	}

	@Override
	protected String makeMessage() {
		return new StringBuilder().append(mHeader)
				.append(DELIMITER).append(mSubHeader)
				.append(DELIMITER).append(uid)
				.append(DELIMITER).append(deviceinfo)
				.append(DELIMITER).append(mMessageId)
				.append(DELIMITER).append(mData)
				.toString();
	}
}