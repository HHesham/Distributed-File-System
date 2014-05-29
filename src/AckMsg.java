import java.io.Serializable;

public class AckMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6954352558662638184L;
	private long txnID;
	private long msgSeqNum;

	public AckMsg(long tId, long msgSeq) {
		this.txnID = tId;
		this.msgSeqNum = msgSeq;
	}

	public long getTxnID() {
		return txnID;
	}

	public long getMsgSeqNum() {
		return msgSeqNum;
	}

	@Override
	public String toString() {
		return txnID + " " + msgSeqNum;
	}
}
