package kr.co.lean.mclient;

public class SeqIdGenerator {

	private static long sInitialTime;
	private static int sSequence = 0;
	
	public static synchronized String generate() {
		if (sSequence == 0) {
			sInitialTime = System.currentTimeMillis() / 1000;
		}
		String seqId = String.format("%d+%04d", sInitialTime, sSequence);
		sSequence++;
		if (sSequence >= 10000) {
			sSequence = 0;
		}
		return seqId;
	}
}