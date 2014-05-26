import java.io.Serializable;

public class FileContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7175077475646791115L;
	String fileName;
	String content;

	public FileContent(String fileName, String content) {
		this.fileName = fileName;
		this.content = content;
	}

	@Override
	public String toString() {
		return "FileContent [fileName=" + fileName + "\n Content=" + content
				+ "]";
	}

}
