import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class replicaServer implements ReplicaServerClientInterface {
	String replicaLocation;
	String replicaPath;
	int portNum;
	int replicaID;
	HashMap<Long, TreeMap<Long, String>> txnToData;
	HashMap<Long, String> tranx;
	MasterServerClientInterface masterHandler;
	HashMap<Integer, ReplicaLoc> replicaSlaves;

	public replicaServer(String replicaLoc, int portNum, String replicaPath,
			int replicaID) throws IOException, NotBoundException {
		this.replicaLocation = replicaLoc;
		this.portNum = portNum;
		this.replicaPath = replicaPath;
		this.replicaID = replicaID;
		this.txnToData = new HashMap<Long, TreeMap<Long, String>>();
		this.tranx = new HashMap<Long, String>();
		initMasterServer();
	}

	private void initMasterServer() throws IOException, NotBoundException {
		Configurator config = Configurator.getInstance();
		Registry registryMaster = LocateRegistry.getRegistry(
				config.getMasterHostname(), config.getMasterPort());
		masterHandler = (MasterServerClientInterface) registryMaster
				.lookup(Global.MASTER_LOOKUP);
		replicaSlaves = masterHandler.getReplicaPaths();
	}

	@Override
	public AckMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		if (!tranx.containsKey(txnID))
			tranx.put(txnID, data.fileName);

		if (txnToData.containsKey(txnID))
			txnToData.get(txnID).put(msgSeqNum, data.fileContent);
		else {
			txnToData.put(txnID, new TreeMap<Long, String>());
			txnToData.get(txnID).put(msgSeqNum, data.fileContent);
		}

		return new AckMsg(txnID, ++msgSeqNum);
	}

	@Override
	public FileContent read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		File file = new File("../" + fileName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String content = new String(data, "UTF-8");
		FileContent fileContent = new FileContent(fileName, content);
		return fileContent;
	}

	@Override
	public boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException {

		// TODO lock
		String fileName = tranx.get(txnID);
		try {
			System.out.println("Commiting to replica : " + replicaPath
					+ "/replica" + this.replicaID + "/" + fileName);

			// Append to file and not overwrite
			PrintWriter fstream = new PrintWriter(new BufferedWriter(
					new FileWriter(replicaPath + "/replica" + this.replicaID
							+ "/" + fileName, true)));

			TreeMap<Long, String> tMap = txnToData.get(txnID);
			for (Iterator<String> iterator = tMap.values().iterator(); iterator
					.hasNext();) {
				fstream.print(iterator.next());
			}
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error in commit: " + e.getMessage());
			return false;
		}

		// unlock
		try {
			broadcastToReplicas(txnID, fileName);
		} catch (NotBoundException e) {
			System.err.println("Error: Broadcast exception");
			return false;
		}
		return true;
	}

	public void broadcastToReplicas(long txID, String fileName)
			throws RemoteException, NotBoundException {
		for (Iterator<Entry<Integer, ReplicaLoc>> iterator = replicaSlaves
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<Integer, ReplicaLoc> entry = iterator.next();

			// skip self-replica
			if (entry.getKey() == this.replicaID)
				continue;

			ReplicaLoc repl = entry.getValue();
			String replLoc = repl.location;
			int replPort = repl.replicaPort;

			// Reading from replica
			Registry replicaReg = LocateRegistry.getRegistry(replLoc, replPort);
			ReplicaServerClientInterface replHandler = (ReplicaServerClientInterface) replicaReg
					.lookup(Global.REPLICA_LOOKUP);
			replHandler.broadcast(fileName, txnToData.get(txID));
		}
	}

	@Override
	public void broadcast(String flName, TreeMap<Long, String> mapValues)
			throws RemoteException {
		try {
			System.out.println("Broadcasting to : " + replicaPath + "/replica"
					+ this.replicaID + "/" + flName);

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(replicaPath + "/replica" + this.replicaID
							+ "/" + flName, true)));
			for (Iterator<String> iterator = mapValues.values().iterator(); iterator
					.hasNext();) {
				out.print((iterator.next()));
			}
			out.close();
		} catch (Exception e) {
			System.err.println("Error in broadcast method: " + e.getMessage());
		}
	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		try {
			txnToData.remove(txnID);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(
					Global.REPLICA_INPUT_PATH));
			String line;
			int id = 0;
			while ((line = buff.readLine()) != null) {
				StringTokenizer st;
				st = new StringTokenizer(line, Global.REPLICA_DELIM);
				String replicaHostname = st.nextToken();
				int replicaPortNo = Integer.parseInt(st.nextToken());
				String replicaPath = st.nextToken();

				LocateRegistry.createRegistry(replicaPortNo);

				replicaServer obj = new replicaServer(replicaHostname,
						replicaPortNo, replicaPath, id++);

				File replicaFolder = new File(replicaPath + "/replica"
						+ obj.replicaID + "/");
				if (!replicaFolder.exists())
					replicaFolder.mkdir();

				ReplicaServerClientInterface stub = (ReplicaServerClientInterface) UnicastRemoteObject
						.exportObject(obj, 0);
				// Bind the remote object's stub in the registry
				Registry registry = LocateRegistry.getRegistry(replicaPortNo);
				registry.rebind(Global.REPLICA_LOOKUP, stub);
			}

			buff.close();
			System.err.println("Replica Server ready");
		} catch (Exception e) {
			System.err.println("Replica Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void createFile(String fileName) throws RemoteException {
		try {
			File file = new File(replicaPath + "/replica" + this.replicaID
					+ "/" + fileName);
			if (file.createNewFile()) {
				System.out.println("File is created !");
			} else {
				System.out.println("File already exists.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
