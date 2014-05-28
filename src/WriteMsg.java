import java.io.Serializable;

public class WriteMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5583287534509576844L;
	private long transactionId;
	private long timeStamp;
	private ReplicaLoc loc;

	public long getTransactionId() {
		return transactionId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public ReplicaLoc getLoc() {
		return loc;
	}

	public WriteMsg(long transID, long tStamp, ReplicaLoc l) {
		this.transactionId = transID;
		this.timeStamp = tStamp;
		this.loc = l;
	}

	@Override
	public String toString() {
		return "Write_Msg [ " + transactionId + " " + timeStamp + " "
				+ loc.toString() + "]";
	}
}
