import java.io.Serializable;

public class ReplicaLoc implements Serializable {

	private static final long serialVersionUID = 1545442265027173644L;
	String location;
	int replicaPort;
	int replicaNum;

	public ReplicaLoc(String replicaLoc, int replicaPort, int rNum) {
		this.location = replicaLoc;
		this.replicaPort = replicaPort;
		this.replicaNum = rNum;
	}

	@Override
	public String toString() {
		return "ReplicaLoc [location=" + location + ", replicaPort="
				+ replicaPort + ", replicaNum=" + replicaNum + "]";
	}
}
