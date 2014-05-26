import java.io.Serializable;

public class ReplicaLoc implements Serializable {

	private static final long serialVersionUID = 1545442265027173644L;
	String location;
	int replicaNum;

	public ReplicaLoc(String location, int rNum) {
		this.location = location;
		this.replicaNum = rNum;
	}
}
