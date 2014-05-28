import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Configurator {
	private String masterHostname;
	private int masterPortNo;
	private static Configurator myConfig;

	private Configurator() throws IOException {
		this.masterHostname = "";
		this.masterPortNo = -1;
		readConfig();
	}

	public static Configurator getInstance() throws IOException {
		if (myConfig == null) {
			return myConfig = new Configurator();
		} else {
			return myConfig;
		}
	}

	public String getMasterHostname() {
		return masterHostname;
	}

	public int getMasterPort() {
		return masterPortNo;
	}

	private void readConfig() throws IOException {
		BufferedReader buff = new BufferedReader(new FileReader(
				"../ServerConfig.in"));
		StringTokenizer st;

		// First entry in the configuration file is the master server
		st = new StringTokenizer(buff.readLine(), ",");
		this.masterHostname = st.nextToken();
		this.masterPortNo = Integer.parseInt(st.nextToken());

		buff.close();
	}
}
