import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Logger {
	private static Logger masterLogger;
	private PrintStream masterLog;

	private Logger() throws FileNotFoundException, UnsupportedEncodingException {
		masterLog = new PrintStream("master-server.log", "UTF-8");
		System.setErr(masterLog); // direct error messages to masterLog
	}

	public static Logger getInstance() throws FileNotFoundException,
			UnsupportedEncodingException {
		if (masterLogger == null) {
			return masterLogger = new Logger();
		} else {
			return masterLogger;
		}
	}

	public void logMessage(String msg) {
		masterLog.println(msg);
	}

	public void closeLogger() {
		masterLog.close();
	}
}
