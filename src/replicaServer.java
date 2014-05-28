import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class replicaServer implements ReplicaServerClientInterface {
	String replicaLocation;
	String replicaPath;
	int portNum;
	int replicaID;
	HashMap<Long, TreeMap<Long, String>> map;
	HashMap<Long, String> tranx;

	public replicaServer(String replicaLoc, int portNum, String replicaPath,
			int replicaID) {
		this.replicaLocation = replicaLoc;
		this.portNum = portNum;
		this.replicaPath = replicaPath;
		this.replicaID = replicaID;
		this.map = new HashMap<Long, TreeMap<Long, String>>();
		this.tranx = new HashMap<Long, String>();
	}

	@Override
	public WriteMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		if (!tranx.containsKey(txnID))
			tranx.put(txnID, data.fileName);
		String content = data.content;
		// String tempFileName = "../" + data.fileName + "." + txnID + ".txt";
		if (map.containsKey(txnID))
			map.get(txnID).put(msgSeqNum, data.content);
		else
			map.put(txnID, new TreeMap<Long, String>());
		// try {
		// // File tempFile = new File(tempFileName);
		// FileWriter fstream = new FileWriter(tempFileName);
		// BufferedWriter out = new BufferedWriter(fstream);
		// out.write(content);
		// out.close();
		// } catch (Exception e) {
		// System.err.println("Error: " + e.getMessage());
		// }
		WriteMsg wMsg = new WriteMsg(txnID, ++msgSeqNum, new ReplicaLoc(
				replicaLocation, portNum, replicaID));
		System.out.println(content);
		return wMsg;
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
		System.out.println(map.get(txnID).toString());

		// TODO lock
		String fileName = tranx.get(txnID);
		try {
			FileWriter fstream = new FileWriter(replicaPath + "/replica"
					+ this.replicaID + "/" + fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			TreeMap<Long, String> tMap = map.get(txnID);
			for (Iterator<String> iterator = tMap.values().iterator(); iterator
					.hasNext();) {
				out.write((iterator.next()));
			}
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// unlock

		return false;
	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(
					"../repServers.in"));
			String line;
			int id = 0;
			while ((line = buff.readLine()) != null) {
				StringTokenizer st;
				st = new StringTokenizer(line, ",");
				String replicaHostname = st.nextToken();
				int replicaPortNo = Integer.parseInt(st.nextToken());
				String replicaPath = st.nextToken();

				LocateRegistry.createRegistry(replicaPortNo);

				replicaServer obj = new replicaServer(replicaHostname,
						replicaPortNo, replicaPath, id++);

				File replicaFolder = new File(replicaPath + "/replica"
						+ obj.replicaID);
				if (!replicaFolder.exists())
					replicaFolder.mkdir();

				ReplicaServerClientInterface stub = (ReplicaServerClientInterface) UnicastRemoteObject
						.exportObject(obj, 0);
				// Bind the remote object's stub in the registry
				Registry registry = LocateRegistry.getRegistry(replicaPortNo);
				registry.rebind("ReplicaServerClientInterface", stub);
			}

			buff.close();
			System.err.println("Replica Server ready");
		} catch (Exception e) {
			System.err.println("Replica Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
