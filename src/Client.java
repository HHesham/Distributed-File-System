import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	public void write() {

	}

	public static void main(String[] args) throws NotBoundException,
			FileNotFoundException, IOException {

		Configurator config = Configurator.getInstance();

		String fileName = "7amda";
		// Get Registry Server
		Registry registryMaster = LocateRegistry.getRegistry(
				config.getMasterHostname(), config.getMasterPort());
		MasterServerClientInterface masterHandler = (MasterServerClientInterface) registryMaster
				.lookup("MasterServerClientInterface");

		// READ FILE:
		// Get primary replica from Master server
		ReplicaLoc[] rlocs = masterHandler.read(fileName);

		String replicaLoc = rlocs[0].location;
		int replicaPort = rlocs[0].replicaPort;

		// Reading from replica
		Registry registryReplica1 = LocateRegistry.getRegistry(replicaLoc,
				replicaPort);
		ReplicaServerClientInterface replicaHandler = (ReplicaServerClientInterface) registryReplica1
				.lookup("ReplicaServerClientInterface");
		FileContent fileContent = replicaHandler.read(fileName);
		System.out.println(fileContent.toString());

		// --------------------------------------------------------------
		// WRITE FILE:
		FileContent fc = new FileContent(fileName, "Bezo Mezo");
		WriteMsg wMsg = masterHandler.write(fc);

		System.out.println(wMsg.toString());

		String writeReplicaLoc = wMsg.getLoc().location;
		int writeReplicaPort = wMsg.getLoc().replicaPort;

		// Writing in replica
		Registry registryReplica2 = LocateRegistry.getRegistry(writeReplicaLoc,
				writeReplicaPort);
		replicaHandler = (ReplicaServerClientInterface) registryReplica2
				.lookup("ReplicaServerClientInterface");
		WriteMsg msg = replicaHandler.write(wMsg.getTransactionId(),
				wMsg.getTimeStamp(), fc);
		System.out.println(msg.toString());
	}
}
