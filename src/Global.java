public class Global {
	public static final int READACK = 0;
	public static final int WRITEACK = 1;
	public static final int BROADCASTSTARTACK = 2;
	public static final int BROADCASTENDACK = 3;
	public static final int ABORTACK = 4;
	public static final String MASTER_DELIM = ",";
	public static final String REPLICA_DELIM = ",";
	public static final String REPLICA_INPUT_PATH = "../repServers.in";
	public static final String MASTER_CONFIG_PATH = "../ServerConfig.in";
	public static final String MASTER_LOOKUP = "MasterServerClientInterface";
	public static final String REPLICA_LOOKUP = "ReplicaServerClientInterface";
	public static final String FILES_DIRECTORY = "../filesDirectory.in";
}
