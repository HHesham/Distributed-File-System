public class WriteMsg {
	private long transactionId;
	private long timeStamp;
	private ReplicaLoc loc;

	public WriteMsg(long transID, long tStamp, ReplicaLoc l) {
		this.transactionId = transID;
		this.timeStamp = tStamp;
		this.loc = l;
	}
}
