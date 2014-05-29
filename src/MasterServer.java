import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Map.Entry;

public class MasterServer implements MasterServerClientInterface {
	HashMap<Integer, ReplicaLoc> replicaPaths;
	HashMap<String, Integer> filePrimReplica;
	ArrayList<String> files;
	int numReplicas = 4;
	Random rand;
	final String repServers = "../repServers.in";
	int txID = 0;

	public MasterServer() throws IOException {
		rand = new Random();
		// initialize the file list
		files = new ArrayList<String>();
		files.add("7amda");
		// TODO

		// initialize the file primary replica map
		filePrimReplica = new HashMap<String, Integer>();
		for (int i = 0; i < files.size(); i++)
			filePrimReplica.put(files.get(i), rand.nextInt(numReplicas));

		// initialize the replica path map
		replicaPaths = new HashMap<Integer, ReplicaLoc>();
		BufferedReader br = new BufferedReader(new FileReader(repServers));
		int replicaIndex = 0;
		String line;
		StringTokenizer st;

		while ((line = br.readLine()) != null) {
			System.out.println(line);
			st = new StringTokenizer(line, ",");
			replicaPaths.put(replicaIndex, new ReplicaLoc(st.nextToken(),
					Integer.parseInt(st.nextToken()), replicaIndex));
			replicaIndex++;
		}
		br.close();
	}

	@Override
	public HashMap<Integer, ReplicaLoc> getReplicaPaths()
			throws RemoteException {
		return replicaPaths;
	}

	@Override
	public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		if (files.contains(fileName)) {
			ReplicaLoc primLoc = replicaPaths
					.get(filePrimReplica.get(fileName));
			// TODO
			// return all replicas
			return new ReplicaLoc[] { primLoc };
		}
		return null;
	}

	@Override
	public WriteMsg write(FileContent data) throws RemoteException,
			IOException, NotBoundException {
		if (!files.contains(data.fileName)) {
			// create new file and set metadata
			files.add(data.fileName);
			filePrimReplica.put(data.fileName, rand.nextInt(numReplicas));

			for (Iterator<Entry<Integer, ReplicaLoc>> iterator = replicaPaths
					.entrySet().iterator(); iterator.hasNext();) {
				Entry<Integer, ReplicaLoc> entry = iterator.next();
				ReplicaLoc repl = entry.getValue();
				String replLoc = repl.location;
				int replPort = repl.replicaPort;

				// Reading from replica
				Registry registryReplica1 = LocateRegistry.getRegistry(replLoc,
						replPort);
				ReplicaServerClientInterface replHandler = (ReplicaServerClientInterface) registryReplica1
						.lookup("ReplicaServerClientInterface");
				replHandler.createFile(data.fileName);
			}
		}
		ReplicaLoc primLoc = replicaPaths.get(filePrimReplica
				.get(data.fileName));
		WriteMsg wMsg = new WriteMsg(txID++, 1, primLoc);
		System.out.println(wMsg.getTransactionId() + "          oooo");
		return wMsg;
	}

	public static void main(String[] args) {
		try {
			Configurator config = Configurator.getInstance();
			String masterHostname = config.getMasterHostname();
			int masterPort = config.getMasterPort();

			System.setProperty("java.rmi.server.hostname", masterHostname);

			LocateRegistry.createRegistry(masterPort);
			MasterServer obj = new MasterServer();
			MasterServerClientInterface stub = (MasterServerClientInterface) UnicastRemoteObject
					.exportObject(obj, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(masterPort);
			registry.rebind("MasterServerClientInterface", stub);

			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}