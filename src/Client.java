import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	static String masterHostname = "localhost";
	static int masterPortNo = 5004;

	public static void main(String[] args) throws NotBoundException,
			FileNotFoundException, IOException {
		String fileName = "7amda";
		Registry registryMaster = LocateRegistry.getRegistry(masterHostname,
				masterPortNo);
		MasterServerClientInterface masterHandler = (MasterServerClientInterface) registryMaster
				.lookup("MasterServerClientInterface");
		ReplicaLoc[] rlocs = masterHandler.read(fileName);
		// use replica location........
		System.out.println("Replica location = " + rlocs[0].location);
		// Reading from replica
		Registry registryReplica = LocateRegistry.getRegistry(
				rlocs[0].location, 5005);
		ReplicaServerClientInterface replicaHandler = (ReplicaServerClientInterface) registryReplica
				.lookup("ReplicaServerClientInterface");
		FileContent fileContent = replicaHandler.read(fileName);
		System.out.println(fileContent.toString());
	}
}
