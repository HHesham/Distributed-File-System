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
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MasterServer implements MasterServerClientInterface {
	HashMap<Integer, ReplicaLoc> replicaPaths;
	HashMap<String, Integer> filePrimReplica;
	ArrayList<String> files;
	int numReplicas = 4;
	Random rand;
	int txID = 0;
	long executionTime;
	Logger masterLogger;

	public MasterServer() throws IOException {
		this.rand = new Random();
		this.executionTime = 0;
		masterLogger = Logger.getInstance();

		// initialize the file list
		this.files = new ArrayList<String>();
		readFilesDirectory();

		// initialize the file primary replica map
		filePrimReplica = new HashMap<String, Integer>();
		for (int i = 0; i < files.size(); i++)
			filePrimReplica.put(files.get(i), rand.nextInt(numReplicas));

		// initialize the replica path map
		masterLogger
				.logMessage(" Initializing replicas paths in Master Server ");
		replicaPaths = new HashMap<Integer, ReplicaLoc>();
		BufferedReader br = new BufferedReader(new FileReader(
				Global.REPLICA_INPUT_PATH));

		int replicaIndex = 0;
		String line;
		StringTokenizer st;

		while ((line = br.readLine()) != null) {
			st = new StringTokenizer(line, Global.REPLICA_DELIM);
			replicaPaths.put(replicaIndex, new ReplicaLoc(st.nextToken(),
					Integer.parseInt(st.nextToken()), replicaIndex));
			replicaIndex++;
		}
		br.close();

		doHearbeats();
	}

	private void doHearbeats() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				// this code will be executed after 5 seconds

				for (Iterator<Entry<Integer, ReplicaLoc>> iterator = replicaPaths
						.entrySet().iterator(); iterator.hasNext();) {
					Entry<Integer, ReplicaLoc> entry = iterator.next();
					ReplicaLoc repl = entry.getValue();
					String replLoc = repl.location;
					int replPort = repl.replicaPort;

					// Reading from replica
					Registry registryReplica1 = null;
					try {
						registryReplica1 = LocateRegistry.getRegistry(replLoc,
								replPort);
					} catch (RemoteException e) {
						e.printStackTrace();
					}

					ReplicaServerClientInterface replHandler = null;
					try {
						replHandler = (ReplicaServerClientInterface) registryReplica1
								.lookup(Global.REPLICA_LOOKUP);
					} catch (RemoteException | NotBoundException e) {
						e.printStackTrace();
					}
					masterLogger.logMessage("===>> Heartbeats @ sec = "
							+ executionTime);
					try {
						if (!replHandler.checkIsAlive()) {
							masterLogger.logMessage("Replica #"
									+ replHandler.getReplicaID()
									+ " is NOT alive !");
						} else {
							masterLogger.logMessage("Replica #"
									+ replHandler.getReplicaID()
									+ " is alive !");

						}

					} catch (RemoteException e) {
						e.printStackTrace();
					}
					masterLogger.logMessage("===>> Heartbeats END");
				}

				executionTime += 10;
				doHearbeats();
			}
		}, 10 * 1000);
	}

	private void readFilesDirectory() throws IOException {
		BufferedReader buff = new BufferedReader(new FileReader(
				Global.FILES_DIRECTORY));
		String fName = "";
		while (!(fName = buff.readLine()).equals("#"))
			this.files.add(fName);

		buff.close();
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

			return new ReplicaLoc[] { primLoc };
		}
		return null;
	}

	@Override
	public WriteMsg write(FileContent data) throws RemoteException,
			IOException, NotBoundException {
		if (!files.contains(data.fileName)) {
			// create new file and set meta data
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
						.lookup(Global.REPLICA_LOOKUP);
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

			obj.masterLogger.logMessage("\t\t Master Server ready ! ");
		} catch (Exception e) {
			System.err.println(" Master Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}