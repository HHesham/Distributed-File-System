import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class replicaServer implements ReplicaServerClientInterface {

	@Override
	public WriteMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	static int registryPort = 5005;

	public static void main(String[] args) {
		try {

			// System.setProperty("java.rmi.server.hostname", "localhost");

			LocateRegistry.createRegistry(registryPort);

			replicaServer obj = new replicaServer();
			ReplicaServerClientInterface stub = (ReplicaServerClientInterface) UnicastRemoteObject
					.exportObject(obj, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(registryPort);
			registry.rebind("ReplicaServerClientInterface", stub);

			System.err.println("Replica Server ready");
		} catch (Exception e) {
			System.err.println("Replica Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
