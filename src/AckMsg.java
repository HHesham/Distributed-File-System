import java.io.Serializable;

public class AckMsg implements Serializable {
	private static final long serialVersionUID = 6954352558662638184L;
	private long txnID; // transaction ID
	private long msgSeqNum; // chunk message sequence number

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
		return "AckMsg [txnID=" + txnID + ", msgSeqNum=" + msgSeqNum + "]";
	}
}
