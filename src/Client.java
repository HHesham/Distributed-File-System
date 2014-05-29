import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	public static final int msgSize = 2; // in characters

	public static void main(String[] args) throws NotBoundException,
			FileNotFoundException, IOException, MessageNotFoundException {

		Configurator config = Configurator.getInstance();

		String fileName = "hhhh";
		// Get Registry Server
		Registry registryMaster = LocateRegistry.getRegistry(
				config.getMasterHostname(), config.getMasterPort());
		MasterServerClientInterface masterHandler = (MasterServerClientInterface) registryMaster
				.lookup("MasterServerClientInterface");

		// // READ FILE:
		// // Get primary replica from Master server
		// ReplicaLoc[] rlocs = masterHandler.read(fileName);
		//
		// String replicaLoc = rlocs[0].location;
		// int replicaPort = rlocs[0].replicaPort;
		//
		// // Reading from replica
		// Registry registryReplica1 = LocateRegistry.getRegistry(replicaLoc,
		// replicaPort);
		// ReplicaServerClientInterface replicaHandler =
		// (ReplicaServerClientInterface) registryReplica1
		// .lookup("ReplicaServerClientInterface");
		// FileContent fileContent = replicaHandler.read(fileName);
		// System.out.println(fileContent.toString());

		// --------------------------------------------------------------
		// WRITE FILE:
		FileContent fc = new FileContent(fileName, "7amdinaelte5ina");
		WriteMsg wMsg = masterHandler.write(fc);

		System.out.println(wMsg.toString());

		String writeReplicaLoc = wMsg.getLoc().location;
		int writeReplicaPort = wMsg.getLoc().replicaPort;

		// Writing in replica
		Registry registryReplica2 = LocateRegistry.getRegistry(writeReplicaLoc,
				writeReplicaPort);
		ReplicaServerClientInterface replicaHandler = (ReplicaServerClientInterface) registryReplica2
				.lookup("ReplicaServerClientInterface");
		int msgNum = (int) Math.ceil(1d * fc.content.length() / msgSize);
		System.out.println(msgNum);
		int start = 0, end = msgSize;
		AckMsg msg = new AckMsg(wMsg.getTransactionId(), 1);
		for (int i = 0; i < msgNum; i++) {
			int temp = end < fc.content.length() ? end : fc.content.length();
			// System.out.println(start + "   " + temp);
			String msgContent = fc.content.substring(start, temp);
			System.out.println(msgContent);
			msg = replicaHandler.write(msg.getTxnID(), msg.getMsgSeqNum(),
					new FileContent(fc.fileName, msgContent));
			start = temp;
			end += msgSize;
			System.out.println(msg.toString());
		}
		replicaHandler.commit(wMsg.getTransactionId(), msgNum);
	}
}
