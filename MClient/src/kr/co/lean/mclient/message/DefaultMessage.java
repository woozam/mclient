package kr.co.lean.mclient.message;

import java.io.Serializable;

import kr.co.lean.mclient.MClientManager;
import kr.co.lean.mclient.SeqIdGenerator;

public abstract class DefaultMessage extends Message implements Serializable {

	private static final long serialVersionUID = -4466061496762042798L;

	protected DefaultMessage(char header, char subHeader) {
		super(header, subHeader, SeqIdGenerator.generate());
	}

	@Override
	protected String makeMessage() {
		String uid = MClientManager.uid;
		String device_info = MClientManager.deviceinfo;
		return new StringBuilder().append(mHeader)
				.append(DELIMITER).append(mSubHeader)
				.append(DELIMITER).append(uid)
				.append(DELIMITER).append(device_info)
				.append(DELIMITER).append(mMessageId)
				.append(DELIMITER).append(mData)
				.toString();
	}
}