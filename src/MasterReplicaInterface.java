import java.rmi.Remote;

public interface MasterReplicaInterface extends Remote {
	public void createFile(String fileName);
}
