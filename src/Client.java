import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Client {

	public static final int msgSize = 2; // in characters

	public static void main(String[] args) throws NotBoundException,
			FileNotFoundException, IOException, MessageNotFoundException {

		Configurator config = Configurator.getInstance();

		// Get Registry Server
		Registry registryMaster = LocateRegistry.getRegistry(
				config.getMasterHostname(), config.getMasterPort());
		MasterServerClientInterface masterHandler = (MasterServerClientInterface) registryMaster
				.lookup(Global.MASTER_LOOKUP);

		// Start Client Parser

		BufferedReader buff = new BufferedReader(new InputStreamReader(
				System.in));
		StringTokenizer st;
		String command, fileName, data;
		while (true) {
			st = new StringTokenizer(buff.readLine());
			command = st.nextToken();
			fileName = st.nextToken();
			if (command.equals("W")) {// write only
				data = st.nextToken();

				// WRITE FILE:
				FileContent fc = new FileContent(fileName, data);
				WriteMsg wMsg = masterHandler.write(fc);

				String writeReplicaLoc = wMsg.getLoc().location;
				int writeReplicaPort = wMsg.getLoc().replicaPort;

				// Writing in replica
				Registry registryReplica2 = LocateRegistry.getRegistry(
						writeReplicaLoc, writeReplicaPort);
				ReplicaServerClientInterface replicaHandler = (ReplicaServerClientInterface) registryReplica2
						.lookup(Global.REPLICA_LOOKUP);

				int msgNum = (int) Math.ceil(1d * fc.fileContent.length()
						/ msgSize);
				int start = 0, end = msgSize;
				AckMsg msg = new AckMsg(wMsg.getTransactionId(), 1);
				for (int i = 0; i < msgNum; i++) {
					int temp = end < fc.fileContent.length() ? end
							: fc.fileContent.length();
					String msgContent = fc.fileContent.substring(start, temp);
					msg = replicaHandler.write(msg.getTxnID(), msg
							.getMsgSeqNum(), new FileContent(fc.fileName,
							msgContent));
					start = temp;
					end += msgSize;
				}
				replicaHandler.commit(wMsg.getTransactionId(), msgNum);
			} else {
				// READ FILE:
				// Get primary replica from Master server
				ReplicaLoc[] rlocs = masterHandler.read(fileName);

				String replicaLoc = rlocs[0].location;
				int replicaPort = rlocs[0].replicaPort;

				// Reading from replica
				Registry registryReplica1 = LocateRegistry.getRegistry(
						replicaLoc, replicaPort);
				ReplicaServerClientInterface replicaHandler = (ReplicaServerClientInterface) registryReplica1
						.lookup(Global.REPLICA_LOOKUP);
				FileContent fileContent = replicaHandler.read(fileName);
				System.out.println(fileContent.toString());
			}
		}
	}
}
